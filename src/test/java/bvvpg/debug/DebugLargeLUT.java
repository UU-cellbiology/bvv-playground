/*-
 * #%L
 * Volume rendering of bdv datasets with gamma and transparency option
 * %%
 * Copyright (C) 2022 - 2025 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
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

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;

import java.awt.image.IndexColorModel;

import javax.swing.UIManager;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import bvvpg.vistools.Bvv;
import bvvpg.vistools.BvvFunctions;
import bvvpg.vistools.BvvSource;

import ij.ImageJ;

public class DebugLargeLUT
{
	public static void main( final String[] args )
	{
		//switch to FlatLaf theme		
		try {
		    UIManager.setLookAndFeel( new FlatIntelliJLaf() );
		    FlatLaf.registerCustomDefaultsSource( "flatlaf" );
		    FlatIntelliJLaf.setup();
		} catch( Exception ex ) {
		    System.err.println( "Failed to initialize LaF" );
		}
		
		int nImageMaxRange = 1024;
		ArrayImg< UnsignedShortType, ShortArray > dirsInt = ArrayImgs.unsignedShorts(new long [] {nImageMaxRange,1,1 });
		Cursor< UnsignedShortType > cursor = Views.flatIterable( dirsInt ).cursor();
		int i = 0;
		while(cursor.hasNext())
		{
			cursor.fwd();
			cursor.get().set( i );
			i++;
		}
		new ImageJ();
		
		IntervalView< UnsignedShortType > imgLUT = Views.expandBorder( ( RandomAccessibleInterval< UnsignedShortType > ) dirsInt, new long [] {0,50,50 });

		final BvvSource source = BvvFunctions.show( imgLUT, "LargeLUTView" );
		//int nLUTMAX = nImageMaxRange;
		int nLUTMAX = 256;
		source.setLUT(  getLargeICM(nLUTMAX), null );
		source.setDisplayRangeBounds( 0, nImageMaxRange -1);

		source.setRenderType( 1 );
		source.setAlphaRangeBounds( 0, 2 );
		source.setAlphaRange( 0, 2 );
		
		//add lut with variable alphas
		AffineTransform3D transform = new AffineTransform3D();
		transform.translate( 0.0,150.0,0.0 );
		final BvvSource source2 = BvvFunctions.show( imgLUT, "LargeLUTAlphaView", Bvv.options().sourceTransform( transform ).addTo( source ) );
		source2.setLUT(  getLargeICMwithWaveAlphas(nLUTMAX), null );
		source2.setDisplayRangeBounds( 0, nImageMaxRange -1);
		source2.setRenderType( 1 );
		source2.setAlphaRangeBounds( 0, 2 );
		source2.setAlphaRange( 0, 2 );
	}
	
	public static IndexColorModel getLargeICM(int nTotLength)
	{
		
		final byte [][] colors = new byte [3][nTotLength];
		colors[0][0] = ( byte ) 0;
		colors[1][0] = ( byte )  0 ;
		colors[2][0] = ( byte ) 255 ;
		for(int i=1;i<nTotLength;i++)
		{
			int nStep = (int) Math.round( 255.0*i/(nTotLength));
			colors[0][i] = ( byte ) nStep ;
			colors[1][i] = ( byte )  (255-nStep ) ;
			colors[2][i] = ( byte ) 0 ;
		}
		colors[0][nTotLength-1] = ( byte ) 255;
		colors[1][nTotLength-1] = ( byte )  255 ;
		colors[2][nTotLength-1] = ( byte ) 255 ;
		
		return new IndexColorModel(16,nTotLength,colors[0],colors[1],colors[2]);
	}
	
	public static IndexColorModel getLargeICMwithWaveAlphas(int nTotLength)
	{
		
		final byte [][] colors = new byte [4][nTotLength];
		colors[0][0] = ( byte ) 0;
		colors[1][0] = ( byte )  0 ;
		colors[2][0] = ( byte ) 255 ;
		colors[3][0] = ( byte ) 255 ;
		
		for(int i=1;i<nTotLength;i++)
		{
			int nStep = (int) Math.round( 255.0*i/(nTotLength));
			colors[0][i] = ( byte ) nStep ;
			colors[1][i] = ( byte )  (255-nStep ) ;
			colors[2][i] = ( byte ) 0 ;
			colors[3][i] = ( byte ) Math.round(255*0.5*(Math.cos(20.0*Math.PI*i/nTotLength )+1)) ;

		}
		colors[0][nTotLength-1] = ( byte ) 255;
		colors[1][nTotLength-1] = ( byte )  255 ;
		colors[2][nTotLength-1] = ( byte ) 255 ;
		colors[3][nTotLength-1] = ( byte ) 255 ;

		return new IndexColorModel(16,nTotLength,colors[0],colors[1],colors[2],colors[3]);
	}
}
