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
package bvv.vistools.examples;


import java.awt.image.IndexColorModel;

import net.imglib2.FinalInterval;
import net.imglib2.Point;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.algorithm.region.hypersphere.HyperSphereCursor;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Actions;

import bvvpg.vistools.Bvv;
import bvvpg.vistools.BvvFunctions;
import bvvpg.vistools.BvvSource;
import ij.IJ;
import ij.ImagePlus;

public class PG_Example02
{
	/** Show difference in the source (volume) interpolation **/

	public static void main( final String[] args )
	{
		final ImagePlus imp = IJ.openImage( "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/t1-head.tif" );
		final Img< UnsignedShortType > img = ImageJFunctions.wrapShort( imp );
		final BvvSource source = BvvFunctions.show( img, "t1-head");
		source.setDisplayRange(0, 700);
		source.setDisplayGamma(0.5);
		//source.setVoxelRenderInterpolation( 0 );
		source.setRenderType( 1 );
		
		final Actions actions = new Actions( new InputTriggerConfig() ); 
		actions.runnableAction(
				() -> {
					source.setVoxelRenderInterpolation( 0 );
					new Thread( () ->
				{
					while (true)
					{
						try
						{
							Thread.sleep( 50 );
						}
						catch ( InterruptedException exc )
						{
							// TODO Auto-generated catch block
							exc.printStackTrace();
						}
						source.setLUT(getRandomICM(256), "test");	
					}
				}).start() ;},
				"glitch",
				"D" );
		actions.install( source.getBvvHandle().getKeybindings(), "mobie-bvv-actions" );

	}
	public static IndexColorModel getRandomICM(int nTotLength)
	{
		
		final byte [][] colors = new byte [3][nTotLength];
		colors[0][0] = ( byte ) 0;
		colors[1][0] = ( byte )  0 ;
		colors[2][0] = ( byte ) 0 ;
		for(int i=1;i<nTotLength;i++)
		{
			int nStep = ( int ) ( Math.random()*(nTotLength-1.0) );
			colors[0][i] = ( byte ) nStep ;
			colors[1][i] = ( byte )  nStep ;
			colors[2][i] = ( byte ) nStep ;
		}

		
		return new IndexColorModel(16,nTotLength,colors[0],colors[1],colors[2]);
	}
}
