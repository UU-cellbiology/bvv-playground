package bvv.vistools.examples;


import java.util.List;

import javax.swing.UIManager;

import org.fusesource.jansi.Ansi.Color;

import com.formdev.flatlaf.FlatIntelliJLaf;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import btbvv.vistools.BvvFunctions;
import btbvv.vistools.BvvSource;
import btbvv.vistools.BvvStackSource;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import mpicbg.spim.data.SpimDataException;
import net.imglib2.FinalRealInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedShortType;


public class BT_Example01 {
	
	/**
	 * Show 16-bit volume, change rendering type and gamma
	 */
	public static void main( final String[] args )
	{
		
		//regular tif init
		/**/
		final ImagePlus imp = IJ.openImage( "https://imagej.nih.gov/ij/images/t1-head.zip" );
		//final ImagePlus imp = IJ.openImage( "/home/eugene/Desktop/head/t1-head_2sources.tif" );
		final Img< UnsignedShortType > img = ImageJFunctions.wrapShort( imp );
		final BvvSource source = BvvFunctions.show( img, "t1-head" );
	
		double [] minI = img.minAsDoubleArray();
		double [] maxI = img.maxAsDoubleArray();
		/**/

		//BDV XML init
		/*
		final String xmlFilename = "/home/eugene/Desktop/head/export.xml";
		SpimDataMinimal spimData = null;
		try {
			spimData = new XmlIoSpimDataMinimal().load( xmlFilename );
		} catch (SpimDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		final List< BvvStackSource< ? > > sources = BvvFunctions.show( spimData );
		final BvvSource source = sources.get(0);
		double [] minI = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(0).minAsDoubleArray();
		double [] maxI = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(0).maxAsDoubleArray();
		*/
	
		
		source.setDisplayRange(0, 655);
		source.setDisplayGamma(0.5);
		
		
		//set volumetric rendering (1), instead of max intensity max intensity (0)
		source.setRenderType(1);
		
		//DisplayRange maps colors (or LUT values) to intensity values
		source.setDisplayRange(0, 400);
		//it is also possible to change LUT gamma value
		//source.setDisplayGamma(0.9);
		
		//alpha channel to intensity mapping can be changed independently
		source.setAlphaRange(0, 500);
		//it is also possible to change alpha-channel gamma value
		//source.setAlphaGamma(0.9);
		
		//assign a "Fire" lookup table to this source
		//(input: float [256][3], the last index (color component) changes from 0 to 1)
		source.setLUT(getRGBLutTable("Fire"));
		
		//crop half of the volume along Z axis in the shaders
		//cropInterval is defined inside the "raw", non-transformed data interval		
		minI[2]=0.5*maxI[2];		
		source.setCropInterval(new FinalRealInterval(minI,maxI));
		
		
	}
	
	//a helper function to get LUT array from ImageJ
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
		
	}
}
