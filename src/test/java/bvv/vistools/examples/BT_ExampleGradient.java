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
import java.util.List;

import javax.swing.UIManager;

import static net.imglib2.cache.img.DiskCachedCellImgOptions.options;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;

import bdv.cache.SharedQueue;
import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.volatiles.VolatileViews;
import btbvv.vistools.Bvv;
import btbvv.vistools.BvvFunctions;
import btbvv.vistools.BvvOptions;
import btbvv.vistools.BvvSource;
import btbvv.vistools.BvvStackSource;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.LutLoader;
import ij.process.ByteProcessor;
import mpicbg.spim.data.SpimDataException;

import net.imglib2.Cursor;
import net.imglib2.FinalRealInterval;
import net.imglib2.algorithm.convolution.Convolution;
import net.imglib2.algorithm.convolution.kernel.Kernel1D;
import net.imglib2.algorithm.convolution.kernel.SeparableKernelConvolution;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.cache.img.optional.CacheOptions.CacheType;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.parallel.Parallelization;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.LinAlgHelpers;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;


public class BT_ExampleGradient {
	
	/**
	 * Show 16-bit volume, change rendering type and gamma
	 */
	public static void main( final String[] args )
	{
		
		//regular tif init
		/**/
		//final ImagePlus imp = IJ.openImage( "https://imagej.nih.gov/ij/images/t1-head.zip" );
		final ImagePlus imp = IJ.openImage( "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/t1-head.tif" );
		//final ImagePlus imp = IJ.openImage( "/home/eugene/Desktop/projects/BigTrace/BB/BB_16bit_crop.tif" );

		final Img< UnsignedShortType > img = ImageJFunctions.wrapShort( imp );
		final BvvSource source = BvvFunctions.show( Views.translate( img, new long[]  {0,0,150}), "t1-head" );

		double [] minI = img.minAsDoubleArray();
		double [] maxI = img.maxAsDoubleArray();
		/**/

		//BDV XML init
		/*
		final String xmlFilename = "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/head_2ch.xml";
		SpimDataMinimal spimData = null;
		try {
			spimData = new XmlIoSpimDataMinimal().load( xmlFilename );
		} catch (SpimDataException e) {
			e.printStackTrace();
		}		
		final List< BvvStackSource< ? > > sources = BvvFunctions.show( spimData );
		final BvvSource source = sources.get(0);
		double [] minI = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(0).minAsDoubleArray();
		double [] maxI = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(0).maxAsDoubleArray();
		*/
	
		source.setDisplayRangeBounds( 0, 40000 );
		source.setDisplayRange(0, 655);
		final long[] dim = Intervals.dimensionsAsLongArray( img );	
		double [][] kernels;
		double [] gammas = new double[3];
		for (int d=0; d<3; d++)
			gammas[d]=2.0;
		ArrayImg<FloatType, FloatArray> gradient = ArrayImgs.floats( dim[ 0 ], dim[ 1 ], dim[ 2 ], 3 );
		Kernel1D[] derivKernel;
		for (int d=0; d<3; d++)
		{
			IntervalView< FloatType > grad2 = Views.hyperSlice( gradient, 3, d );
			int [] nDerivOrder = new int [3];
			nDerivOrder[d]++; 
			kernels = DerivConvolutionKernels.convolve_derive_kernel(gammas, nDerivOrder );
			derivKernel = Kernel1D.centralAsymmetric(kernels);
			final Convolution convObjx = SeparableKernelConvolution.convolution( derivKernel );
			Parallelization.runMultiThreaded( () -> {
				convObjx.process(Views.extendBorder(img), grad2 );
			} );
		}
		new ImageJ();
		//ImageJFunctions.show( Views.hyperSlice( gradient, 3, 0 ),"test X" );
		//ImageJFunctions.show( Views.hyperSlice( gradient, 3, 1 ),"test Y" );
		//ImageJFunctions.show( Views.hyperSlice( gradient, 3, 2 ),"test Z" );
		
		ArrayImg<UnsignedShortType, ShortArray> val_grad = ArrayImgs.unsignedShorts( dim[ 0 ], dim[ 1 ], dim[ 2 ] );
		final Cursor< FloatType > in0 = Views.flatIterable( Views.hyperSlice( gradient, 3, 0 ) ).cursor();
		final Cursor< FloatType > in1 = Views.flatIterable( Views.hyperSlice( gradient, 3, 1 ) ).cursor();
		final Cursor< FloatType > in2 = Views.flatIterable( Views.hyperSlice( gradient, 3, 2 ) ).cursor();
		final Cursor< UnsignedShortType > out = Views.flatIterable( val_grad ).cursor();
		while ( out.hasNext() )
		{
			final double [] vect = new double[3];
			vect[0]=in0.next().get();
			vect[1]=in1.next().get();
			vect[2]=in2.next().get();
			
			out.next().set( (int) (Math.round(  Math.abs(LinAlgHelpers.length( vect )))) );
		}
		BvvFunctions.show( val_grad, "grad x 1", BvvOptions.options().addTo( source ) );
		
//		final UnsignedShortType type = new UnsignedShortType();
//		final int[] cellDimensions = new int[] { 32, 32, 32 };
//		final DiskCachedCellImgOptions factoryOptions = options()
//				.cellDimensions( cellDimensions )
//				.cacheType( CacheType.SOFTREF );
//		final DiskCachedCellImgFactory< UnsignedShortType > sfactory = new DiskCachedCellImgFactory<>( type, factoryOptions );
//		final Img< UnsignedShortType > diff = sfactory.create( dim, cell -> {
//			final Cursor< FloatType > in0 = Views.flatIterable( Views.hyperSlice( gradient, 3, 0 ) ).cursor();
//			final Cursor< UnsignedShortType > out = Views.flatIterable( cell ).cursor();
//			while ( out.hasNext() )
//			{
//				out.next().set( (Math.round(  Math.abs(in0.next().get() ))) );
//			}
//		}, options().initializeCellsAsDirty( true ) );
//		final SharedQueue queue = new SharedQueue( 7 );
//		BvvFunctions.show( VolatileViews.wrapAsVolatile( diff, queue ), "Gauss 1", BvvOptions.options().addTo( source ) );
//		ImageJFunctions.show( diff,"test G" );
	}
	
}
