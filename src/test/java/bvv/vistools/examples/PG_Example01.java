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
package bvv.vistools.examples;

import java.util.List;
import java.util.ArrayList;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;

import bvvpg.vistools.BvvFunctions;
import bvvpg.vistools.BvvOptions;
import bvvpg.vistools.BvvSource;
import bvvpg.vistools.BvvStackSource;
import ij.IJ;
import ij.ImagePlus;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.sequence.VoxelDimensions;

import net.imglib2.FinalRealInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.integer.UnsignedShortType;


public class PG_Example01 {
	
	/**
	 * Show 16-bit volume, change display range, change gamma,
	 * rendering type, lighting type, alpha value, apply LUT and clip volume in half
	 */
	public static void main( final String[] args )
	{
		
		//regular tif init
		/**/
		//you can download the data here
		//final ImagePlus imp = IJ.openImage( "https://imagej.nih.gov/ij/images/t1-head.zip" );
		final ImagePlus imp = IJ.openImage( "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/t1-head.tif" );
		final Img< UnsignedShortType > img = ImageJFunctions.wrapShort( imp );

		//let's put volumes next to each other
		AffineTransform3D shift2 = new AffineTransform3D();
		shift2.translate( 200.0, 0.0, 0.0 );
		
		AffineTransform3D shift3 = new AffineTransform3D();
		shift3.translate( 400.0, 0.0, 0.0 );
		
		ArrayList<BvvSource> bvvSources = new ArrayList<>();
		
		final BvvSource source1 = BvvFunctions.show( img, "headMAX" );
		bvvSources.add( source1 );
		final BvvSource source2 = BvvFunctions.show( img, "headVOL", BvvOptions.options().addTo( source1 ).sourceTransform( shift2 ) );
		bvvSources.add( source2 );
		final BvvSource source3 = BvvFunctions.show( img, "headSurface", BvvOptions.options().addTo( source1 ).sourceTransform( shift3 ) );
		bvvSources.add( source3 );
		
		
		//clipping interval
		double [] minI = img.minAsDoubleArray();
		double [] maxI = img.maxAsDoubleArray();
		//clip half of the volume along Z axis 	
		minI[2] = 0.5 * maxI[2] ;	
		
		final FinalRealInterval clipInterval = new FinalRealInterval(minI,maxI);
		
		//set volumetric rendering (1), instead of max intensity max intensity (0)
		bvvSources.get( 1 ).setRenderType(1);
		//set isosurface rendering (2), instead of max intensity max intensity (0)
		bvvSources.get( 2 ).setRenderType(2);
		
		//set "shiny" lighting (2) for the surface instead of the plain (0)
		bvvSources.get( 2 ).setLightingType( 2 );
		
		for(final BvvSource source:bvvSources)
		{	
			source.setDisplayRangeBounds( 0, 655 );
			source.setDisplayRange(0, 655);
			source.setDisplayGamma(0.5);
		
			//DisplayRange maps colors (or LUT values) to intensity values
			source.setDisplayRange(0, 400);
			//it is also possible to change gamma value
			//source.setDisplayGamma(0.9);
		
			//alpha channel to intensity mapping can be changed independently
			source.setAlphaRange(0, 500);
			//it is also possible to change alpha-channel gamma value
			//source.setAlphaGamma(0.9);
		
			//assign a "Fire" lookup table to this source
			source.setLUT("Fire");
		
			//or one can assign custom IndexColorModel + name as string
			//in this illustration we going to get IndexColorModel from IJ 
			//(but it could be made somewhere else)
			//final IndexColorModel icm_lut = LutLoader.getLut("Spectrum");
			//source.setLUT( icm_lut, "SpectrumLUT" );

	
			source.setClipInterval(clipInterval);		
			//turn on clipping "inside" mode
			source.setClipState( 1 );
		}
		//move clipping to the corresponding volume position
		bvvSources.get( 1 ).setClipTransform( shift2 );
		bvvSources.get( 2 ).setClipTransform( shift3 );
		
		//adjust surface threshold
		bvvSources.get( 2 ).setAlphaRange(0, 50);
	}
	
}
