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

import static com.jogamp.opengl.GL3.GL_TEXTURE_2D_MULTISAMPLE;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import bvvpg.core.backend.Texture2D;

/** 
 * Framebuffer that supports multisampling anti-aliasing.
 * Intended to be used for geometry primitives rendering.
 * Hardcoded number of samples is equal to 4.
 * 
 * @author Eugene Katrukha
 *
 */
public class MultisampleGeometryBuffer
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

	
	/**
	 * Use {@code GL_RGB32F} as internalFormat.
	 * @param fbWidth width of offscreen framebuffer
	 * @param fbHeight height of offscreen framebuffer
	 * @param internalFormat internal texture format
	 */
	public MultisampleGeometryBuffer( final int fbWidth, final int fbHeight, final int internalFormat )
	{
		this( fbWidth, fbHeight, internalFormat, false );
	}
	
	/**
	 * Use {@code GL_RGB32F} as internalFormat.
	 * @param fbWidth width of offscreen framebuffer
	 * @param fbHeight height of offscreen framebuffer
	 * @param flipY whether to flip the Y axis when {@link #drawQuad drawing the texture}
	 */
	public MultisampleGeometryBuffer( final int fbWidth, final int fbHeight, final boolean flipY )
	{
		this( fbWidth, fbHeight, GL_RGB32F, flipY );
	}

	/**
	 * @param fbWidth width of offscreen framebuffer
	 * @param fbHeight height of offscreen framebuffer
	 * @param internalFormat internal texture format
	 * @param flipY whether to flip the Y axis when {@link #drawQuad drawing the texture}
	 */
	public MultisampleGeometryBuffer( final int fbWidth, final int fbHeight, final int internalFormat, final boolean flipY )
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

	public Texture2D getDepthTexture()
	{
		return resolveBuffer.getDepthTexture();
	}


	public void bind( GL3 gl, boolean clear )
	{
		initFrameBuffer( gl );
			
		if(!bInitResolveBuffer)
		{
			resolveBuffer.initFrameBuffer( gl );
			bInitResolveBuffer = true;
		}

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
		gl.glEnable( GL.GL_MULTISAMPLE );
	}

	public void unbind( GL3 gl )
	{		
		gl.glBindFramebuffer(GL.GL_READ_FRAMEBUFFER, framebuffer);
		gl.glBindFramebuffer(GL.GL_DRAW_FRAMEBUFFER, resolveBuffer.getFrameBuffer());
		gl.glBlitFramebuffer(
			    0, 0, fbWidth, fbHeight, 
			    0, 0, fbWidth, fbHeight, 
			    GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT, // Resolve both color and depth!
			    GL.GL_NEAREST
			);
		gl.glDisable( GL.GL_MULTISAMPLE );
		gl.glBindFramebuffer( GL_FRAMEBUFFER, restoreFramebuffer );
		gl.glViewport( viewport[ 0 ], viewport[ 1 ], viewport[ 2 ], viewport[ 3 ] );
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
		resolveBuffer.drawQuad( gl, minFilter, magFilter );
	}
	
	public void drawQuadAlpha( GL3 gl )
	{
		resolveBuffer.drawQuadAlpha( gl );
	}

	/** draws only current stored depth component, optionally flipping it **/
	public void drawQuadDepth( GL3 gl, boolean bFlipY )
	{
		resolveBuffer.drawQuadDepth( gl, bFlipY );
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
