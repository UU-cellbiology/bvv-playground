package bvvpg.debug;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import net.imglib2.FinalRealInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.Views;

import org.fusesource.jansi.Ansi.Color;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bdv.tools.transformation.TransformedSource;
import bdv.util.BoundedValueDouble;
import bdv.viewer.SourceAndConverter;
import bvvpg.core.VolumeViewerPanel;
import bvvpg.ui.panels.BoundedValuePanelPG;
import bvvpg.vistools.Bvv;
import bvvpg.vistools.BvvFunctions;
import bvvpg.vistools.BvvSource;
import bvvpg.vistools.BvvStackSource;
import ij.IJ;
import ij.ImagePlus;
import mpicbg.spim.data.SpimDataException;

public class DebugClipInterval
{
	public static void main( final String[] args )
	{
		
		//regular tif init
		/**/

		final ImagePlus imp = IJ.openImage( "/home/eugene/Desktop/projects/BVB/cliptest.tif" );
		final Img< UnsignedShortType > img = ImageJFunctions.wrapShort( imp );
		ArrayList< BvvStackSource< ? >> sources =  new ArrayList<>();
		
		AffineTransform3D scaleAF = new AffineTransform3D();
		scaleAF.scale( 3.0 );
		
		sources.add( BvvFunctions.show( Views.hyperSlice(img,2,0), "ch1",  Bvv.options().sourceTransform( scaleAF ) ));
		sources.add( BvvFunctions.show( Views.hyperSlice(img,2,1), "ch2", Bvv.options().sourceTransform( scaleAF ).addTo( sources.get( 0 ).getBvvHandle() ) ));
		
		
		
//		final String xmlFilename = "/home/eugene/Desktop/projects/BVB/cliptest.xml";
//		SpimDataMinimal spimData = null;
//		try {
//			spimData = new XmlIoSpimDataMinimal().load( xmlFilename );
//		} catch (SpimDataException e) {
//			e.printStackTrace();
//		}		
//		List< BvvStackSource< ? > > sources = BvvFunctions.show( spimData );
		
		
		
		sources.get( 0 ).setLUT( "Green" );
		sources.get( 1 ).setLUT( "Red" );
		sources.get( 0 ).setVoxelRenderInterpolation( 0 );
		sources.get( 1 ).setVoxelRenderInterpolation( 0 );
		double [] min = new double[3];
		double [] max = new double[3];
		//min[0]=0.1;
		for (int d=0;d<3;d++)
		{
			max[d]= 100;
		}
		
		sources.get( 1 ).setClipInterval( new FinalRealInterval(min,max) );
		final VolumeViewerPanel panel = sources.get(0).getBvvHandle().getViewerPanel();
		
		JFrame frame = new JFrame("clip bound");
		frame.setAlwaysOnTop( true );
		frame.getContentPane().setPreferredSize( new Dimension(450,40) );
		BoundedValueDouble model = new BoundedValueDouble( -1.0, 20.0, 0. );
		BoundedValuePanelPG clipXPanel = new BoundedValuePanelPG(model);
		clipXPanel.setConsistent( true );
		
		clipXPanel.changeListeners().add(new BoundedValuePanelPG.ChangeListener()
		{

			@Override
			public void boundedValueChanged(  )
			{
				min[0]= clipXPanel.getValue().getCurrentValue();
				sources.get( 1 ).setClipInterval( new FinalRealInterval(min,max) );
				panel.requestRepaint();
			}});

		frame.getContentPane().add(clipXPanel);
		frame.pack();
		frame.setVisible(true);
	}
}
