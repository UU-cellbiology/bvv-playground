package tpietzsch.example2;

import net.imglib2.display.ColorConverter;
import net.imglib2.type.numeric.ARGBType;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import tpietzsch.backend.GpuContext;
import tpietzsch.cache.CacheSpec;
import tpietzsch.cache.TextureCache;
import tpietzsch.shadergen.Uniform1f;
import tpietzsch.shadergen.Uniform3f;
import tpietzsch.shadergen.Uniform3fv;
import tpietzsch.shadergen.Uniform4f;
import tpietzsch.shadergen.UniformMatrix4f;
import tpietzsch.shadergen.UniformSampler;
import tpietzsch.shadergen.generate.Segment;
import tpietzsch.shadergen.generate.SegmentTemplate;
import tpietzsch.shadergen.generate.SegmentedShader;
import tpietzsch.shadergen.generate.SegmentedShaderBuilder;

public class MultiVolumeShaderMip6
{
	private static final int NUM_BLOCK_SCALES = 10;

	private final int numVolumes;

	private final SegmentedShader prog;
	private final VolumeSegment[] volumeSegments;
	private final ConverterSegment[] converterSegments;

	private final UniformMatrix4f uniformIpv;
	private final Uniform3f uniformViewportSize;

	private final Uniform1f uniformXf;

	public MultiVolumeShaderMip6( final int numVolumes )
	{
		this.numVolumes = numVolumes;

		final SegmentedShaderBuilder builder = new SegmentedShaderBuilder();
		final Segment vp = new SegmentTemplate("ex5vol.vp" ).instantiate();
		builder.vertex( vp );

		final SegmentTemplate templateIntersectBox = new SegmentTemplate(
				"intersectbox.fp" );
		builder.fragment( templateIntersectBox.instantiate() );
		final SegmentTemplate templateBlkVol = new SegmentTemplate(
				"blkvol.fp",
				"im", "sourcemin", "sourcemax", "intersectBoundingBox",
				"lutSampler", "blockScales", "lutSize", "lutOffset", "blockTexture" );
		final SegmentTemplate templateColConv = new SegmentTemplate(
				"colconv.fp",
				"convert", "offset", "scale" );
		final SegmentTemplate templateFp = new SegmentTemplate(
				"ex6vol.fp",
				"intersectBoundingBox", "blockTexture", "convert", "vis" );
		final Segment fp = templateFp.instantiate();
		fp.repeat( "vis", numVolumes );
		final Segment blkVols[] = new Segment[ numVolumes ];
		final Segment colConvs[] = new Segment[ numVolumes ];
		for ( int i = 0; i < numVolumes; ++i )
		{
			final Segment blkVol = templateBlkVol.instantiate();
			builder.fragment( blkVol );
			fp.bind( "intersectBoundingBox", i, blkVol, "intersectBoundingBox" );
			fp.bind( "blockTexture", i, blkVol, "blockTexture" );
			blkVols[ i ] = blkVol;

			final Segment colConv = templateColConv.instantiate();
			builder.fragment( colConv );
			fp.bind( "convert", i, colConv, "convert" );
			colConvs[ i ] = colConv;
		}
		builder.fragment( fp );
		prog = builder.build();

		uniformIpv = prog.getUniformMatrix4f( "ipv" );
		uniformXf = prog.getUniform1f( "xf" );
		uniformViewportSize = prog.getUniform3f( "viewportSize" );

		volumeSegments = new VolumeSegment[ numVolumes ];
		converterSegments = new ConverterSegment[ numVolumes ];
		for ( int i = 0; i < numVolumes; ++i )
		{
			volumeSegments[ i ] = new VolumeSegment( prog, blkVols[ i ] );
			converterSegments[ i ] = new ConverterSegment( prog, colConvs[ i ] );
		}

//		final StringBuilder vertexShaderCode = prog.getVertexShaderCode();
//		System.out.println( "vertexShaderCode = " + vertexShaderCode );
//		System.out.println( "\n\n--------------------------------\n\n" );
//		final StringBuilder fragmentShaderCode = prog.getFragmentShaderCode();
//		System.out.println( "fragmentShaderCode = " + fragmentShaderCode );
//		System.out.println( "\n\n--------------------------------\n\n" );
	}

	public int getNumVolumes()
	{
		return numVolumes;
	}

	public void setTextureCache( TextureCache textureCache )
	{
		CacheSpec spec = textureCache.spec();
		final int[] bs = spec.blockSize();
		final int[] pbs = spec.paddedBlockSize();
		final int[] bo = spec.padOffset();
		prog.getUniform3f( "blockSize" ).set( bs[ 0 ], bs[ 1 ], bs[ 2 ] );
		prog.getUniform3f( "paddedBlockSize" ).set( pbs[ 0 ], pbs[ 1 ], pbs[ 2 ] );
		prog.getUniform3f( "cachePadOffset" ).set( bo[ 0 ], bo[ 1 ], bo[ 2 ] );

		prog.getUniformSampler( "volumeCache" ).set( textureCache );
		prog.getUniform3f( "cacheSize" ).set( textureCache.texWidth(), textureCache.texHeight(), textureCache.texDepth() );
	}

	public void setConverter( int index, ColorConverter converter )
	{
		converterSegments[ index ].setData( converter );
	}

	public void setVolume( int index, VolumeBlocks volume )
	{
		volumeSegments[ index ].setData( volume );
	}

	public void setProjectionViewMatrix( final Matrix4fc pv )
	{
		uniformIpv.set( pv.invert( new Matrix4f() ) );
		stuff( pv );
	}

	private void stuff( final Matrix4fc pv )
	{
		final Matrix4f ipv = pv.invert( new Matrix4f() );
		final Vector4f a = ipv.transform( new Vector4f( 0, 0, -1, 1 ) );
		final Vector4f b = ipv.transform( new Vector4f( 0, 0,  0, 1 ) );
		final Vector4f c = ipv.transform( new Vector4f( 0, 0,  1, 1 ) );
		a.div( a.w() );
		b.div( b.w() );
		c.div( c.w() );
		double f = b.sub( a ).length() / c.sub( a ).length();
		uniformXf.set( ( float ) f );



		final int width = 640;

		Vector4f p0 = ipv.transform( new Vector4f( 0, 0, -1, 1 ) );
		p0.div( p0.w );
		Vector4f p1 = ipv.transform( new Vector4f( 0, 0, ( float ) ( -1.0 + 2.0 / width ), 1 ) );
		p1.div( p1.w );

		final float worldStepOnNear = p1.sub( p0 ).length();
		System.out.println( "worldStepOnNear = " + worldStepOnNear );

		p0 = ipv.transform( new Vector4f( 0, 0, ( float ) ( 1.0 - 2.0 / width ), 1 ) );
		p0.div( p0.w );
		p1 = ipv.transform( new Vector4f( 0, 0, 1, 1 ) );
		p1.div( p1.w );

		final float worldStepOnFar = p1.sub( p0 ).length();
		System.out.println( "worldStepOnFar = " + worldStepOnFar );

		p0 = ipv.transform( new Vector4f( 0, 0, -1, 1 ) );
		p0.div( p0.w );
		p1 = ipv.transform( new Vector4f( ( float ) ( 2.0 / width ), 0, -1, 1 ) );
		p1.div( p1.w );

		final float pixWidthOnNear = p1.sub( p0 ).length();
		System.out.println( "pixWidthOnNear = " + pixWidthOnNear );


		p0 = ipv.transform( new Vector4f( 0, 0, 1, 1 ) );
		p0.div( p0.w );
		p1 = ipv.transform( new Vector4f( ( float ) ( 2.0 / width ), 0, 1, 1 ) );
		p1.div( p1.w );

		final float pixWidthOnFar = p1.sub( p0 ).length();
		System.out.println( "pixWidthOnFar = " + pixWidthOnFar );

		System.out.println( "pixWidthOnFar / worldStepOnFar = " + pixWidthOnFar / worldStepOnFar );
		System.out.println( "pixWidthOnNear / worldStepOnNear = " + pixWidthOnNear / worldStepOnNear );
	}

	public void setViewportSize( int width, int height )
	{
		int maxNumSteps = width;
		uniformViewportSize.set( width, height, maxNumSteps );
	}

	public void use( GpuContext context )
	{
		prog.use( context );
		prog.bindSamplers( context );
		prog.setUniforms( context );
	}

	static class ConverterSegment
	{
		private final Uniform4f uniformOffset;
		private final Uniform4f uniformScale;

		public ConverterSegment( final SegmentedShader prog, final Segment segment )
		{
			uniformOffset = prog.getUniform4f( segment,"offset" );
			uniformScale = prog.getUniform4f( segment,"scale" );
		}

		public void setData( ColorConverter converter )
		{
			final double fmin = converter.getMin() / 0xffff;
			final double fmax = converter.getMax() / 0xffff;
			final double s = 1.0 / ( fmax - fmin );
			final double o = -fmin * s;

			final int color = converter.getColor().get();
			final double r = ( double ) ARGBType.red( color ) / 255.0;
			final double g = ( double ) ARGBType.green( color ) / 255.0;
			final double b = ( double ) ARGBType.blue( color ) / 255.0;

			uniformOffset.set(
					( float ) ( o * r ),
					( float ) ( o * g ),
					( float ) ( o * b ),
					1f );
			uniformScale.set(
					( float ) ( s * r ),
					( float ) ( s * g ),
					( float ) ( s * b ),
					0f );
		}
	}

	static class VolumeSegment
	{
		private final Uniform3fv uniformBlockScales;
		private final UniformSampler uniformLutSampler;
		private final Uniform3f uniformLutSize;
		private final Uniform3f uniformLutOffset;
		private final UniformMatrix4f uniformIm;
		private final Uniform3f uniformSourcemin;
		private final Uniform3f uniformSourcemax;

		public VolumeSegment( final SegmentedShader prog, final Segment volume )
		{
			uniformBlockScales = prog.getUniform3fv( volume, "blockScales" );
			uniformLutSampler = prog.getUniformSampler( volume,"lutSampler" );
			uniformLutSize = prog.getUniform3f( volume, "lutSize" );
			uniformLutOffset = prog.getUniform3f( volume, "lutOffset" );
			uniformIm = prog.getUniformMatrix4f( volume, "im" );
			uniformSourcemin = prog.getUniform3f( volume,"sourcemin" );
			uniformSourcemax = prog.getUniform3f( volume,"sourcemax" );
		}

		public void setData( VolumeBlocks blocks )
		{
			uniformBlockScales.set( blocks.getLutBlockScales( NUM_BLOCK_SCALES ) );
			uniformLutSampler.set( blocks.getLookupTexture() );
			uniformLutSize.set( blocks.getLutSize() );
			uniformLutOffset.set( blocks.getLutOffset() );
			uniformIm.set( blocks.getIms() );
			uniformSourcemin.set( blocks.getSourceLevelMin() );
			uniformSourcemax.set( blocks.getSourceLevelMax() );
		}
	}
}
