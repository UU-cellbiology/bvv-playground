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
package bvvpg.debug.luts;

import java.awt.image.IndexColorModel;
import java.util.List;
import java.util.Random;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bvvpg.vistools.Bvv;
import bvvpg.vistools.BvvFunctions;
import bvvpg.vistools.BvvOptions;
import bvvpg.vistools.BvvStackSource;
import mpicbg.spim.data.SpimDataException;

public class DebugLUTUploadSpeed
{
	public static void main( final String[] args )
	{
		final String xmlFilename = "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/t1-head_8bit.xml";
		//final String xmlFilename = "/home/eugene/Desktop/projects/BVB/points/mastodon/datasethdf5.xml";

		
		SpimDataMinimal spimData = null;


		try {
			spimData = new XmlIoSpimDataMinimal().load( xmlFilename );
		} catch (SpimDataException e) {
			e.printStackTrace();
		}
		final Bvv bvv = BvvFunctions.show(BvvOptions.options().frameTitle( "Test LUT upload speed" ));
		List< BvvStackSource< ? > > bvvSources8bit = BvvFunctions.show( spimData, BvvOptions.options().addTo( bvv ) );
		BvvStackSource< ? > src = bvvSources8bit.get( 0 );
		src.setDisplayRange( 0, 255 );
		src.setRenderType( 1 );
		while(true)
		{
			src.setLUT(  getRandomICM(32), "test" );
			try
			{
				Thread.sleep( 1 );
			}
			catch ( InterruptedException exc )
			{
				// TODO Auto-generated catch block
				exc.printStackTrace();
			}
		}
	}
	
	public static IndexColorModel getRandomICM(int nTotLength)
	{
		final Random random = new Random();
		final byte [][] colors = new byte [3][nTotLength];
		colors[0][0] = ( byte ) 0;
		colors[1][0] = ( byte )  0 ;
		colors[2][0] = ( byte ) 0 ;
		
		for(int i = 1; i < nTotLength; i++)
		{
			int r = random.nextInt(255);
			//int g = random.nextInt(255);
			//int b = random.nextInt(255);
			colors[0][i] = ( byte ) r;
			colors[1][i] = ( byte ) r;
			colors[2][i] = ( byte ) r ;
		}
		
		return new IndexColorModel(16,nTotLength,colors[0],colors[1],colors[2]);
	}
}
