package bvvpg.debug;

import java.util.ArrayList;

import bdv.tools.transformation.TransformedSource;
import bdv.viewer.SourceAndConverter;
import bvvpg.core.VolumeViewerPanel;
import bvvpg.vistools.Bvv;
import bvvpg.vistools.BvvFunctions;
import bvvpg.vistools.BvvSource;

import ij.IJ;
import ij.ImagePlus;

import net.imglib2.FinalInterval;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.interpolation.Interpolant;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.interpolation.stack.LinearRealRandomAccessibleStackInterpolatorFactory;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class DebugLLSRAI 
{
	public static void main( final String[] args )
	{
		
		final ImagePlus imp = IJ.openImage( "/home/eugene/Desktop/projects/BigTrace/LLS_interp/0.6.tif" );
		
		final Img< UnsignedShortType > img3d = ImageJFunctions.wrapShort( imp );
		
		final BvvSource source = BvvFunctions.show( img3d, "LLS" );
		
		AffineTransform3D deskew = makeLLS7Transform();
		VolumeViewerPanel panel = source.getBvvHandle().getViewerPanel();

		for ( SourceAndConverter< ? > sourceC : panel.state().getSources() )
		{
			(( TransformedSource< ? > ) sourceC.getSpimSource() ).setFixedTransform(deskew);
		}	
		
		source.setDisplayRange( 0, 400 );
		
		//rotate view so we see the stripes
		AffineTransform3D viewSideAT = source.getBvvHandle().getViewerPanel().state().getViewerTransform();
		viewSideAT.rotate(1, Math.PI/2 );
		viewSideAT.rotate(2, (-1.0)*Math.PI/2 );
		viewSideAT.translate( new double [] {-200.0,100.0,0.0} );
		source.getBvvHandle().getViewerPanel().state().setViewerTransform( viewSideAT );

		
		//calculate per slice shift and then interpolation
				
		final ArrayList<RealRandomAccessible<UnsignedShortType>> transformedRealSlices = new ArrayList<>();

		InterpolatorFactory<UnsignedShortType, RandomAccessible< UnsignedShortType >> interpolatorFactory = new NearestNeighborInterpolatorFactory<>();
		AffineTransform2D shiftXY = new AffineTransform2D();
		
		for(int i=0;i<img3d.dimension( 2 );i++)
		{			
			final RandomAccessibleInterval<UnsignedShortType> img = Views.hyperSlice( img3d,2,i);
			final ExtendedRandomAccessibleInterval<UnsignedShortType, RandomAccessibleInterval<UnsignedShortType>> extended = Views.extendZero(img);
			final RealRandomAccessible<UnsignedShortType> interpolant = Views.interpolate(extended, interpolatorFactory);
			AffineTransform2D out = new AffineTransform2D ();
			out.set( shiftXY );
			transformedRealSlices.add(RealViews.affineReal(interpolant, out));
			shiftXY.translate(0.0, Math.cos( Math.PI/6.0 )*0.6/0.145 );
		}	
		
		final Interpolant<UnsignedShortType, ArrayList<RealRandomAccessible<UnsignedShortType>>> interpolatedStack = new Interpolant<>(
				transformedRealSlices,
						new LinearRealRandomAccessibleStackInterpolatorFactory<>(),
				3);
		
		long [][] lBounds = new long [2][3];
		lBounds[0] = img3d.minAsLongArray();
		lBounds[1] = img3d.maxAsLongArray();
		lBounds[1][1] += Math.round(img3d.dimension( 2 )*(Math.cos( Math.PI/6.0 )*0.6/0.145));


		IntervalView< UnsignedShortType > outRAI = Views.interval( Views.raster( interpolatedStack ), new FinalInterval(lBounds[0],lBounds[1]) );
	
		final BvvSource source2 = BvvFunctions.show( outRAI, "LLS_interp", Bvv.options().sourceTransform( 1.0,1.0, 0.5*0.6/0.145));
	
		source2.setDisplayRange( 0, 400 );
	
		//rotate view so we see the stripes
		AffineTransform3D viewSideAT2 = source2.getBvvHandle().getViewerPanel().state().getViewerTransform();
		viewSideAT2.rotate(1, Math.PI/2.0 );
		viewSideAT2.rotate(2, (-1.0)*Math.PI/3.0 );
		viewSideAT2.translate( -10.0,60.0,0.0 );
		viewSideAT2.scale( 1.65 );
		source2.getBvvHandle().getViewerPanel().state().setViewerTransform( viewSideAT2 );
		
		
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
								0.0, 0.0, 0.5*0.6/0.145, 0.0);
		
		afDataTransform = tShear.concatenate(afDataTransform);
		afDataTransform = tRotate.concatenate(afDataTransform);
		return afDataTransform;
	}
}