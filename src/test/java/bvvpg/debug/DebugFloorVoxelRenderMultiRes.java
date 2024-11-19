package bvvpg.debug;

import java.util.List;

import net.imglib2.FinalRealInterval;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;

import bvvpg.vistools.Bvv;
import bvvpg.vistools.BvvFunctions;
import bvvpg.vistools.BvvSource;
import bvvpg.vistools.BvvStackSource;

import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.sequence.VoxelDimensions;

public class DebugFloorVoxelRenderMultiRes
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
		sources = BvvFunctions.show( spimData, Bvv.options().addTo( source ));
		final BvvSource source2 = sources.get(0);
		double [] minI = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(0).minAsDoubleArray();
		double [] maxI = spimData.getSequenceDescription().getImgLoader().getSetupImgLoader(0).getImage(0).maxAsDoubleArray();

		VoxelDimensions voxSize = spimData.getSequenceDescription().getViewSetupsOrdered().get( 0 ).getVoxelSize();
		double [][] clipRange = new double [2][3];
		
		for(int d=0;d<3;d++)
		{
			clipRange[0][d] = minI[d]*voxSize.dimension( d );
			clipRange[1][d] = maxI[d]*voxSize.dimension( d );
		}
		
		//clip axis
		int nClipAxis = 0;
		
		clipRange[0][nClipAxis]=0.5*clipRange[1][nClipAxis];
		source.setVoxelRenderInterpolation( 0 );
		source.setDisplayRangeBounds( 0, 40000 );
		source.setDisplayRange(0, 655);
		source.setDisplayGamma(0.5);
		source.setRenderType( 1 );
		source.setClipInterval( new FinalRealInterval(clipRange[0], clipRange[1])  );
		source.setLUT("Fire");
		
		clipRange[0][nClipAxis] = minI[nClipAxis]*voxSize.dimension( nClipAxis );
		clipRange[1][nClipAxis]*=0.5;
		source2.setDisplayRangeBounds( 0, 40000 );
		source2.setDisplayRange(0, 655);
		source2.setDisplayGamma(0.5);
		source2.setRenderType( 1 );
		source2.setClipInterval( new FinalRealInterval(clipRange[0], clipRange[1])  );
		source2.setLUT("Fire");

	}
}
