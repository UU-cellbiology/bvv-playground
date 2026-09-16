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
		
		// Extract unique LUTs preserving order
		final Map< ColorLutKey, IndexColorModel > uniqueLuts = new LinkedHashMap<>();
		for ( final ConverterSetup cs : renderConverters )
		{
			final GammaConverterSetup gc = (GammaConverterSetup)cs;
			final ColorLutKey key = gc.getLutKey();
			if ( key != null )
				uniqueLuts.putIfAbsent( key, gc.getLutICM() );
		}
		
		//Check if all requested LUTs are already uploaded and mapped
		boolean allPresent = true;
		for ( final ColorLutKey key : uniqueLuts.keySet() )
		{
			if ( !lutToLayerMap.containsKey( key ) )
			{
				allPresent = false;
				break;
			}
		}
		
		if ( allPresent && globalLutArrayTexture != null )
		{
			updateGCSindex( renderConverters );
			return;
		}
		final int neededLayers = Math.max( 1, uniqueLuts.size() );
		// Re-allocate texture array only if capacity is exceeded
		if ( globalLutArrayTexture == null || neededLayers > maxAllocatedLayers )
		{
			if ( globalLutArrayTexture != null )
			{
				context.delete( globalLutArrayTexture );
			}
			
			// Grow capacity to avoid frequent reallocations (min capacity 4)
			maxAllocatedLayers = Math.max( 4, Math.max( neededLayers, maxAllocatedLayers * 2 ) );
			globalLutArrayTexture = new ColorLutArrayTexture( LUT_EDGE, LUT_EDGE, maxAllocatedLayers );
			lutToLayerMap.clear();
		}
		
		//  Upload missing or all layers to texture array
		int currentLayer = 0;
		for ( final Map.Entry< ColorLutKey, IndexColorModel > entry : uniqueLuts.entrySet() )
		{
			final ColorLutKey key = entry.getKey();
			final IndexColorModel icm = entry.getValue();

			// Assign layer index and upload pixel data if not already present in current map
			if ( !lutToLayerMap.containsKey( key ) )
			{
				uploadLayerToTexture( context, icm, currentLayer );
				lutToLayerMap.put( key, currentLayer );
			}
			currentLayer++;
		}

		updateGCSindex( renderConverters );
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
	
	/** Map each setup converter to its corresponding allocated layer index **/
//	private void uploadLayerToTexture( final GpuContext context, final IndexColorModel icm, final int layer )
//	{
//		data.clear();
//		if ( icm != null )
//		{
//			final int size_ = icm.getMapSize();
//			final byte[][] colorsARGB = new byte[4][size_];
//			icm.getAlphas( colorsARGB[0] );
//			icm.getReds( colorsARGB[1] );
//			icm.getGreens( colorsARGB[2] );
//			icm.getBlues( colorsARGB[3] );
//
//			int lastColor = 0;
//			final int count = Math.min( size_, LUT_SQUARE );
//			for ( int i = 0; i < count; i++ )
//			{
//				final int a = colorsARGB[0][i] & 0xff;
//				final int r = colorsARGB[1][i] & 0xff;
//				final int g = colorsARGB[2][i] & 0xff;
//				final int b = colorsARGB[3][i] & 0xff;
//				lastColor = ( a << 24 ) | ( b << 16 ) | ( g << 8 ) | r;
//				sdata.put( i, lastColor );
//			}
//			// Fill remainder if LUT map size < 256
//			for ( int i = count; i < LUT_SQUARE; i++ )
//			{
//				sdata.put( i, lastColor );
//			}
//		}
//
//		data.rewind();
//		// Calls glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, x=0, y=0, z=layer, width=LUT_WIDTH, height=1, depth=1, ...)
//		//globalLutArrayTexture.uploadSubImage3D( context, layer, data );
//		globalLutArrayTexture.uploadLayer( context, layer, data );
//	}
	
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
