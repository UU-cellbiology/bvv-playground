package bvvpg.debug;

import java.util.List;

import net.imglib2.FinalRealInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bvvpg.vistools.BvvFunctions;
import bvvpg.vistools.BvvSource;
import bvvpg.vistools.BvvStackSource;
import ij.IJ;
import ij.ImagePlus;
import mpicbg.spim.data.SpimDataException;

public class DebugFloorVoxelRender
{
	public static void main( final String[] args )
	{
		
		//regular tif init
		/**/
		//final ImagePlus imp = IJ.openImage( "https://imagej.nih.gov/ij/images/t1-head.zip" );
//		final ImagePlus imp = IJ.openImage( "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/t1-head.tif" );
//		final Img< UnsignedShortType > img = ImageJFunctions.wrapShort( imp );
//		final BvvSource source = BvvFunctions.show( img, "t1-head" );
		
		final String xmlFilename = "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/head_2ch.xml";
		SpimDataMinimal spimData = null;
		try {
			spimData = new XmlIoSpimDataMinimal().load( xmlFilename );
		} catch (SpimDataException e) {
			e.printStackTrace();
		}		
		final List< BvvStackSource< ? > > sources = BvvFunctions.show( spimData );
		final BvvSource source = sources.get(0);
		source.setDisplayRangeBounds( 0, 40000 );
		source.setDisplayRange(0, 655);
		source.setDisplayGamma(0.5);
		source.setRenderType( 1 );
		source.setLUT("Fire");
		//source.setVoxelRenderInterpolation( 1 );
		
		
		double [] minI = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(0).minAsDoubleArray();
		double [] maxI = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(0).maxAsDoubleArray();
//		double [] minI = img.minAsDoubleArray();
//		double [] maxI = img.maxAsDoubleArray();

		
		minI[2]=0.5*maxI[2];		
		source.setClipInterval(new FinalRealInterval(minI,maxI));		
	}
}
