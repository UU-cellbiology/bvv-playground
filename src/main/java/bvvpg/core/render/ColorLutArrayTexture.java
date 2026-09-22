/*-
 * #%L
 * Volume rendering of bdv datasets with gamma and transparency option
 * %%
 * Copyright (C) 2022 - 2026 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
