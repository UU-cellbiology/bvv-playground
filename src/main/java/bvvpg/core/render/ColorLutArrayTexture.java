package bvvpg.core.render;

import java.nio.ByteBuffer;

import bvvpg.core.backend.GpuContext;
import bvvpg.core.backend.Texture;


public class ColorLutArrayTexture implements Texture
{
	private final int size[] = new int[ 3 ];

	public ColorLutArrayTexture( final int width, final int height, final int depth )
	{
		this.size[0] = width;
		this.size[1] = height;
		this.size[2] = depth;
	}

	public void uploadLayer( final GpuContext context, final int layerIndex, final ByteBuffer buffer )
	{
		// Uploads a 2D slice (depth = 1) at zoffset = layerIndex
		context.texSubImage3D( this, 0, 0, layerIndex, size[0], size[1], 1, buffer );
	}

	@Override
	public int texDims()
	{
		return 3; // 2D Texture Array uses 3D texture coordinate addressing (u, v, layer)
	}

	@Override
	public int texWidth()
	{
		return size[0];
	}

	@Override
	public int texHeight()
	{
		return size[1];
	}

	@Override
	public int texDepth()
	{
		return size[2];
	}

	@Override
	public InternalFormat texInternalFormat()
	{
		return InternalFormat.RGBA8;
	}

	@Override
	public MinFilter texMinFilter()
	{
		return MinFilter.NEAREST;
	}

	@Override
	public MagFilter texMagFilter()
	{
		return MagFilter.NEAREST;
	}

	@Override
	public Wrap texWrap()
	{
		return Wrap.CLAMP_TO_EDGE;
	}
}