package btbvv.btuitools;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;


import btbvv.core.backend.GpuContext;
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
	
	public boolean bInit = false;

	public BTLutTexture()
	{
		size[0]=1;
		data = ByteBuffer.allocateDirect( 4 );
		data.order( ByteOrder.nativeOrder() );
	}

	public void init(final RandomAccessibleInterval< ARGBType > rai)
	{
		size[0]=(int) Intervals.numElements( rai );
		final int numBytes = ( int ) ( 4 *  size[0]);
		data = ByteBuffer.allocateDirect( numBytes ); // allocate a bit more than needed...
		data.order( ByteOrder.nativeOrder() );
		copyToBufferRGBA8( rai );
		bInit = true;
	}
	
	public void upload( final GpuContext context )
	{
		context.delete( this ); // TODO: is this necessary everytime?
		context.texSubImage3D( this, 0, 0, 0, texWidth(), texHeight(), texDepth(), data );
	}
	
//	private static void copyToBufferRGBA8( final RandomAccessibleInterval< ARGBType > rai, final ByteBuffer buffer )
//	{
//		// TODO handle specific RAI types more efficiently
//		// TODO multithreading
//		final Cursor< ARGBType > cursor = Views.flatIterable( rai ).cursor();
//		final IntBuffer sdata = buffer.asIntBuffer();
//		int i = 0;
//		while ( cursor.hasNext() )
//			sdata.put( i++, toRGBA( cursor.next().get() ) );
//	}
	private static void copyToBufferRGBA8( final RandomAccessibleInterval< ARGBType > rai )
	{
		// TODO handle specific RAI types more efficiently
		// TODO multithreading
		final Cursor< ARGBType > cursor = Views.flatIterable( rai ).cursor();
		final IntBuffer sdata = data.asIntBuffer();
		int i = 0;
		while ( cursor.hasNext() )
			sdata.put( i++, toRGBA( cursor.next().get() ) );
	}
	
	private static int toRGBA( final int argb )
	{
		final int a = ( argb >> 24 ) & 0xff;
		final int r = ( argb >> 16 ) & 0xff;
		final int g = ( argb >> 8 ) & 0xff;
		final int b = argb & 0xff;
		return ( a << 24 ) | ( b << 16 ) | ( g << 8 ) | r;
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

	@Override
	public int texDepth() {
		
		return 1;
	}



}
