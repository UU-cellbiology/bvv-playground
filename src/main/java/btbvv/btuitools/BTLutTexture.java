package btbvv.btuitools;

import java.awt.image.IndexColorModel;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;


import btbvv.core.backend.GpuContext;
import btbvv.core.backend.Texture1D;
import btbvv.core.backend.Texture3D;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;

import net.imglib2.type.numeric.ARGBType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;


public class BTLutTexture implements Texture3D
{
	/**
	 * Size of the lut texture.
	 */
	private final int[] size = new int[ 1 ];
	
	private static ByteBuffer data;
	//private ByteBuffer data;
	public boolean bNeedsUpload = false;

	public BTLutTexture()
	{
		size[0]=1;
		data = ByteBuffer.allocateDirect( 4 );
		data.order( ByteOrder.nativeOrder() );
	}

	public void initBuffer(final IndexColorModel icm)
	{
		size[0] = icm.getMapSize();
		final int numBytes = 4 *  size[0];
		data = ByteBuffer.allocateDirect( numBytes ); // allocate a bit more than needed...
		data.order( ByteOrder.nativeOrder() );
		final IntBuffer sdata = data.asIntBuffer();
		byte [][] colors = new byte[3][size[0]];
		icm.getReds(colors[0]);
		icm.getGreens(colors[1]);
		icm.getBlues(colors[2]);
		for (int i=0; i<size[0];i++)
		{
			final int r = colors[0][i] & 0xff;
			final int g = colors[1][i] & 0xff;
			final int b = colors[2][i] & 0xff;
			final int all = ( 255 << 24 ) | ( b << 16 ) | ( g << 8 ) | r;
			sdata.put( i,all );
		}
		//copyToBufferRGBA8( rai );
		bNeedsUpload = true;
	}
	
	public void upload( final GpuContext context )
	{
		context.delete( this ); // TODO: is this necessary everytime?
		context.texSubImage3D( this, 0, 0, 0, texWidth(), texHeight(), texDepth(), data );
		bNeedsUpload = false;
		//context.texSubImage1D( this, 0, texWidth(), data );
	}
	

	
	@Override
	public InternalFormat texInternalFormat() 
	{
		return InternalFormat.RGBA8;
	}

	@Override
	public int texWidth() 
	{
		return size[ 0 ];
	}

	@Override
	public int texHeight() 
	{
		return 1;
	}
	@Override
	public int texDepth() 
	{
		
		return 1;
	}


	@Override
	public MinFilter texMinFilter() 
	{
		return MinFilter.LINEAR;
	}

	@Override
	public MagFilter texMagFilter() 
	{
		return MagFilter.LINEAR;
	}

	@Override
	public Wrap texWrap() 
	{
		return Wrap.CLAMP_TO_EDGE;
	}





}
