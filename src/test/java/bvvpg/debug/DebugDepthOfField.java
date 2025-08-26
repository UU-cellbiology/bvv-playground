package bvvpg.debug;

import java.util.List;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bvvpg.vistools.BvvFunctions;
import bvvpg.vistools.BvvSource;
import bvvpg.vistools.BvvStackSource;
import mpicbg.spim.data.SpimDataException;

public class DebugDepthOfField
{
	public static void main( final String[] args )
	{
		
		//regular tif init
		/**/

		final String xmlFilename = "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/t1-head.xml";
		SpimDataMinimal spimData = null;
		try {
			spimData = new XmlIoSpimDataMinimal().load( xmlFilename );
		} catch (SpimDataException e) {
			e.printStackTrace();
		}		
		List< BvvStackSource< ? > > sources = BvvFunctions.show( spimData );
		final BvvSource source = sources.get(0);
		source.setDisplayRangeBounds( 0, 500 );
		source.setDisplayRange(0, 355);
		source.setDisplayGamma(0.5);
		source.setRenderType( 1 );
	}
}
