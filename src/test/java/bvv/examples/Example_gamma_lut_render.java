/*-
 * #%L
 * Volume rendering of bdv datasets
 * %%
 * Copyright (C) 2018 - 2021 Tobias Pietzsch
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

package bvv.examples;

import java.util.List;

import org.joml.Matrix4f;

import com.jogamp.opengl.GL;

import bdv.BigDataViewer;
import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bdv.tools.brightness.ConverterSetup;
import bdv.util.AxisOrder;
import bdv.util.RandomAccessibleIntervalSource;
import bdv.viewer.Interpolation;
import bdv.viewer.SourceAndConverter;
import bvv.tools.RealARGBColorGammaConverterSetup;
import bvv.util.Bvv;
import bvv.util.BvvFunctions;
import bvv.util.BvvHandle;
import bvv.util.BvvSource;
import bvv.util.BvvStackSource;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.converter.Converter;
import net.imglib2.converter.RealLUTConverter;
import net.imglib2.display.ColorTable8;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.Views;
import tpietzsch.example2.VolumeViewerPanel;


public class Example_gamma_lut_render
{
	/**
	 * Show 16-bit volume.
	 */
	public static void main( final String[] args )
	{
		
		
		//final ImagePlus imp = IJ.openImage( "https://imagej.nih.gov/ij/images/t1-head.zip" );
		//final Img< UnsignedShortType > img = ImageJFunctions.wrapShort( imp );
		//final ImagePlus imp = IJ.openImage( "/home/eugene/Desktop/BigTrace_data/ExM_MT_8bit_blur.tif" );
		final ImagePlus imp = IJ.openImage( "/home/eugene/Desktop/emma8bit.tif" );
		final Img< UnsignedByteType > img = ImageJFunctions.wrapByte( imp );

		//final ImagePlus imp = IJ.openImage( "/home/eugene/Desktop/emma16bit.tif" );
		//final Img< UnsignedShortType > img = ImageJFunctions.wrapShort( imp );
		//final ImagePlus imp = IJ.openImage( "/home/eugene/Desktop/BigTrace_data/ch2.tif" );
		//final Img< UnsignedByteType > img = ImageJFunctions.wrapByte( imp );
		//final ImagePlus imp2 = IJ.openImage( "/home/eugene/Desktop/BigTrace_data/ch1.tif" );
		//final Img< UnsignedByteType > img2 = ImageJFunctions.wrapByte( imp2 );

		
		//final BvvStackSource source = BvvFunctions.showGamma( img, "test");
		final BvvStackSource source = BvvFunctions.show( img, "test");
		
		//source.setDisplayRange( 10000, 20000 );
		//source.setDisplayRangeBounds(0, 555);
		//final BvvStackSource source2 = BvvFunctions.showGamma( Views.translate( img, 0, 0, 500 ), "view", Bvv.options().addTo( source ) );
		//source2.setDisplayRange( 0, 455 );
		//source2.setDisplayRangeBounds(0, 555);

		RealARGBColorGammaConverterSetup conv1 = (RealARGBColorGammaConverterSetup) source.getConverterSetups().get(0);
		
		//set LUT
		//conv1.setLUT(RealARGBColorGammaConverterSetup.getRGBLutTable("Fire"));
		//set render type (1 = transparency, 0 = max intensity projection)
		conv1.setRenderType(0);
	

		RealARGBColorGammaConverterSetup conv2 = (RealARGBColorGammaConverterSetup) source.getConverterSetups().get(1);		
		//conv2.setLUT(RealARGBColorGammaConverterSetup.getRGBLutTable("Grays"));
		conv2.setRenderType(0);
		
		
		
		//final VolumeViewerPanel viewer = source.getBvvHandle().getViewerPanel();
		//viewer.state().setInterpolation(Interpolation.NLINEAR);
		////set background color
		//viewer.setRenderScene( ( gl, data ) -> {
		//	gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		//	gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		//} );
	
		//viewer.requestRepaint();

	}
}
