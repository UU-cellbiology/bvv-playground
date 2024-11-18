/*-
 * #%L
 * Volume rendering of bdv datasets with gamma and transparency option
 * %%
 * Copyright (C) 2022 - 2024 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
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
package bvvpg.pguitools;

import java.nio.Buffer;

import bvvpg.core.backend.GpuContext;
import bvvpg.core.backend.Texture3D;


public class LutCSTexturePG implements Texture3D
{
	/**
	 * Size of the lut texture.
	 */
	private final int[] size = new int[ 2 ];

	public void init( final int size_)
	{
		this.size[ 0 ] = 256;
		this.size[ 1 ] = (int)Math.ceil(size_/256.0);
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
		return size[ 1 ];
	}
	@Override
	public int texDepth() 
	{
		
		return 1;
	}


	@Override
	public MinFilter texMinFilter() 
	{
		return MinFilter.NEAREST;
		//return MinFilter.LINEAR;
	}

	@Override
	public MagFilter texMagFilter() 
	{
		return MagFilter.NEAREST;
		//return MagFilter.LINEAR;
	}

	@Override
	public Wrap texWrap() 
	{
		return Wrap.CLAMP_TO_EDGE;
	}


}
