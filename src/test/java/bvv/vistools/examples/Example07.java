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

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import org.joml.Matrix4f;

import btbvv.core.VolumeViewerPanel;
import btbvv.vistools.Bvv;
import btbvv.vistools.BvvFunctions;
import btbvv.vistools.BvvSource;
import bvv.vistools.examples.scene.TexturedUnitCube;

public class Example07
{
	/**
	 * ImgLib2 :-)
	 */
	public static void main( final String[] args )
	{
		new ImageJ();
		final ImagePlus imp = IJ.openImage( "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/t1-head.zip" );
		final Img< UnsignedShortType > img = ImageJFunctions.wrapShort( imp );

		final BvvSource source = BvvFunctions.show( img, "t1-head",
				Bvv.options().maxAllowedStepInVoxels( 0 ).renderWidth( 1280 ).renderHeight( 720).preferredSize( 1024, 1024 ) );
		source.setDisplayRange( 0, 800 );
		source.setColor( new ARGBType( 0xffff8800 ) );

		final TexturedUnitCube cube = new TexturedUnitCube( "imglib2.png" );
		final TexturedUnitCube cube2 = new TexturedUnitCube( "imglib2.png" );
		cube.fOpacity = 0.75f;
		final VolumeViewerPanel viewer = source.getBvvHandle().getViewerPanel();
		viewer.setRenderScene( ( gl, data ) -> {
			final Matrix4f cubetransform = new Matrix4f().translate( 100, 150, 65 ).scale( 80 );
			cube.draw( gl, new Matrix4f( data.getPv() ).mul( cubetransform ) );
			final Matrix4f cubetransform2 = new Matrix4f().translate( 170, 150, 65 ).scale( 80 );
			cube2.draw( gl, new Matrix4f( data.getPv() ).mul( cubetransform2 ) );

		} );

		viewer.requestRepaint();
	}
}
