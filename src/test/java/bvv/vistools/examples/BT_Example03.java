package bvv.vistools.examples;


import java.util.List;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bdv.tools.transformation.TransformedSource;
import bdv.viewer.SourceAndConverter;
import btbvv.core.VolumeViewerPanel;
import btbvv.vistools.BvvFunctions;
import btbvv.vistools.BvvSource;
import btbvv.vistools.BvvStackSource;
import mpicbg.spim.data.SpimDataException;
import net.imglib2.FinalInterval;
import net.imglib2.FinalRealInterval;
import net.imglib2.Interval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;


public class BT_Example03 {
	public static void main( final String[] args )
	{
		
		final String xmlFilename = "/home/eugene/Desktop/BigTrace_data/BioFormats/small_crop.xml";
		SpimDataMinimal spimData = null;
		try {
			spimData = new XmlIoSpimDataMinimal().load( xmlFilename );
		} catch (SpimDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		//create a deskew transform
		
		AffineTransform3D deskew = new AffineTransform3D();
		//deskew.translate(100.,100,100);
		makeLLS7Transform(spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(0), deskew);
		
		final List< BvvStackSource< ? > > sources = BvvFunctions.show( spimData );
		final BvvSource source = sources.get(0);
		final BvvSource source2 = sources.get(1);
		VolumeViewerPanel panel = source.getBvvHandle().getViewerPanel();
		for ( SourceAndConverter< ? > sourceC : panel.state().getSources() )
		{
			(( TransformedSource< ? > ) sourceC.getSpimSource() ).setFixedTransform(deskew);
		}
		
		long [] minI = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(0).minAsLongArray();
		long [] maxI = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(0).maxAsLongArray();
	
		
		source.setColor(new ARGBType(ARGBType.rgba(255, 0.0, 0.0, 255.0)));
		source.setDisplayRange(0, 200);
		source2.setColor(new ARGBType(ARGBType.rgba(0.0,255.0, 0.0, 255.0)));
		source2.setDisplayRange(0, 200);

		source.setClipTransform(deskew);
		
		
		//clip half of the volume along Z axis in the shaders
		//clipInterval is defined inside the "raw", non-transformed data interval	
		FinalRealInterval finRealAfter = deskew.estimateBounds(new FinalInterval(minI,maxI));
		double [] newMin =  finRealAfter.minAsDoubleArray();
		double [] newMax =  finRealAfter.maxAsDoubleArray();
		int nAxes = 1;
		newMin[nAxes]=newMin[nAxes]+0.5*(newMax[nAxes]-newMin[nAxes]);		
		source.setClipInterval(new FinalRealInterval(newMin,newMax));
		
		
	}
	
	/** function assigns new LLS7 transform to bt.afDataTransform (using provided voxel size of original data) 
	 * and returns the new interval of transformed source **/
	static FinalInterval makeLLS7Transform(final Interval orig_rai, final AffineTransform3D out)
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
								0.0, 1.0, 0.0, 0.0, 
								0.0, 0.0, 2.068965517, 0.0);
		
		afDataTransform = tShear.concatenate(afDataTransform);
		afDataTransform = tRotate.concatenate(afDataTransform);
		FinalRealInterval finReal = afDataTransform.estimateBounds(orig_rai);
		double [][] dBounds = new double [2][3]; 
		long [][] lBounds = new long [2][3]; 
		finReal.realMin(dBounds[0]);
		AffineTransform3D tZeroMin = new AffineTransform3D();
		for (int i = 0;i<3;i++)
		{
			dBounds[0][i] = dBounds[0][i]*(-1);
		}			
		tZeroMin.translate(dBounds[0]);
		afDataTransform = afDataTransform.preConcatenate(tZeroMin);
		finReal = afDataTransform.estimateBounds(orig_rai);
		finReal.realMin(dBounds[0]);
		finReal.realMax(dBounds[1]);
		for (int i = 0;i<3;i++)
			{
				lBounds[0][i] = (long) Math.floor(dBounds[0][i]);
				lBounds[1][i] = (long) Math.ceil(dBounds[1][i]);
			}
		
		out.concatenate(afDataTransform);
		return new FinalInterval(lBounds[0],lBounds[1]);
	}
}
