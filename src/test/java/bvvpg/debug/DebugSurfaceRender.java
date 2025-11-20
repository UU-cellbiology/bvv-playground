package bvvpg.debug;

import net.imglib2.FinalRealInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import bvvpg.vistools.BvvFunctions;
import bvvpg.vistools.BvvSource;
import ij.IJ;
import ij.ImagePlus;

public class DebugSurfaceRender
{
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
		
		//scale clipping interval
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
		source.setRenderType(2);
		
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

		

	}
}
