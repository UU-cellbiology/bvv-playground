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

import ij.IJ;
import ij.ImagePlus;
import net.imglib2.FinalInterval;
import net.imglib2.FinalRealInterval;
import net.imglib2.Interval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import bvvpg.vistools.Bvv;
import bvvpg.vistools.BvvFunctions;
import bvvpg.vistools.BvvSource;

public class PG_Example03 {
	public static void main( final String[] args )
	{
		
		//regular tif init
		/**/
		//final ImagePlus imp = IJ.openImage( "https://imagej.nih.gov/ij/images/t1-head.zip" );
		final ImagePlus imp = IJ.openImage( "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/BioFormats/small_crop.tif" );
		final Img< UnsignedShortType > img = ImageJFunctions.wrapShort( imp );

	
		long [] minI = img.minAsLongArray();
		long [] maxI = img.maxAsLongArray();

		//create a deskew transform
		
		AffineTransform3D deskew = new AffineTransform3D();
		//deskew.translate(100.,100,100);
		makeLLS7Transform(img, deskew);
		
		final BvvSource source = BvvFunctions.show( img, "t1-head", Bvv.options().sourceTransform(deskew));
		final BvvSource source2 = BvvFunctions.show( img, "t1-head", Bvv.options().sourceTransform(deskew).addTo(source));

		source.setColor(new ARGBType(ARGBType.rgba(255, 0.0, 0.0, 255.0)));
		source.setDisplayRange(0, 2100);
		source2.setColor(new ARGBType(ARGBType.rgba(0.0, 255.0, 0.0, 255.0)));
		source2.setDisplayRange(0, 2100);
		
		
		int nAxes = 1;
		
		//clip source 1 in 'data' coordinates		
		double [] newMin2 =  img.minAsDoubleArray();
		double [] newMax2 =  img.maxAsDoubleArray();
		newMin2[nAxes] = newMin2[nAxes]+0.5*(newMax2[nAxes]-newMin2[nAxes]);
		source.setClipInterval(new FinalRealInterval(newMin2,newMax2));

		//clip source 2 in 'clip' coordinates		
		source2.setClipTransform(deskew);
			
		//clip half of the volume along Z axis in the shaders
		//clipInterval is defined inside the "raw", non-transformed data interval	
		FinalRealInterval finRealAfter = deskew.estimateBounds(new FinalInterval(minI,maxI));
		double [] newMin =  finRealAfter.minAsDoubleArray();
		double [] newMax =  finRealAfter.maxAsDoubleArray();
	
		newMin[nAxes] = newMin[nAxes]+0.5*(newMax[nAxes]-newMin[nAxes]);		
		source2.setClipInterval(new FinalRealInterval(newMin,newMax));
		

	}
	
	/** function assigns new LLS7 transform to bt.afDataTransform (using provided voxel size of original data) 
	 * and returns the new interval of transformed source **/
	static FinalInterval makeLLS7Transform(final Interval orig_rai, final AffineTransform3D out)
	{
		AffineTransform3D afDataTransform = new AffineTransform3D();
		AffineTransform3D tShear = new AffineTransform3D();
		AffineTransform3D tRotate = new AffineTransform3D();
	
		
		//rotate 30 degrees
		tRotate.rotate(0, (-1.0)*Math.PI/6.0);
		//shearing transform
		tShear.set(1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.7320508075688767, 0.0, 0.0, 0.0, 1.0, 0.0);
		//Z-step adjustment transform
		afDataTransform.set(1.0, 0.0, 0.0, 0.0, 
								0.0, 1.0, 0.0, 0.0, 
								0.0, 0.0, 2.068965517, 0.0);
		
		afDataTransform = tShear.concatenate(afDataTransform);
		afDataTransform = tRotate.concatenate(afDataTransform);
		FinalRealInterval finReal = afDataTransform.estimateBounds(orig_rai);
		double [][] dBounds = new double [2][3]; 
		long [][] lBounds = new long [2][3]; 
		finReal.realMin(dBounds[0]);
		AffineTransform3D tZeroMin = new AffineTransform3D();
		for (int i = 0;i<3;i++)
		{
			dBounds[0][i] = dBounds[0][i]*(-1);
		}			
		tZeroMin.translate(dBounds[0]);
		afDataTransform = afDataTransform.preConcatenate(tZeroMin);
		finReal = afDataTransform.estimateBounds(orig_rai);
		finReal.realMin(dBounds[0]);
		finReal.realMax(dBounds[1]);
		for (int i = 0;i<3;i++)
			{
				lBounds[0][i] = (long) Math.floor(dBounds[0][i]);
				lBounds[1][i] = (long) Math.ceil(dBounds[1][i]);
			}
		
		out.concatenate(afDataTransform);
		return new FinalInterval(lBounds[0],lBounds[1]);
	}
}
