package btbvv.btuitools;

import java.nio.Buffer;

import btbvv.core.backend.GpuContext;
import btbvv.core.backend.Texture3D;


public class LutCSTextureBT implements Texture3D
{
	/**
	 * Size of the lut texture.
	 */
	private final int[] size = new int[ 1 ];

	public void init( final int size_)
	{
		this.size[ 0 ] = size_;
	}
	
	public void upload( final GpuContext context, final Buffer data )
	{
		context.delete( this ); // TODO: is this necessary everytime?
		context.texSubImage3D( this, 0, 0, 0, texWidth(), texHeight(), texDepth(), data );
		
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
