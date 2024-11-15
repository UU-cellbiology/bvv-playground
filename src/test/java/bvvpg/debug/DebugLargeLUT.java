package bvvpg.debug;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import bvvpg.vistools.BvvFunctions;
import bvvpg.vistools.BvvSource;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class DebugLargeLUT
{
	public static void main( final String[] args )
	{
		
		int nLUTN = 500;
		ArrayImg< UnsignedShortType, ShortArray > dirsInt = ArrayImgs.unsignedShorts(new long [] {nLUTN,1,1 });
		Cursor< UnsignedShortType > cursor = Views.flatIterable( dirsInt ).cursor();
		int i=0;
		while(cursor.hasNext())
		{
			cursor.fwd();
			cursor.get().set( i );
			i++;
		}
		new ImageJ();
		
		IntervalView< UnsignedShortType > imgLUT = Views.expandBorder( ( RandomAccessibleInterval< UnsignedShortType > ) dirsInt, new long [] {0,50,50 });
		ImageJFunctions.show( imgLUT );
		/**/
		//final ImagePlus imp = IJ.openImage( "https://imagej.nih.gov/ij/images/t1-head.zip" );
		//final ImagePlus imp = IJ.openImage( "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/t1-head.tif" );
		//final Img< UnsignedShortType > img = ImageJFunctions.wrapShort( imp );
		final BvvSource source = BvvFunctions.show( imgLUT, "LUTVIEW" );
	}
}
