/*-
 * #%L
 * Volume rendering of bdv datasets with gamma and transparency option
 * %%
 * Copyright (C) 2022 - 2026 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package bvvpg.core.render;

import java.awt.image.IndexColorModel;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import bdv.tools.brightness.ConverterSetup;
import bvvpg.core.backend.GpuContext;
import bvvpg.core.backend.Texture;
import bvvpg.source.converters.GammaConverterSetup;

public class GlobalColorLutManager

{	
	private ColorLutArrayTexture globalLutArrayTexture = null;
	private final Map<ColorLutKey, Integer> lutToLayerMap = new HashMap<>();
	private final Map< ConverterSetup, Integer > converterToLayerMap = new HashMap<>();
	private final Deque< Integer > freeSlots = new ArrayDeque<>();
	
	private int maxAllocatedLayers = 0;
	private static final int LUT_EDGE = 256; // Fixed resolution for 1D LUT ramps in 2D array slice
	private static final int LUT_SQUARE = LUT_EDGE * LUT_EDGE ; // Fixed resolution for 1D LUT ramps in 2D array slice
	
	final ByteBuffer data = ByteBuffer.allocateDirect( LUT_SQUARE * 4 ).order( ByteOrder.nativeOrder() );
	final ByteBuffer uploadBuffer = ByteBuffer.allocateDirect( LUT_SQUARE * 4 ).order( ByteOrder.nativeOrder() );
	final IntBuffer sdata = data.asIntBuffer();
	
	public GlobalColorLutManager()
	{
	}
	
	public synchronized void updateLUTArray( final GpuContext context, final List< ConverterSetup > renderConverters )
	{
	    if ( renderConverters == null || renderConverters.isEmpty() )
	        return;

	    // Collect unique active LUT keys requested for this frame
	    final Map< ColorLutKey, IndexColorModel > activeLuts = new LinkedHashMap<>();
	    for ( final ConverterSetup cs : renderConverters )
	    {
	        final GammaConverterSetup gc = (GammaConverterSetup) cs;
	        final ColorLutKey key = gc.getLutKey();
	        if ( key != null )
	            activeLuts.putIfAbsent( key, gc.getLutICM() );
	    }
	    
	    // Quick exit: All active LUTs are already mapped and texture exists
	    if ( globalLutArrayTexture != null && lutToLayerMap.keySet().containsAll( activeLuts.keySet() ) )
	    {
	        updateGCSindex( renderConverters );
	        return;
	    }

	    // Reclaim slots from LUTs that are no longer active
	    final Iterator< Map.Entry< ColorLutKey, Integer > > iter = lutToLayerMap.entrySet().iterator();
	    while ( iter.hasNext() )
	    {
	        final Map.Entry< ColorLutKey, Integer > entry = iter.next();
	        if ( !activeLuts.containsKey( entry.getKey() ) )
	        {
	            freeSlots.push( entry.getValue() ); // Free up this layer slot
	            iter.remove();
	        }
	    }

	    // Separate already mapped LUTs from completely new LUTs
	    final List< Map.Entry< ColorLutKey, IndexColorModel > > newLuts = new ArrayList<>();
	    for ( final Map.Entry< ColorLutKey, IndexColorModel > entry : activeLuts.entrySet() )
	    {
	        if ( !lutToLayerMap.containsKey( entry.getKey() ) )
	            newLuts.add( entry );
	    }

	    // Quick exit: No new LUTs need to be uploaded
	    if ( newLuts.isEmpty() && globalLutArrayTexture != null )
	    {
	        updateGCSindex( renderConverters );
	        return;
	    }

	    // Determine if there is a need to resize the GPU texture array
	    final boolean capacityExceeded = ( newLuts.size() > freeSlots.size() );

	    if ( globalLutArrayTexture == null || capacityExceeded )
	    {
	        if ( globalLutArrayTexture != null )
	            context.delete( globalLutArrayTexture );

	        final int totalActiveCount = activeLuts.size();
	        maxAllocatedLayers = Math.max( 4, Math.max( totalActiveCount, maxAllocatedLayers * 2 ) );
	        globalLutArrayTexture = new ColorLutArrayTexture( LUT_EDGE, LUT_EDGE, maxAllocatedLayers );

	        // Reset tracking state
	        lutToLayerMap.clear();
	        freeSlots.clear();
	        //System.out.println( "maxAllocatedLayers " + maxAllocatedLayers );

	        // Populate remaining unused slots for the new larger texture
	        for ( int i = totalActiveCount; i < maxAllocatedLayers; i++ )
	        {
	            freeSlots.push( i );
	        }

	        // Complete re-upload of ALL active LUTs into continuous slots [0 ... N-1]
	        int layerIndex = 0;
	        for ( final Map.Entry< ColorLutKey, IndexColorModel > entry : activeLuts.entrySet() )
	        {
	            uploadLayerToTexture( context, entry.getValue(), layerIndex );
	            lutToLayerMap.put( entry.getKey(), layerIndex );
	            layerIndex++;
	        }
	    }
	    else
	    {
	        // Incremental fill: there is enough freeSlots in the current texture
	        for ( final Map.Entry< ColorLutKey, IndexColorModel > entry : newLuts )
	        {
	            final int targetLayer = freeSlots.pop();
	            uploadLayerToTexture( context, entry.getValue(), targetLayer );
	            lutToLayerMap.put( entry.getKey(), targetLayer );
	        }
	    }
	    updateGCSindex( renderConverters );

//	    updateGCSindex( renderConverters );
//		System.out.println("lutToLayerMap");
//		lutToLayerMap.forEach((key, value) -> System.out.println(key.hashCode() + " => " + value));
//
//		updateGCSindex( renderConverters );
//		System.out.println("converterToLayerMap");
//		converterToLayerMap.forEach((key, value) -> System.out.println(key + " => " + value));
	}
	
	void updateGCSindex (final List< ConverterSetup > renderConverters)
	{
		converterToLayerMap.clear();
		for ( final ConverterSetup cs : renderConverters )
		{		
			final GammaConverterSetup gc = (GammaConverterSetup)cs;
			final ColorLutKey key = gc.getLutKey();
			if(key != null)
			{
				final int layerIndex = lutToLayerMap.getOrDefault( key, 0 );
				converterToLayerMap.put( cs, layerIndex );
			}
		}
	}
	
	private void uploadLayerToTexture( final GpuContext context, final IndexColorModel icm, final int layer )
	{
		uploadBuffer.clear();

		if ( icm != null )
		{
			final int size_ = icm.getMapSize();
			final byte[] alphas = new byte[ size_ ];
			final byte[] reds   = new byte[ size_ ];
			final byte[] greens = new byte[ size_ ];
			final byte[] blues  = new byte[ size_ ];

			icm.getAlphas( alphas );
			icm.getReds( reds );
			icm.getGreens( greens );
			icm.getBlues( blues );

			int lastR = 0, lastG = 0, lastB = 0, lastA = 255;
			final int count = Math.min( size_, LUT_SQUARE );
			for ( int i = 0; i < count; i++ )
			{
				lastR = reds[i] & 0xff;
				lastG = greens[i] & 0xff;
				lastB = blues[i] & 0xff;
				lastA = alphas[i] & 0xff;

				uploadBuffer.put( (byte) lastR );
				uploadBuffer.put( (byte) lastG );
				uploadBuffer.put( (byte) lastB );
				uploadBuffer.put( (byte) lastA );
			}
			// Fill remaining buffer entries if ICM size < LUT_SQUARE
			for ( int i = count; i < LUT_SQUARE; i++ )
			{
				uploadBuffer.put( (byte) lastR );
				uploadBuffer.put( (byte) lastG );
				uploadBuffer.put( (byte) lastB );
				uploadBuffer.put( (byte) lastA );
			}
		}

		uploadBuffer.flip();
		globalLutArrayTexture.uploadLayer( context, layer, uploadBuffer );
	}
	
	public Texture getGlobalLutArrayTexture()
	{
		return globalLutArrayTexture;
	}
	
	public synchronized int getLayerIndex( final ConverterSetup cs )
	{
		return converterToLayerMap.getOrDefault( cs, 0 );
	}
	
	public void freeGlobalLUT( final GpuContext context )
	{
		if ( globalLutArrayTexture != null )
		{
			context.delete( globalLutArrayTexture );
			globalLutArrayTexture = null;
		}
		lutToLayerMap.clear();
		converterToLayerMap.clear();
		maxAllocatedLayers = 0;
	}
}
