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
		bvvSources16bit.get( 0 ).setVoxelRenderInterpolation( 0 );
		bvvSources8bit.get( 0 ).setVoxelRenderInterpolation( 0 );
	}
	

}