package bvv.vistools.examples;

import java.util.List;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import btbvv.vistools.BvvFunctions;
import btbvv.vistools.BvvStackSource;
import mpicbg.spim.data.SpimDataException;

public class BT_RenderFrame
{
	public static void main( final String[] args )
	{
		final String xmlFilename = "/home/eugene/Desktop/bend/lxml/trace172232825.xml";
		SpimDataMinimal spimData = null;
		try {
			spimData = new XmlIoSpimDataMinimal().load( xmlFilename );
		} catch (SpimDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}	
		final List< BvvStackSource< ? > > sources = BvvFunctions.show( spimData );
		while(true)
		{
			try
			{
				Thread.sleep( 10 );
				System.out.println( sources.get( 0 ).getBvvHandle().getViewerPanel().getRepaintStatus());
			}
			catch ( InterruptedException exc )
			{
				// TODO Auto-generated catch block
				exc.printStackTrace();
			}
			
		}
	}
}
