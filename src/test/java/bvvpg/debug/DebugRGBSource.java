package bvvpg.debug;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.ARGBType;

import bvvpg.vistools.Bvv;
import bvvpg.vistools.BvvFunctions;
import bvvpg.vistools.BvvStackSource;
import ij.IJ;
import ij.ImagePlus;

public class DebugRGBSource
{
	/**
	 * Test rendering of RGB volume (shaders)
	 */
	public static void main( final String[] args )
	{
		final ImagePlus imp = IJ.openImage( "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/RGB/ExM_MT.tif" );
		final RandomAccessibleInterval< ARGBType > rgbRAI = ImageJFunctions.wrapRGBA( imp );
		final BvvStackSource< ? > sourceRGB = BvvFunctions.show( rgbRAI,
				"rgbVolume" );
	}
}
