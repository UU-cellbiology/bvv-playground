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


import net.imglib2.FinalInterval;
import net.imglib2.Point;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.algorithm.region.hypersphere.HyperSphereCursor;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import bvvpg.vistools.Bvv;
import bvvpg.vistools.BvvFunctions;
import bvvpg.vistools.BvvSource;

public class PG_Example02
{
	/** Show difference in the source (volume) interpolation **/

	public static void main( final String[] args )
	{
		int nRadius = 35;
	
		//Let's make a hyperSphere (3D ball) with random intensity values 
		long [] dim = new long[] {2*nRadius+2,2*nRadius+2,2*nRadius+2};
		Point center = new Point( 3 );
		center.setPosition( nRadius+1 , 0 );
		center.setPosition( nRadius+1 , 1 );
		center.setPosition( nRadius+1 , 2 );
		
		ArrayImg< UnsignedShortType, ShortArray > sphereRai = ArrayImgs.unsignedShorts(dim);
		HyperSphere< UnsignedShortType > hyperSphere =
				new HyperSphere<>( sphereRai, center, nRadius);			
		
		HyperSphereCursor< UnsignedShortType > cursor = hyperSphere.localizingCursor();
		
		while ( cursor.hasNext() )
		{
			cursor.fwd();
			cursor.get().setInteger( Math.round(Math.random()*255.0) );
		}
		
		//regular interpolation (left half)
		final BvvSource source = BvvFunctions.show( sphereRai, "sphere_left" );
		source.setClipInterval( new FinalInterval(new long[] {0,0,0}, new long[] {nRadius,2*nRadius+2,2*nRadius+2}) );
		source.setDisplayRangeBounds( 0, 255 );
		source.setRenderType( 1 );
		source.setAlphaRangeBounds( 0, 255 );
		source.setLUT( "Spectrum" );
		source.setAlphaRangeBounds( 0, 1 );
		
		//no interpolation (right half)
		final BvvSource source2 = BvvFunctions.show( sphereRai, "sphere_right" ,Bvv.options().addTo( source ));
		source2.setClipInterval( new FinalInterval(new long[] {nRadius,0,0}, new long[] {2*nRadius+2,2*nRadius+2,2*nRadius+2}) );
		source2.setDisplayRangeBounds( 0, 255 );
		source2.setRenderType( 1 );
		source2.setAlphaRangeBounds( 0, 255 );
		
		//set source as no interpolation
		source2.setVoxelRenderInterpolation( 0 );
		source2.setLUT( "Spectrum" );
		source2.setAlphaRangeBounds( 0, 1 );
	}
}
