/*-
 * #%L
 * Volume rendering of bdv datasets with gamma and transparency option
 * %%
 * Copyright (C) 2022 - 2025 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
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
package bvvpg.core.offscreen;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import bvvpg.core.backend.Texture2D;
import bvvpg.core.backend.jogl.JoglGpuContext;
import bvvpg.core.shadergen.DefaultShader;
import bvvpg.core.shadergen.generate.Segment;
import bvvpg.core.shadergen.generate.SegmentTemplate;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_CLAMP_TO_EDGE;
import static com.jogamp.opengl.GL.GL_COLOR_ATTACHMENT0;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_ATTACHMENT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_COMPONENT24;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER_BINDING;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER_COMPLETE;
import static com.jogamp.opengl.GL.GL_LINEAR;
import static com.jogamp.opengl.GL.GL_RGB;
import static com.jogamp.opengl.GL.GL_RGB32F;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_S;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_T;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;
import static com.jogamp.opengl.GL.GL_VIEWPORT;
import static com.jogamp.opengl.GL2ES2.GL_DEPTH_COMPONENT;

/**
 * Render to texture. Print values. For debugging debugging shaders.
 */
public class OffScreenFrameBufferWithDepth
{
	private int vaoQuad;

	private final DefaultShader progQuad;
	
	private final DefaultShader progQuadAlpha;
	
	private final DefaultShader progQuadDepth;
	
	private boolean flipY;

	private int framebuffer;

	private int texColorBuffer;

	private int texDepthBuffer;

	private final Texture2D depthTexture;

	private final int fbWidth;

	private final int fbHeight;

	// texture format for color attachment
	private final int internalFormat;

	// back up window viewport when binding this OffScreenFrameBuffer
	private int[] viewport = new int[ 4 ];

	// downloaded texture data
	private float[] rgb;

	// downloaded texture data
	private float[] depth;

	// downloaded texture data as 3 * fwWidth * fbHeight image
	private Img< FloatType > rgbImg;

	// downloaded texture data as 3 * fwWidth * fbHeight image
	private Img< FloatType > depthImg;

	private boolean framebufferInitialized;

	private boolean quadInitialized;

	private boolean imgsInitialized;

	private boolean imgValid;

	private int restoreFramebuffer;

	
	/**
	 * Use {@code GL_RGB32F} as internalFormat.
	 * @param fbWidth width of offscreen framebuffer
	 * @param fbHeight height of offscreen framebuffer
	 * @param internalFormat internal texture format
	 */
	public OffScreenFrameBufferWithDepth( final int fbWidth, final int fbHeight, final int internalFormat )
	{
		this( fbWidth, fbHeight, internalFormat, false );
	}
	
	/**
	 * Use {@code GL_RGB32F} as internalFormat.
	 * @param fbWidth width of offscreen framebuffer
	 * @param fbHeight height of offscreen framebuffer
	 * @param flipY whether to flip the Y axis when {@link #drawQuad drawing the texture}
	 */
	public OffScreenFrameBufferWithDepth( final int fbWidth, final int fbHeight, final boolean flipY )
	{
		this( fbWidth, fbHeight, GL_RGB32F, flipY );
	}

	/**
	 * @param fbWidth width of offscreen framebuffer
	 * @param fbHeight height of offscreen framebuffer
	 * @param internalFormat internal texture format
	 * @param flipY whether to flip the Y axis when {@link #drawQuad drawing the texture}
	 */
	public OffScreenFrameBufferWithDepth( final int fbWidth, final int fbHeight, final int internalFormat, final boolean flipY )
	{
		this.fbWidth = fbWidth;
		this.fbHeight = fbHeight;
		this.internalFormat = internalFormat;
		this.flipY = flipY;

		final Segment quadvp = new SegmentTemplate( OffScreenFrameBufferWithDepth.class, "osfbquad.vp" ).instantiate();
		final Segment quadfp = new SegmentTemplate( OffScreenFrameBufferWithDepth.class, "osfbquad.fp" ).instantiate();
		progQuad = new DefaultShader( quadvp.getCode(), quadfp.getCode() );

		final Segment quadfpa = new SegmentTemplate( OffScreenFrameBufferWithDepth.class, "osfbquad_alpha.fp" ).instantiate();
		progQuadAlpha = new DefaultShader( quadvp.getCode(), quadfpa.getCode() );
		
		final Segment quadvpd = new SegmentTemplate( OffScreenFrameBufferWithDepth.class, "osfbquad_depth.vp" ).instantiate();
		final Segment quadfpd = new SegmentTemplate( OffScreenFrameBufferWithDepth.class, "osfbquad_depth.fp" ).instantiate();
		progQuadDepth = new DefaultShader( quadvpd.getCode(), quadfpd.getCode() );

		depthTexture = new DepthTexture( fbWidth, fbHeight );
	}

	private void initFrameBuffer( GL3 gl )
	{
		if ( framebufferInitialized )
			return;
		framebufferInitialized = true;

		final int[] tmp = new int[ 2 ];
		gl.glGenFramebuffers( 1, tmp, 0 );
		framebuffer = tmp[ 0 ];

		gl.glGetIntegerv( GL_FRAMEBUFFER_BINDING, tmp, 0 );
		restoreFramebuffer = tmp[ 0 ];
		gl.glBindFramebuffer( GL_FRAMEBUFFER, framebuffer );

		// generate texture
		gl.glGenTextures( 2, tmp, 0 );
		texColorBuffer = tmp[ 0 ];
		gl.glBindTexture( GL_TEXTURE_2D, texColorBuffer );
		gl.glTexStorage2D( GL_TEXTURE_2D, 1, internalFormat, fbWidth, fbHeight );
		texDepthBuffer = tmp[ 1 ];
		gl.glBindTexture( GL_TEXTURE_2D, texDepthBuffer );
		gl.glTexStorage2D( GL_TEXTURE_2D, 1, GL_DEPTH_COMPONENT24, fbWidth, fbHeight );
		gl.glBindTexture( GL_TEXTURE_2D, 0 );

		// attach it to currently bound framebuffer object
		gl.glFramebufferTexture2D( GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texColorBuffer, 0 );
		gl.glFramebufferTexture2D( GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, texDepthBuffer, 0 );

		if ( gl.glCheckFramebufferStatus( GL_FRAMEBUFFER ) != GL_FRAMEBUFFER_COMPLETE )
			System.err.println( "ERROR::FRAMEBUFFER:: Framebuffer is not complete!" );
		gl.glBindFramebuffer( GL_FRAMEBUFFER, restoreFramebuffer );

		JoglGpuContext.get( gl ).registerTexture( depthTexture, texDepthBuffer );
	}

	private void initQuad( GL3 gl )
	{
		if ( quadInitialized )
			return;
		quadInitialized = true;

		final float verticesQuad[] = {
				//    pos      texture
				 1,  1, 0,     1, 1,   // top right
				 1, -1, 0,     1, 0,   // bottom right
				-1, -1, 0,     0, 0,   // bottom left
				-1,  1, 0,     0, 1    // top left
		};
		
		final float verticesQuadFlipY[] = {
				//    pos      texture
				 1,  1, 0,     1, 0,   // top right
				 1, -1, 0,     1, 1,   // bottom right
				-1, -1, 0,     0, 1,   // bottom left
				-1,  1, 0,     0, 0    // top left
		};

		final int[] tmp = new int[ 1 ];
		gl.glGenBuffers( 1, tmp, 0 );
		final int vboQuad = tmp[ 0 ];
		gl.glBindBuffer( GL_ARRAY_BUFFER, vboQuad );
		gl.glBufferData( GL_ARRAY_BUFFER, verticesQuad.length * Float.BYTES, FloatBuffer.wrap( flipY ? verticesQuadFlipY : verticesQuad ), GL.GL_STATIC_DRAW );
		gl.glBindBuffer( GL_ARRAY_BUFFER, 0 );

		final int indices[] = {
				0, 3, 1,   // first triangle
				1, 3, 2    // second triangle
		};
		gl.glGenBuffers( 1, tmp, 0 );
		final int eboQuad = tmp[ 0 ];
		gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, eboQuad );
		gl.glBufferData( GL_ELEMENT_ARRAY_BUFFER, indices.length * Integer.BYTES, IntBuffer.wrap( indices ), GL.GL_STATIC_DRAW );
		gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, 0 );

		gl.glGenVertexArrays( 1, tmp, 0 );
		vaoQuad = tmp[ 0 ];
		gl.glBindVertexArray( vaoQuad );
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, vboQuad );
		gl.glVertexAttribPointer( 0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0 );
		gl.glEnableVertexAttribArray( 0 );
		gl.glVertexAttribPointer( 1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES );
		gl.glEnableVertexAttribArray( 1 );
		gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, eboQuad );
		gl.glBindVertexArray( 0 );
	}

	private void initImgs()
	{
		System.out.println( "OffScreenFrameBufferWithDepth.initImgs" );
		if ( imgsInitialized )
			return;
		imgsInitialized = true;

		rgb = new float[ fbWidth * fbHeight * 3 ];
		rgbImg = ArrayImgs.floats( rgb, 3, fbWidth, fbHeight );

		depth = new float[ fbWidth * fbHeight ];
		depthImg = ArrayImgs.floats( depth, fbWidth, fbHeight );
	}

	public int getTexColorBuffer()
	{
		return texColorBuffer;
	}

	public int getTexDepthBuffer()
	{
		return texDepthBuffer;
	}
	
	/**
	 * Bind this framebuffer and clear it.
	 * Call before rendering.
	 */
	public void bind( GL3 gl )
	{
		bind( gl, true );
	}

	/**
	 * Unbind this framebuffer (bind framebuffer 0) and download the texture into a float array.
	 * Call after rendering.
	 */
	public void unbind( GL3 gl )
	{
		unbind( gl, true );
	}

	public Img< FloatType > getDepthImg()
	{
		return depthImg;
	}

	public Texture2D getDepthTexture()
	{
		return depthTexture;
	}

	/**
	 * Get a value from the texture (downloaded by {@link #unbind(GL3)} or {@link #getTexture(GL3)})
	 * @param c channel (rgb)
	 * @param x
	 * @param y
	 * @return color value from the texture
	 */
	public float get( int c, int x, int y )
	{
		if ( !imgValid )
			System.err.println( "Img not valid. Call getTexture() first." );

		final RandomAccess< FloatType > a = rgbImg.randomAccess();
		a.setPosition( new long[] { c, x, y } );
		return a.get().get();
	}

	public void bind( GL3 gl, boolean clear )
	{
		initFrameBuffer( gl );

		final int[] tmp = new int[ 1 ];
		gl.glGetIntegerv( GL_FRAMEBUFFER_BINDING, tmp, 0 );
		restoreFramebuffer = tmp[ 0 ];

		gl.glBindFramebuffer( GL_FRAMEBUFFER, framebuffer );
		gl.glGetIntegerv( GL_VIEWPORT, viewport, 0 );
		gl.glViewport( 0, 0, fbWidth, fbHeight );
		if ( clear )
		{
			gl.glClearColor( 0, 0, 0, 0 );
			gl.glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );
		}
	}

	public void unbind( GL3 gl, boolean getTexture )
	{
		gl.glBindFramebuffer( GL_FRAMEBUFFER, restoreFramebuffer );
		gl.glViewport( viewport[ 0 ], viewport[ 1 ], viewport[ 2 ], viewport[ 3 ] );
		imgValid = false;

		if ( getTexture )
			getTexture( gl );
	}

	/**
	 * Render fullscreen quad with the texture.
	 */
	public void drawQuad( GL3 gl )
	{
		drawQuad( gl, GL_LINEAR, GL_LINEAR );
	}

	public void drawQuad( GL3 gl, int minFilter, int magFilter )
	{
		initQuad( gl );

		progQuad.use( JoglGpuContext.get( gl ) );
		gl.glActiveTexture( GL_TEXTURE0 );
		gl.glBindTexture( GL_TEXTURE_2D, texColorBuffer );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
		gl.glBindVertexArray( vaoQuad );
		gl.glDrawElements( GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0 );
		gl.glBindVertexArray( 0 );
		gl.glBindTexture( GL_TEXTURE_2D, 0 );
	}
	
	public void drawQuadAlpha( GL3 gl )
	{
		initQuad( gl );

		progQuadAlpha.use( JoglGpuContext.get( gl ) );
		gl.glActiveTexture( GL_TEXTURE0 );
		gl.glBindTexture( GL_TEXTURE_2D, texColorBuffer );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
		gl.glBindVertexArray( vaoQuad );
		gl.glDrawElements( GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0 );
		gl.glBindVertexArray( 0 );
		gl.glBindTexture( GL_TEXTURE_2D, 0 );
	}

	/** draws only current stored depth component, optionally flipping it **/
	public void drawQuadDepth( GL3 gl, boolean bFlipY )
	{
		initQuad( gl );
		JoglGpuContext context = JoglGpuContext.get( gl );
		progQuadDepth.getUniform1i( "nFlip" ).set( bFlipY?1:0 );
		progQuadDepth.setUniforms( context );
		progQuadDepth.use( context );
		gl.glActiveTexture( GL_TEXTURE0 );
		gl.glBindTexture( GL_TEXTURE_2D, texDepthBuffer );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
		gl.glBindVertexArray( vaoQuad );
		gl.glDrawElements( GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0 );
		gl.glBindVertexArray( 0 );
		gl.glBindTexture( GL_TEXTURE_2D, 0 );
	}

	public void getTexture( GL3 gl )
	{
		if ( imgValid )
			return;
		imgValid = true;

		initImgs();

		gl.glBindTexture( GL_TEXTURE_2D, texColorBuffer );
		gl.glGetTexImage( GL_TEXTURE_2D, 0, GL_RGB, GL_FLOAT, FloatBuffer.wrap( rgb ) );
		gl.glBindTexture( GL_TEXTURE_2D, texDepthBuffer );
		gl.glGetTexImage( GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, GL_FLOAT, FloatBuffer.wrap( depth ) );
		gl.glBindTexture( GL_TEXTURE_2D, 0 );
	}

	public int getWidth()
	{
		return fbWidth;
	}

	public int getHeight()
	{
		return fbHeight;
	}


	/*
	 * ================================= TEXTURE WRAPPERS =============================================
	 */


	static class DepthTexture implements Texture2D
	{
		private final int width;

		private final int height;

		public DepthTexture( final int width, final int height )
		{
			this.width = width;
			this.height = height;
		}

		@Override
		public InternalFormat texInternalFormat()
		{
			return InternalFormat.UNKNOWN;
		}

		@Override
		public int texWidth()
		{
			return width;
		}

		@Override
		public int texHeight()
		{
			return height;
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
}
