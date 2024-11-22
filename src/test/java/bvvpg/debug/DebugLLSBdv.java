package bvvpg.debug;

import java.util.List;

import net.imglib2.realtransform.AffineTransform3D;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bdv.tools.transformation.TransformedSource;
import bdv.util.BdvFunctions;
import bdv.util.BdvStackSource;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.ViewerPanel;
import mpicbg.spim.data.SpimDataException;

public class DebugLLSBdv {
	public static void main( final String[] args )
	{
		
		final String xmlFilename = "/home/eugene/Desktop/projects/BigTrace/LLS_interp/LLS_interpolation.xml";
		SpimDataMinimal spimData = null;
		try {
			spimData = new XmlIoSpimDataMinimal().load( xmlFilename );
		} catch (SpimDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}				
		
		//make a deskew transform
		AffineTransform3D deskew = makeLLS7Transform();
		List< BdvStackSource< ? > > sources = BdvFunctions.show(spimData);		
		//final List< BvvStackSource< ? > > sources = BvvFunctions.show( spimData );
		//final BvvSource source = sources.get(0);
		BdvStackSource< ? > source = sources.get(0);

		 ViewerPanel panel = source.getBdvHandle().getViewerPanel();

		for ( SourceAndConverter< ? > sourceC : panel.state().getSources() )
		{
			(( TransformedSource< ? > ) sourceC.getSpimSource() ).setFixedTransform(deskew);
		}	
		
		source.setDisplayRange( 0, 400 );

		AffineTransform3D viewSideAT = source.getBdvHandle().getViewerPanel().state().getViewerTransform();
		viewSideAT.rotate(1, Math.PI/2 );
		viewSideAT.rotate(2, (-1.0)*Math.PI/2 );
		viewSideAT.translate( new double [] {-150.0,-100.0,450.0} );
		//rotate view so we see the stripes
		source.getBdvHandle().getViewerPanel().state().setViewerTransform( viewSideAT );

	}
	
	/** function generates new LLS7 transform (rotation/shear/z-scaling **/
	static AffineTransform3D makeLLS7Transform()
	{
		AffineTransform3D afDataTransform = new AffineTransform3D();
		AffineTransform3D tShear = new AffineTransform3D();
		AffineTransform3D tRotate = new AffineTransform3D();
			
		//rotate 30 degrees
		tRotate.rotate(0, (-1.0)*Math.PI/6.0);
		//shearing transform
		tShear.set(1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.7320508075688767, 0.0, 0.0, 0.0, 1.0, 0.0);
		//Z-step adjustment transform
		afDataTransform.set(1.0, 0.0, 0.0, 0.0, 
								0.0,1.0, 0.0, 0.0, 
								0.0, 0.0, 0.5, 0.0);
		
		afDataTransform = tShear.concatenate(afDataTransform);
		afDataTransform = tRotate.concatenate(afDataTransform);
		return afDataTransform;
	}
}
