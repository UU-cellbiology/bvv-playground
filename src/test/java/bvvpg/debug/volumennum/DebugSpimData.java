/*-
 * #%L
 * Volume rendering of bdv datasets with gamma and transparency option
 * %%
 * Copyright (C) 2022 - 2026 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
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
package bvvpg.debug.volumennum;


import java.awt.image.IndexColorModel;
import java.util.List;
import java.util.Random;

import net.imglib2.realtransform.AffineTransform3D;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bvvpg.vistools.Bvv;
import bvvpg.vistools.BvvFunctions;
import bvvpg.vistools.BvvOptions;
import bvvpg.vistools.BvvStackSource;
import mpicbg.spim.data.SpimDataException;

public class DebugSpimData
{
	//test true multi res images of different types
	public static void main( final String[] args )
	{
		final String xmlFilename8bit = "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/t1-head_8bit.xml";
		final String xmlFilename16bit = "/home/eugene/Desktop/projects/BVB/points/mastodon/datasethdf5.xml";
		//final String xmlFilename16bit = "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/t1-head_shifted.xml";

		SpimDataMinimal spimData8bit = null;
		SpimDataMinimal spimData16bit = null;

		try {
			spimData8bit = new XmlIoSpimDataMinimal().load( xmlFilename8bit );
			spimData16bit = new XmlIoSpimDataMinimal().load( xmlFilename16bit );
		} catch (SpimDataException e) {
			e.printStackTrace();
		}
		final Bvv bvv = BvvFunctions.show(BvvOptions.options().frameTitle( "Test spimdata" ));

//		List< BvvStackSource< ? > > bvvSources16bit = BvvFunctions.show( spimData16bit, BvvOptions.options().addTo( bvv ) );
//		bvvSources16bit.get( 0 ).setDisplayRange( 0, 700 );
		List< BvvStackSource< ? > > bvvSources16bit = BvvFunctions.show( spimData16bit, BvvOptions.options().addTo( bvv ) );
		bvvSources16bit.get( 0 ).setDisplayRange( 0, 1255 );
		List< BvvStackSource< ? > > bvvSources8bit = BvvFunctions.show( spimData8bit, BvvOptions.options().addTo( bvv ) );
		bvvSources8bit.get( 0 ).setDisplayRange( 0, 255 );
		//bvvSources16bit.get( 0 ).setDisplayRange( 0, 1255 );
		//bvvSources16bit.get( 0 ).setLUT( "Fire" );
		bvvSources16bit.get( 0 ).setVoxelRenderInterpolation( 1 );
		bvvSources8bit.get( 0 ).setVoxelRenderInterpolation( 1 );
	}
	

}
