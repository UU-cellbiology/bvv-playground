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

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bvv.tools.RealARGBColorGammaConverterSetup;
import bvv.util.BvvFunctions;
import bvv.util.BvvStackSource;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ByteProcessor;

import java.util.List;
import mpicbg.spim.data.SpimDataException;
import net.imglib2.FinalRealInterval;

public class Example05
{
	/**
	 * Show BDV multi-channel, -angle, etc datasets as cached multi-resolution stacks.
	 */
	public static void main( final String[] args ) throws SpimDataException
	{
		final String xmlFilename = "/home/eugene/Desktop/export.xml";
		//final String xmlFilename = "/home/eugene/Desktop/emma_test.xml";
		final SpimDataMinimal spimData = new XmlIoSpimDataMinimal().load( xmlFilename );

		final List< BvvStackSource< ? > > sources = BvvFunctions.show( spimData );
		RealARGBColorGammaConverterSetup conv1 = (RealARGBColorGammaConverterSetup) sources.get(0).getConverterSetups().get(0);
		//sources.get(0).setDisplayRange(104, 120);
		conv1.setRenderType(1);
		conv1.setLUT(getRGBLutTable("Fire"));
		double [] minI = new double [3];
		double [] maxI = new double [3];
		for (int i=0;i<3;i++)
		{
			minI[i]=50.0;
			maxI[i]=150.0;
		}
		conv1.setCropInterval(new FinalRealInterval(minI,maxI));
		
		RealARGBColorGammaConverterSetup conv2 = (RealARGBColorGammaConverterSetup) sources.get(1).getConverterSetups().get(0);
		conv2.setRenderType(1);
		//conv2.setCropInterval(new FinalRealInterval(minI,maxI));
		
	}
	
	static public float [][]  getRGBLutTable(String sLUTName)
	{
		int i,j;
	
		int [] onepix; 
		float [][] RGBLutTable = new float[256][3];
		ByteProcessor ish = new ByteProcessor(256,1);
		for ( i=0; i<256; i++)
			for (j=0; j<10; j++)
				ish.putPixel(i, j, i);
		ImagePlus ccc = new ImagePlus("test",ish);
		ccc.show();
		IJ.run(sLUTName);
		IJ.run("RGB Color");
		//ipLUT= (ColorProcessor) ccc.getProcessor();
		ccc.setSlice(1);
		for(i=0;i<256;i++)
		{
			
			onepix= ccc.getPixel(i, 0);
			//rgbtable[i]=ccc.getPixel(i, 1);
			//java.awt.Color.RGBtoHSB(onepix[0], onepix[1], onepix[2], hsbvals);
			RGBLutTable[i][0]=(float)(onepix[0]/255.0f);
			RGBLutTable[i][1]=(float)(onepix[1]/255.0f);
			RGBLutTable[i][2]=(float)(onepix[2]/255.0f);
		}

		ccc.changes=false;
		ccc.close();
		return RGBLutTable;
		
		/*
		float [][] RGBLutTable = new float[256][3];
		for(int i=0;i<256;i++)
			for(int j=0;j<3;j++)
			{
				RGBLutTable [i][j]=i/255.0f;
			}
		return RGBLutTable;*/
		//return;
	}
}
