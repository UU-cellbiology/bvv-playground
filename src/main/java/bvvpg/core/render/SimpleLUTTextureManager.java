/*-
 * #%L
 * Volume rendering of bdv datasets with gamma and transparency option
 * %%
 * Copyright (C) 2022 - 2024 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
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
import java.util.Map;

import bvvpg.core.backend.GpuContext;
import bvvpg.pguitools.GammaConverterSetup;
import bvvpg.pguitools.LutCSTexturePG;



public class SimpleLUTTextureManager
{
	private final HashMap< GammaConverterSetup, LutCSTexturePG > texturesLUT;
	private final HashMap< LutCSTexturePG, Integer > timestamps;
	private int currentTimestamp;
	
	public SimpleLUTTextureManager()
	{
		texturesLUT = new HashMap<>();
		timestamps = new HashMap<>();
		currentTimestamp = 0;
	}
	
	public synchronized void processTextureLUT( final GpuContext context, GammaConverterSetup gc )
	{
		final LutCSTexturePG texture;
		if(gc.updateNeededLUT())
		{
			final LutCSTexturePG old_texture = texturesLUT.get( gc );
			context.delete(old_texture);
			timestamps.remove( old_texture );
			texturesLUT.remove( gc );			
		}
		texture = texturesLUT.computeIfAbsent( gc, s -> uploadLUTToTexture( context, gc.getLutICM()) );
		gc.setLUTTexture( texture );
		timestamps.put( texture, currentTimestamp );
	}
	
	/**
	 * Free allocated resources associated to all LUTS that have not been
	 * {@link #processTextureLUT(GpuContext,GammaConverterSetup) requested} since the
	 * last call to {@link #freeUnusedLUTs(GpuContext)}.
	 */
	public synchronized void freeUnusedLUTs( final GpuContext context )
	{
		final Iterator< Map.Entry< LutCSTexturePG, Integer > > it = timestamps.entrySet().iterator();


		texturesLUT.entrySet().removeIf( entry -> timestamps.get( entry.getValue() ) < currentTimestamp );
		while ( it.hasNext() )
		{
			final Map.Entry< LutCSTexturePG, Integer > entry = it.next();
			if ( entry.getValue() < currentTimestamp )
			{
				context.delete( entry.getKey() );
				it.remove();
			}
		}
		++currentTimestamp;
	}
	
	public void freeLUTs( final GpuContext context )
	{

		texturesLUT.clear();
		timestamps.keySet().forEach( context::delete );
		timestamps.clear();
	}
	
	private static LutCSTexturePG uploadLUTToTexture( final GpuContext context, final IndexColorModel icm )
	{
		final LutCSTexturePG texture = new LutCSTexturePG();
		final ByteBuffer data;
		if(icm != null)
		{
			int size_ = icm.getMapSize();
			if (size_ < 65536)
			{
				int nTextureSpan = 256*(int)Math.ceil(size_/256.0);
				texture.init( size_ );
				final int numBytes = 4 * nTextureSpan;
				data = ByteBuffer.allocateDirect( numBytes ); // allocate a bit more than needed...
				data.order( ByteOrder.nativeOrder() );	
				final IntBuffer sdata = data.asIntBuffer();
				byte [][] colors = new byte[3][nTextureSpan];
				icm.getReds(colors[0]);
				icm.getGreens(colors[1]);
				icm.getBlues(colors[2]);
				int all = 0;
				for (int i=0; i<size_;i++)
				{
					final int r = colors[0][i] & 0xff;
					final int g = colors[1][i] & 0xff;
					final int b = colors[2][i] & 0xff;
					all = ( 255 << 24 ) | ( b << 16 ) | ( g << 8 ) | r;
					sdata.put( i, all );	
				}
				//fill the rest with the last color
				for (int i=size_; i<nTextureSpan;i++)
				{
					sdata.put( i, all );
				}
				texture.upload( context, data );
				return texture;
			}
			
			System.out.println("Error! Provided LUT exceeds current maximum LUT size of 65536, loading dummy LUT.");
		}
		
		//upload dummy
		texture.init( 1 );
		data = ByteBuffer.allocateDirect( 256*4 ); // minimum one row of 2D texture
		data.order( ByteOrder.nativeOrder() );	
		texture.upload( context, data );
		return texture;

	}
}
