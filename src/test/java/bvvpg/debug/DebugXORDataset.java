package bvvpg.debug;

import net.imglib2.Cursor;
import net.imglib2.FinalRealInterval;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.Views;

import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Actions;

import bvvpg.vistools.Bvv;
import bvvpg.vistools.BvvFunctions;
import bvvpg.vistools.BvvSource;
import ij.ImageJ;
import ij.plugin.LutLoader;
import ij.process.LUT;

public class DebugXORDataset
{
	public static void main( final String[] args )
	{
		int nBoxSize = 16;
		ArrayImg< UnsignedShortType, ShortArray > dirsInt = ArrayImgs.unsignedShorts(new long [] {nBoxSize,nBoxSize,nBoxSize });
		Cursor< UnsignedShortType > cursor = dirsInt.localizingCursor( dirsInt );
		long [] pos = new long [3];
		int value;
		int nMax = 0;
		while(cursor.hasNext())
		{
			cursor.fwd();
			cursor.localize( pos );
			value = (int)(( pos[0] ^ pos[1] ^ pos [2])& 0xFF );
			cursor.get().set( value);
			if(nMax<value)
			{
				nMax = value;
			}
		}
		new ImageJ();
		LUT xyz = LutLoader.openLut( "/home/eugene/Documents/Fiji.app/luts/Cyan Hot.lut" );
		
		final BvvSource source = BvvFunctions.show( dirsInt, "XOR_dataset", Bvv.options().ditherWidth( 1 ).renderHeight( 800 ).renderWidth( 800 ).dClipFar( 500 ).dClipNear( 500 ).dCam( 501 ));
		source.setVoxelRenderInterpolation( 1 );
		source.setRenderType( 0 );
		source.setDisplayRange( 0, nBoxSize);
		source.setClipInterval(new FinalRealInterval(new double [] {0.,0.,0.},new double []{16.,16.,16.}));
		source.setLUT( xyz.getColorModel(),"HOT" );
		
		final Actions actions = new Actions( new InputTriggerConfig() );
		actions.runnableAction(() -> {
			source.setVoxelRenderInterpolation(0);
			source.getBvvHandle().getViewerPanel().requestRepaint();
			source.getBvvHandle().getViewerPanel().showMessage( "trilinear" );
			},"trilinear", "D" );
		actions.runnableAction(() -> {
			source.setVoxelRenderInterpolation(1);
			source.getBvvHandle().getViewerPanel().requestRepaint();
			source.getBvvHandle().getViewerPanel().showMessage( "floor/nearest neighbor" );
			},"floor", "S" );
		actions.runnableAction(() -> {
			source.setVoxelRenderInterpolation(2);
			source.getBvvHandle().getViewerPanel().requestRepaint();
			source.getBvvHandle().getViewerPanel().showMessage( "smaller voxels" );
			},"boxes", "A" );
		actions.install( source.getBvvHandle().getKeybindings(), "spheres actions" );
	}
}
