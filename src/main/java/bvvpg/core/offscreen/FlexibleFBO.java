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
package bvvpg.core.offscreen;

import static com.jogamp.opengl.GL.GL_COLOR_ATTACHMENT0;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_ATTACHMENT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_COMPONENT24;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER_BINDING;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER_COMPLETE;
import static com.jogamp.opengl.GL.GL_LINEAR;
import static com.jogamp.opengl.GL.GL_RGB32F;
import static com.jogamp.opengl.GL.GL_VIEWPORT;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_2D_MULTISAMPLE;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

import bvvpg.core.backend.Texture2D;

public class FlexibleFBO
{
	private int framebuffer;

	private int texColorBuffer;

	private int texDepthBuffer;

	private final int fbWidth;

	private final int fbHeight;
	
	/** final averaged framebuffer for rendering **/
	private final OffScreenFrameBufferWithDepth resolveBuffer;
	
	private boolean bInitResolveBuffer = false;

	// texture format for color attachment
	private final int internalFormat;

	// back up window viewport when binding this OffScreenFrameBuffer
	private int[] viewport = new int[ 4 ];

	private boolean framebufferInitialized;

	private int restoreFramebuffer;

	private boolean useMSAA = false;
	
	private boolean bIsFBOBound = false;
	
	/**
	 * Use {@code GL_RGB32F} as internalFormat.
	 * @param fbWidth width of offscreen framebuffer
	 * @param fbHeight height of offscreen framebuffer
	 * @param internalFormat internal texture format
	 */
	public FlexibleFBO( final int fbWidth, final int fbHeight, final int internalFormat )
	{
		this( fbWidth, fbHeight, internalFormat, false );
	}
	
	/**
	 * Use {@code GL_RGB32F} as internalFormat.
	 * @param fbWidth width of offscreen framebuffer
	 * @param fbHeight height of offscreen framebuffer
	 * @param flipY whether to flip the Y axis when {@link #drawQuad drawing the texture}
	 */
	public FlexibleFBO( final int fbWidth, final int fbHeight, final boolean flipY )
	{
		this( fbWidth, fbHeight, GL_RGB32F, flipY );
	}

	/**
	 * @param fbWidth width of offscreen framebuffer
	 * @param fbHeight height of offscreen framebuffer
	 * @param internalFormat internal texture format
	 * @param flipY whether to flip the Y axis when {@link #drawQuad drawing the texture}
	 */
	public FlexibleFBO( final int fbWidth, final int fbHeight, final int internalFormat, final boolean flipY )
	{
		this.fbWidth = fbWidth;
		this.fbHeight = fbHeight;
		this.internalFormat = internalFormat;
		
		resolveBuffer = new OffScreenFrameBufferWithDepth(fbWidth, fbHeight, internalFormat, flipY);
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

		// generate texture IDs
		gl.glGenTextures( 2, tmp, 0 );
		texColorBuffer = tmp[ 0 ];
		texDepthBuffer = tmp[ 1 ];
		
		gl.glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, texColorBuffer);
		gl.glTexStorage2DMultisample( GL_TEXTURE_2D_MULTISAMPLE, 4, internalFormat, fbWidth, fbHeight, true );

		gl.glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, texDepthBuffer);
		gl.glTexStorage2DMultisample( GL_TEXTURE_2D_MULTISAMPLE, 4, GL_DEPTH_COMPONENT24, fbWidth, fbHeight, true );

		gl.glBindTexture( GL_TEXTURE_2D_MULTISAMPLE, 0 );

		// attach it to currently bound framebuffer object
		gl.glFramebufferTexture2D( GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D_MULTISAMPLE, texColorBuffer, 0 );
		gl.glFramebufferTexture2D( GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D_MULTISAMPLE, texDepthBuffer, 0 );

		if ( gl.glCheckFramebufferStatus( GL_FRAMEBUFFER ) != GL_FRAMEBUFFER_COMPLETE )
			System.err.println( "ERROR::FRAMEBUFFER:: Framebuffer is not complete!" );
		gl.glBindFramebuffer( GL_FRAMEBUFFER, restoreFramebuffer );

	}
	
	public void setMSAAEnabled(boolean bEnabled)
	{
		if(!bIsFBOBound)
		{
			useMSAA = bEnabled;
		}
		else
		{
			System.err.println("Cannot change buffer MSAA settings while bound.");
		}
	}

	public int getTexColorBuffer()
	{
		return resolveBuffer.getTexColorBuffer();
	}

	public int getTexDepthBuffer()
	{
		return resolveBuffer.getTexDepthBuffer();
	}
	
	/**
	 * Bind this framebuffer and clear it.
	 * Call before rendering.
	 */
	public void bind( GL3 gl )
	{
		bind( gl, true );
	}

	public void bind( GL3 gl, boolean clear )
	{
		initFrameBuffer( gl );
		bIsFBOBound = true;	
		if(!bInitResolveBuffer)
		{
			resolveBuffer.initFrameBuffer( gl );
			bInitResolveBuffer = true;
		}

		final int[] tmp = new int[ 1 ];
		gl.glGetIntegerv( GL_FRAMEBUFFER_BINDING, tmp, 0 );
		restoreFramebuffer = tmp[ 0 ];
		
		if(useMSAA)
		{	
			gl.glBindFramebuffer( GL_FRAMEBUFFER, framebuffer );
		}
		else
		{
			gl.glBindFramebuffer( GL_FRAMEBUFFER, resolveBuffer.getFrameBuffer() );
		}
		gl.glGetIntegerv( GL_VIEWPORT, viewport, 0 );
		gl.glViewport( 0, 0, fbWidth, fbHeight );
		if ( clear )
		{
			gl.glClearColor( 0, 0, 0, 0 );
			gl.glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );
		}
		
		if(useMSAA)
		{
			gl.glEnable( GL.GL_MULTISAMPLE );		
		}
	}

	public void unbind( GL3 gl, boolean getTexture )
	{	
		if(useMSAA)
		{
			gl.glBindFramebuffer(GL.GL_READ_FRAMEBUFFER, framebuffer);
			gl.glBindFramebuffer(GL.GL_DRAW_FRAMEBUFFER, resolveBuffer.getFrameBuffer());
			gl.glBlitFramebuffer(
				    0, 0, fbWidth, fbHeight, 
				    0, 0, fbWidth, fbHeight, 
				    GL.GL_COLOR_BUFFER_BIT, 
				    GL.GL_LINEAR
				); 
				gl.glBlitFramebuffer(
				    0, 0, fbWidth, fbHeight, 
				    0, 0, fbWidth, fbHeight, 
				    GL.GL_DEPTH_BUFFER_BIT, 
				    GL.GL_NEAREST
				);
			gl.glDisable( GL.GL_MULTISAMPLE );
		}
		
		gl.glBindFramebuffer( GL_FRAMEBUFFER, restoreFramebuffer );
		gl.glViewport( viewport[ 0 ], viewport[ 1 ], viewport[ 2 ], viewport[ 3 ] );
		if ( getTexture )
			resolveBuffer.getTexture( gl );
		bIsFBOBound = false;
	}	
	
	public Img< FloatType > getDepthImg()
	{
		return resolveBuffer.getDepthImg();
	}

	public Texture2D getDepthTexture()
	{
		return resolveBuffer.getDepthTexture();
	}

	/**
	 * Render fullscreen quad with the texture (only color).
	 */
	public void drawQuad( GL3 gl )
	{
		drawQuad( gl, GL_LINEAR, GL_LINEAR );
	}

	public void drawQuad( GL3 gl, int minFilter, int magFilter )
	{
		resolveBuffer.drawQuad( gl, minFilter, magFilter, false );
	}

	public void drawQuad( GL3 gl, boolean bFlipY )
	{
		resolveBuffer.drawQuad( gl, GL_LINEAR, GL_LINEAR, bFlipY );
	}
	
	public void drawQuadAlpha( GL3 gl )
	{
		resolveBuffer.drawQuadAlpha( gl );
	}

	/** draws only current stored depth component, optionally flipping it **/
	public void drawQuadDepth( GL3 gl, boolean bFlipY )
	{
		resolveBuffer.drawQuadOnlyDepth( gl, bFlipY );
	}
	
	/** draws both color and depth **/
	public void drawQuadColorDepth( GL3 gl )
	{
		resolveBuffer.drawQuadColorDepth( gl );
	}
	
	public void drawQuadEDL( GL3 gl, float fnratio, float fRadius, float fStrength)
	{
		resolveBuffer.drawQuadEDL( gl, fnratio, fRadius, fStrength );
	}

	public int getWidth()
	{
		return fbWidth;
	}

	public int getHeight()
	{
		return fbHeight;
	}
}
