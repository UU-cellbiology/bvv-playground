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

import bdv.BigDataViewer;
import bdv.tools.brightness.ConverterSetup;
import bdv.util.AxisOrder;
import bdv.util.RandomAccessibleIntervalSource;
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


public class Example_test
{
	/**
	 * Show 16-bit volume.
	 */
	public static void main( final String[] args )
	{
		final ImagePlus imp = IJ.openImage( "https://imagej.nih.gov/ij/images/t1-head.zip" );
		final Img< UnsignedShortType > img = ImageJFunctions.wrapShort( imp );
		//final ImagePlus imp = IJ.openImage( "/home/eugene/Desktop/BigTrace_data/ExM_MT_8bit_blur.tif" );
		//final Img< UnsignedByteType > img = ImageJFunctions.wrapByte( imp );
		//final ImagePlus imp = IJ.openImage( "/home/eugene/Desktop/BigTrace_data/ch2.tif" );
		//final Img< UnsignedByteType > img = ImageJFunctions.wrapByte( imp );
		//final ImagePlus imp2 = IJ.openImage( "/home/eugene/Desktop/BigTrace_data/ch1.tif" );
		//final Img< UnsignedByteType > img2 = ImageJFunctions.wrapByte( imp2 );


		final BvvStackSource source =		BvvFunctions.showGamma( img, "test");
		source.setDisplayRange( 0, 255 );
		final BvvStackSource source2 = BvvFunctions.showGamma( Views.translate( img, 0, 0, 100 ), "view", Bvv.options().addTo( source ) );
		source2.setDisplayRange( 0, 255 );
		int zzz = source.getConverterSetups().size();
		RealARGBColorGammaConverterSetup conv1 = (RealARGBColorGammaConverterSetup) source.getConverterSetups().get(0);
		
		conv1.setLUT(RealARGBColorGammaConverterSetup.getRGBLutTable("Spectrum"));
		
		RealARGBColorGammaConverterSetup conv2 = (RealARGBColorGammaConverterSetup) source2.getConverterSetups().get(0);
		
		conv2.setLUT(RealARGBColorGammaConverterSetup.getRGBLutTable("Fire"));
		//final BvvSource source2 = BvvFunctions.show( img2, "view", Bvv.options().addTo( source ) );
		//sds.getConverter();
		BvvHandle handle = source.getBvvHandle();
		//handle.getConverterSetups().s
		
		//handle.getCardPanel()
		
		//AffineTransform3D transform = new AffineTransform3D();
		
		//source.getBvvHandle().getViewerPanel().state().getViewerTransform(transform);

		// source handle can be used to set color, display range, visibility, ...
		//source.setDisplayRange( 0, 555 );
	}
}
