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

import java.util.List;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;

import bvvpg.vistools.Bvv;
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

import net.imglib2.type.numeric.integer.UnsignedShortType;



public class PG_Example01 {
	
	/**
	 * Show 16-bit volume, change display range, change gamma,
	 * rendering type, alpha value, apply LUT and clip volume in half
	 */
	public static void main( final String[] args )
	{
		
		//regular tif init
		/**/
		//final ImagePlus imp = IJ.openImage( "https://imagej.nih.gov/ij/images/t1-head.zip" );
		final ImagePlus imp = IJ.openImage( "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/t1-head.tif" );
		final Img< UnsignedShortType > img = ImageJFunctions.wrapShort( imp );
		final BvvSource source = BvvFunctions.show( img, "t1-head" );

		double [] minI = img.minAsDoubleArray();
		double [] maxI = img.maxAsDoubleArray();
		/**/

		//BDV XML init (multiscale cached)
		/*
		final String xmlFilename = "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/t1-head.xml";
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
		VoxelDimensions voxSize = spimData.getSequenceDescription().getViewSetupsOrdered().get( 0 ).getVoxelSize();
		for(int d=0; d<3; d++)
		{
			minI[d] *= voxSize.dimension( d );
			maxI[d] *= voxSize.dimension( d );

		}
		*/
	
		source.setDisplayRangeBounds( 0, 40000 );
		source.setDisplayRange(0, 655);
		source.setDisplayGamma(0.5);
		
		
		//set volumetric rendering (1), instead of max intensity max intensity (0)
		source.setRenderType(1);
		
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

		
		//clip half of the volume along Z axis in the shaders
		//clipInterval is defined inside the "raw", non-transformed data interval		
		minI[2]=0.5*maxI[2];		
		source.setClipInterval(new FinalRealInterval(minI,maxI));		
		
	}
	
}
