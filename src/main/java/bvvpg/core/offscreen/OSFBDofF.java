package bvvpg.core.offscreen;

import static com.jogamp.opengl.GL.GL_CLAMP_TO_EDGE;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_LINEAR;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE1;
import static com.jogamp.opengl.GL.GL_TEXTURE2;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_S;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_T;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;
import static com.jogamp.opengl.GL.GL_RGB32F;
import static com.jogamp.opengl.GL.GL_RGB8;

import com.jogamp.opengl.GL3;

import bvvpg.core.backend.jogl.JoglGpuContext;
import bvvpg.core.shadergen.DefaultShader;
import bvvpg.core.shadergen.generate.Segment;
import bvvpg.core.shadergen.generate.SegmentTemplate;

public class OSFBDofF extends OffScreenFrameBufferWithDepth
{
	private final DefaultShader progQuadBlur;
	private final DefaultShader progDoF;
	
	private final OffScreenFrameBuffer blurBuf;

	public OSFBDofF( int fbWidth, int fbHeight, boolean flipY )
	{
		this( fbWidth, fbHeight, GL_RGB32F, flipY );
	}
	public OSFBDofF( final int fbWidth, final int fbHeight, final int internalFormat, final boolean flipY )
	{
		super(fbWidth, fbHeight, internalFormat, flipY);
		final Segment quadvpblur = new SegmentTemplate( OffScreenFrameBufferWithDepth.class, "osfbquadblur.vp" ).instantiate();
		final Segment quadfpblur = new SegmentTemplate( OffScreenFrameBufferWithDepth.class, "osfbquadblur.fp" ).instantiate();
		progQuadBlur = new DefaultShader( quadvpblur.getCode(), quadfpblur.getCode() );
		
		final Segment quadvpdof = new SegmentTemplate( OffScreenFrameBufferWithDepth.class, "osfbquaddof.vp" ).instantiate();
		final Segment quadfpdof = new SegmentTemplate( OffScreenFrameBufferWithDepth.class, "osfbquaddof.fp" ).instantiate();
		progDoF = new DefaultShader( quadvpdof.getCode(), quadfpdof.getCode() );
		
		blurBuf = new OffScreenFrameBuffer(fbWidth,fbHeight,GL_RGB8, false, true);
	}
	
	public void drawQuadBlurred( GL3 gl, final float xf, final float focalDepth, final float focalRange, final float fBlurRadius)
	{
		blurBuf.bind( gl );
		drawQuadBlurred( gl, GL_LINEAR, GL_LINEAR, fBlurRadius );
		blurBuf.unbind( gl );
		drawQuadDoF( gl, GL_LINEAR, GL_LINEAR, xf, focalDepth, focalRange);
	}
	
	public void drawQuadDoF( GL3 gl, int minFilter, int magFilter, final float xf, final float focalDepth, final float focalRange)
	{
		initQuad( gl );

		final JoglGpuContext context = JoglGpuContext.get( gl );
		gl.glActiveTexture( GL_TEXTURE0 );
		gl.glBindTexture( GL_TEXTURE_2D, texColorBuffer  );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
		progDoF.getUniform1i("colorTex").set( 0 );
		
		gl.glActiveTexture( GL_TEXTURE1);
		gl.glBindTexture( GL_TEXTURE_2D, blurBuf.getTexColorBuffer() );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
		progDoF.getUniform1i("blurTex").set( 1 );
		
		gl.glActiveTexture( GL_TEXTURE2);
		gl.glBindTexture( GL_TEXTURE_2D, texDepthBuffer );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
		progDoF.getUniform1i("depthTex").set( 2 );
		
		progDoF.getUniform1f("xf").set( xf );
		progDoF.getUniform1f("focalDepth").set( focalDepth );
		progDoF.getUniform1f("focalRange").set( focalRange );
		
		progDoF.use( context );
		progDoF.setUniforms( context );
		gl.glBindVertexArray( vaoQuad );
		gl.glDrawElements( GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0 );
		gl.glBindVertexArray( 0 );
		gl.glBindTexture( GL_TEXTURE_2D, 0 );
	}
	
	public void drawQuadBlurred( GL3 gl, int minFilter, int magFilter, final float fBlurRadius )
	{
		initQuad( gl );
		final JoglGpuContext context = JoglGpuContext.get( gl );
		
		gl.glActiveTexture( GL_TEXTURE0 );
		gl.glBindTexture( GL_TEXTURE_2D, texColorBuffer );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
		progQuadBlur.getUniform1f( "r" ).set( fBlurRadius );
		progQuadBlur.getUniform1i( "nFlip" ).set( 1 );
		progQuadBlur.getUniform1i( "tex" ).set( 0 );
		progQuadBlur.setUniforms( context );
	
		progQuadBlur.use( context );
		gl.glBindVertexArray( vaoQuad );
		gl.glDrawElements( GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0 );
		gl.glBindVertexArray( 0 );
		gl.glBindTexture( GL_TEXTURE_2D, 0 );
	}
}
