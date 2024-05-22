package btbvv.core.render;

import java.awt.image.IndexColorModel;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Intervals;

import btbvv.btuitools.LutCSTextureBT;
import btbvv.btuitools.GammaConverterSetup;
import btbvv.core.backend.GpuContext;
import btbvv.core.backend.Texture3D;



public class SimpleLUTTextureManager
{
	private final HashMap< GammaConverterSetup, LutCSTextureBT > texturesLUT;
	private final HashMap< LutCSTextureBT, Integer > timestamps;
	private int currentTimestamp;
	
	public SimpleLUTTextureManager()
	{
		texturesLUT = new HashMap<>();
		timestamps = new HashMap<>();
		currentTimestamp = 0;
	}
	
	public synchronized void processTextureLUT( final GpuContext context, GammaConverterSetup gc )
	{
		final LutCSTextureBT texture;
		if(gc.updateNeededLUT())
		{
			final LutCSTextureBT old_texture = texturesLUT.get( gc );
			context.delete(old_texture);
			timestamps.remove( old_texture );
			texturesLUT.remove( gc );			
		}
		texture = texturesLUT.computeIfAbsent( gc, s -> uploadLUTToTexture( context, gc.getLutICM()) );
		gc.setLUTTexture( texture );
		timestamps.put( texture, currentTimestamp );
	}
	
	/**
	 * Free allocated resources associated to all LUTS that have not been
	 * {@link #processTextureLUT(GpuContext,GammaConverterSetup) requested} since the
	 * last call to {@link #freeUnusedLUTs(GpuContext)}.
	 */
	public synchronized void freeUnusedLUTs( final GpuContext context )
	{
		final Iterator< Map.Entry< LutCSTextureBT, Integer > > it = timestamps.entrySet().iterator();


		texturesLUT.entrySet().removeIf( entry -> timestamps.get( entry.getValue() ) < currentTimestamp );
		while ( it.hasNext() )
		{
			final Map.Entry< LutCSTextureBT, Integer > entry = it.next();
			if ( entry.getValue() < currentTimestamp )
			{
				context.delete( entry.getKey() );
				it.remove();
			}
		}
		++currentTimestamp;
	}
	
	public void freeLUTs( final GpuContext context )
	{

		texturesLUT.clear();
		timestamps.keySet().forEach( context::delete );
		timestamps.clear();
	}
	
	private static LutCSTextureBT uploadLUTToTexture( final GpuContext context, final IndexColorModel icm )
	{
		final LutCSTextureBT texture = new LutCSTextureBT();
		final ByteBuffer data;
		if(icm != null)
		{
			int size_ = icm.getMapSize();
			texture.init( size_ );
			final int numBytes = 4 *  size_;
			data = ByteBuffer.allocateDirect( numBytes ); // allocate a bit more than needed...
			data.order( ByteOrder.nativeOrder() );	
			final IntBuffer sdata = data.asIntBuffer();
			byte [][] colors = new byte[3][size_];
			icm.getReds(colors[0]);
			icm.getGreens(colors[1]);
			icm.getBlues(colors[2]);
			for (int i=0; i<size_;i++)
			{
				final int r = colors[0][i] & 0xff;
				final int g = colors[1][i] & 0xff;
				final int b = colors[2][i] & 0xff;
				final int all = ( 255 << 24 ) | ( b << 16 ) | ( g << 8 ) | r;
				sdata.put( i,all );
			}
		}
		//upload dummy
		else
		{
			texture.init( 1 );
			data = ByteBuffer.allocateDirect( 4 ); // allocate a bit more than needed...
			data.order( ByteOrder.nativeOrder() );	
		}
		texture.upload( context, data );
		return texture;
	}
}
