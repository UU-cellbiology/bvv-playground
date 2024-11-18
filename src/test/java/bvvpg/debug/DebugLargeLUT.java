package bvvpg.debug;

import java.awt.image.IndexColorModel;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;


import bvvpg.vistools.BvvFunctions;
import bvvpg.vistools.BvvSource;
import ij.ImageJ;

public class DebugLargeLUT
{
	public static void main( final String[] args )
	{
		
		int nImageMaxRange = 1024;
		ArrayImg< UnsignedShortType, ShortArray > dirsInt = ArrayImgs.unsignedShorts(new long [] {nImageMaxRange,1,1 });
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
		//ImageJFunctions.show( imgLUT );
		/**/
		//final ImagePlus imp = IJ.openImage( "https://imagej.nih.gov/ij/images/t1-head.zip" );
		//final ImagePlus imp = IJ.openImage( "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/t1-head.tif" );
		//final Img< UnsignedShortType > img = ImageJFunctions.wrapShort( imp );
		final BvvSource source = BvvFunctions.show( imgLUT, "LUTVIEW" );
		//int nLUTMAX = nImageMaxRange;
		int nLUTMAX = 65535;
		source.setLUT(  getLargeTestICM(nLUTMAX), null );
		source.setDisplayRangeBounds( 0, nImageMaxRange -1);
		//source.setAlphaGammaRangeBounds( 0, 1 );
		//source.setAlphaRange( 0, 1 );
		source.setRenderType( 1 );
		source.setAlphaRangeBounds( -2, -1 );
	}
	
	public static IndexColorModel getLargeTestICM(int nTotLength)
	{
		
		final byte [][] colors = new byte [3][nTotLength];
		colors[0][0] = ( byte ) 0;
		colors[1][0] = ( byte )  0 ;
		colors[2][0] = ( byte ) 255 ;
		for(int i=1;i<nTotLength;i++)
		{
			int nStep = (int) Math.round( 255.0*i/(nTotLength));
			colors[0][i] = ( byte ) nStep ;
			colors[1][i] = ( byte )  (255-nStep ) ;
			colors[2][i] = ( byte ) 0 ;
		}
		colors[0][nTotLength-1] = ( byte ) 255;
		colors[1][nTotLength-1] = ( byte )  255 ;
		colors[2][nTotLength-1] = ( byte ) 255 ;
		return new IndexColorModel(8,nTotLength,colors[0],colors[1],colors[2]);
	}
}
