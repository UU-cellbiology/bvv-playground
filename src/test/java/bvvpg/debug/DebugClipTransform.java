package bvvpg.debug;

import java.util.List;

import net.imglib2.FinalRealInterval;
import net.imglib2.realtransform.AffineTransform3D;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bvvpg.vistools.BvvFunctions;
import bvvpg.vistools.BvvStackSource;
import bvvpg.source.converters.GammaConverterSetup;
import mpicbg.spim.data.SpimDataException;

public class DebugClipTransform
{

	public static void main( final String[] args )
	{
		final String xmlFilename = "/home/eugene/Desktop/projects/BVB/whitecube_2ch.xml";
		SpimDataMinimal spimData = null;
		try {
			spimData = new XmlIoSpimDataMinimal().load( xmlFilename );
		} catch (SpimDataException e) {
			e.printStackTrace();
		}		
		List< BvvStackSource< ? > > sources = BvvFunctions.show( spimData );
		
		sources.get( 0 ).setLUT( "Green" );
		sources.get( 1 ).setLUT( "Red" );
		
		double [][] mm = new double [2][3];
		for (int d=0;d<3;d++)
		{
			mm[1][d] = 100;
		}
		AffineTransform3D tr = new AffineTransform3D();
		double [] trans = new double [3];
		trans[0] = 50;
		tr.translate( trans);
		System.out.println( tr );
		sources.get( 0 ).setClipInterval( new FinalRealInterval(mm[0],mm[1]));
		sources.get( 0 ).setClipTransform( tr );
		((GammaConverterSetup)(sources.get( 0 ).getConverterSetups().get( 0 ))).getClipTransform(tr);
		System.out.println( tr );
	}
}
