package bvvpg.debug;

import java.util.List;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bvvpg.vistools.Bvv;
import bvvpg.vistools.BvvFunctions;
import bvvpg.vistools.BvvSource;
import bvvpg.vistools.BvvStackSource;
import mpicbg.spim.data.SpimDataException;

public class DebugVolumeNumber
{
	public static void main( final String[] args )
	{
		final String xmlFilename = "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/t1-head.xml";
		SpimDataMinimal spimData = null;
		try {
			spimData = new XmlIoSpimDataMinimal().load( xmlFilename );
		} catch (SpimDataException e) {
			e.printStackTrace();
		}		
		List< BvvStackSource< ? > > sources = BvvFunctions.show( spimData );
		final BvvSource source = sources.get(0);
		for(int i = 0; i < 9; i++)
		{
			BvvFunctions.show( spimData, Bvv.options().addTo( source ));
			System.out.println( "Volume " + Integer.toString( i+2 ));
		}
	}
}
