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
package bvvpg.debug;


import net.imglib2.Cursor;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayLocalizingCursor;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class GenerateVolumes
{

	static public < T extends RealType< T > & NativeType< T >> ArrayImg< T, ? > makeSimpleRAI(final T type, final int nEdge, final double minAmp, final double maxAmp, final boolean bConstantMax)
	{
		final long[] dims = new long[] {nEdge, nEdge, nEdge};
		ArrayImgFactory<T> factoryImg = new ArrayImgFactory<>(type);
		ArrayImg< T, ? > img = factoryImg.create( dims );
		ArrayLocalizingCursor< T > cursor = img.localizingCursor();
		double period = nEdge * 0.5 + Math.random() *  nEdge * 0.5;

		final double [] pos = new double[3];
		while(cursor.hasNext())
		{
			cursor.fwd();
			cursor.localize( pos );
			if(bConstantMax)
			{
				cursor.get().setReal( maxAmp ); 
			}
			else
			{
				double val = gyroid(pos, period, minAmp, maxAmp);
				cursor.get().setReal( val ); 
			}
		}
		return img;
	}
	
	/** creates a 3D cube volume with nEdge size filled with gyroid in the range of minAmp and maxAmp.
	 *  The period of gyroid randomly varies from 0.5 to 1.0 nEdge
	 *  If bConstantMax is true, single values volume filled with maxAmp**/
	static public < T extends RealType< T > & NativeType< T >> Img< T > makeCachedCellImg(final T type, final int nEdge, final double minAmp, final double maxAmp, final boolean bConstantMax)
	{
		final long[] dims = new long[] {nEdge, nEdge, nEdge};
		final ReadOnlyCachedCellImgFactory factory = new ReadOnlyCachedCellImgFactory(
				ReadOnlyCachedCellImgOptions.options().cellDimensions( 32 ) );
		double period = nEdge * 0.5 + Math.random() *  nEdge * 0.5;
		final Img< T > cellimg = factory.create( dims, type, cell -> {
			Cursor< T > cursor = cell.localizingCursor();
			final double [] pos = new double[3];
			while(cursor.hasNext())
			{
				cursor.fwd();
				cursor.localize( pos );
				if( bConstantMax)
				{
					cursor.get().setReal( maxAmp ); 
				}
				else
				{
					final double val = gyroid(pos, period, minAmp, maxAmp);
					cursor.get().setReal( val ); 					
				}
			}
			//Thread.sleep( 80 );
		});

		return cellimg;
	}


	static double gyroid(final double [] pos, final double period, final double minAmp, final double maxAmp) 
	{
		double w = 2.0 * Math.PI / period;

		double g = Math.sin(pos[0] * w ) * Math.cos(pos[1] * w) 
				+ Math.sin(pos[1] * w ) * Math.cos(pos[2] * w ) 
				+ Math.sin(pos[2] * w ) * Math.cos(pos[0] * w );
		g = Math.pow((Math.tanh( g ) + 1) * 0.5, 7);
		return g  * (maxAmp - minAmp) + minAmp;
	}
}
