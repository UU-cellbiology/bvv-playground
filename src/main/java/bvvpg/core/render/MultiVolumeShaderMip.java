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

import bdv.tools.brightness.ConverterSetup;
import bvvpg.core.backend.GpuContext;
import bvvpg.core.backend.Texture;
import bvvpg.core.backend.Texture2D;
import bvvpg.core.cache.CacheSpec;
import bvvpg.core.cache.TextureCache;
import bvvpg.core.dither.DitherBuffer;
import bvvpg.core.multires.SourceStacks;
import bvvpg.core.shadergen.Uniform1f;
import bvvpg.core.shadergen.Uniform1i;
import bvvpg.core.shadergen.Uniform2f;
import bvvpg.core.shadergen.Uniform3f;
import bvvpg.core.shadergen.Uniform3fv;
import bvvpg.core.shadergen.Uniform4f;
import bvvpg.core.shadergen.UniformMatrix3f;
import bvvpg.core.shadergen.UniformMatrix4f;
import bvvpg.core.shadergen.UniformSampler;
import bvvpg.core.shadergen.generate.Segment;
import bvvpg.core.shadergen.generate.SegmentTemplate;
import bvvpg.core.shadergen.generate.SegmentType;
import bvvpg.core.shadergen.generate.SegmentedShader;
import bvvpg.core.shadergen.generate.SegmentedShaderBuilder;
import bvvpg.core.util.MatrixMath;
import bvvpg.source.converters.GammaConverterSetup;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class MultiVolumeShaderMip
{
	private static final int NUM_BLOCK_SCALES = 10;

	private final VolumeShaderSignature signature;

	private final boolean useDepthTexture;

	// step size on near plane = pixel_width
	// step size on far plane = degrade * pixel_width
	private double degrade;

	private final SegmentedShader prog;
	private final VolumeSegment[] volumeSegments;
	private final ConverterSegment[] converterSegments;

	// Global caches
	private final UniformSampler[] uniformCaches;
    //it should be the same for all caches
    private final Uniform3f uniformCacheBlockSize;
    private final Uniform3f uniformPaddedBlockSize;
    private final Uniform3f uniformCachePadOffset;
    private final Uniform3f[] uniformCacheSize;

    //Global cache lut-leve table for mutlires volumes
	private final UniformSampler uniformGlobalCacheLut;
	
	private final UniformSampler uniformGlobalColorLut;
	private final Uniform3f uniformGlobalCacheLutSize;
	
	private final UniformMatrix4f uniformIpv;
	private final Uniform2f uniformViewportSize;

	private final Uniform1f uniformNw;
	private final Uniform1f uniformFwnw;
	private final Uniform1f uniformXf;

	private final UniformMatrix4f uniformTransform;
	private final Uniform2f uniformDsp;

	private int viewportWidth;
	private String sceneDepthTextureName;

	/**
	 * Functional interface to be able to use and integrate custom segments. Basically takes a three-parameter
	 * lambda to contain segments, their instances, and the volume id. The lambda is run for each volume id. This
	 * interface then enables custom segments and instances to be connected with each other, e.g. making new custom
	 * per-volume uniforms possible.
	 */
	@FunctionalInterface
	public interface SegmentConsumer
	{
		/**
		 * Runs the lambda that was handed over, offering access to available segments, their instances, and the current
		 * volume index. The lambda is executed for each individual volume id.
		 *
		 * @param segments
		 * @param segmentInstances
		 * @param volumeIndex
		 */
		void accept( Map< SegmentType, SegmentTemplate > segments,
				Map< SegmentType, Segment > segmentInstances, int volumeIndex );
	}

	/**
	 * @param runBeforeBinding
	 * 		<em>(added for use by scenery)</em>
	 * 		This is a lambda (functional interface) called for each volume to be able to establish additional segment
	 * 		binding, for example to connect convert-segment to sample-segment for alpha blending instead of
	 * 		max projection.
	 */
	public MultiVolumeShaderMip( VolumeShaderSignature signature, final boolean useDepthTexture, final double degrade,
			final Map< SegmentType, SegmentTemplate > segments,
			final SegmentConsumer runBeforeBinding,
			final String depthTextureName,
			final List<TextureCache> caches)
	{
		this.signature = signature;
		this.useDepthTexture = useDepthTexture;
		this.degrade = degrade;
		this.sceneDepthTextureName = depthTextureName;

		final int numVolumes = signature.getVolumeSignatures().size();

		// ensure we have all segments we need
		if ( !Arrays.stream( SegmentType.values() ).allMatch( segments::containsKey ) )
			throw new IllegalStateException( "Segments array does not contain all required SegmentTypes." );

		final SegmentedShaderBuilder builder = new SegmentedShaderBuilder();
		final Segment vp = segments.get( SegmentType.VertexShader ).instantiate();
		builder.vertex( vp );

		final SegmentTemplate templateVolBlocks = segments.get( SegmentType.SampleMultiresolutionVolume );
		final SegmentTemplate templateVolSimple = segments.get( SegmentType.SampleVolume );
		final SegmentTemplate templateVolSimpleRGBA = segments.get( SegmentType.SampleRGBAVolume );
		final SegmentTemplate templateConvert = segments.get( SegmentType.Convert );
		final SegmentTemplate templateConvertRGBA = segments.get( SegmentType.ConvertRGBA );
		final SegmentTemplate templateMaxDepth = segments.get( SegmentType.MaxDepth );
		builder.fragment( templateMaxDepth.instantiate() );

		final SegmentTemplate templateMainFp = segments.get( SegmentType.FragmentShader );
		final Segment fp = templateMainFp.instantiate();
		fp.repeat( "vis", numVolumes );

		final SegmentTemplate templateAccumulateMipBlocks = segments.get( SegmentType.AccumulatorMultiresolution );
		final SegmentTemplate templateAccumulateMipSimple = segments.get( SegmentType.Accumulator );

		final Segment[] sampleVolumeSegs = new Segment[ numVolumes ];
		final Segment[] convertSegs = new Segment[ numVolumes ];
		final Segment[] accumulateSegs = new Segment[ numVolumes ];
		for ( int i = 0; i < numVolumes; ++i )
		{
			final HashMap< SegmentType, Segment > instancedSegments = new HashMap<>();
			final VolumeShaderSignature.VolumeSignature volumeSignature = signature.getVolumeSignatures().get( i );
			instancedSegments.put(SegmentType.FragmentShader, fp);

			final Segment accumulate;
			final Segment sampleVolume;
			switch ( volumeSignature.getSourceStackType() )
			{
			case MULTIRESOLUTION:
				accumulate = templateAccumulateMipBlocks.instantiate();
				instancedSegments.put( SegmentType.AccumulatorMultiresolution, accumulate );
				sampleVolume = templateVolBlocks.instantiate();
				instancedSegments.put( SegmentType.SampleMultiresolutionVolume, sampleVolume );
				break;
			case SIMPLE:
				accumulate = templateAccumulateMipSimple.instantiate();
				instancedSegments.put( SegmentType.Accumulator, accumulate );
				sampleVolume = volumeSignature.getPixelType() == VolumeShaderSignature.PixelType.ARGB
						? templateVolSimpleRGBA.instantiate()
						: templateVolSimple.instantiate();
				instancedSegments.put( SegmentType.SampleVolume, sampleVolume );
				break;
			default:
				throw new IllegalArgumentException();
			}

			final Segment convert;
			switch ( volumeSignature.getPixelType() )
			{
			default:
			case SFLOAT:
			case USHORT:
			case UBYTE:
				convert = templateConvert.instantiate();
				instancedSegments.put( SegmentType.Convert, convert );
				break;
			case ARGB:
				convert = templateConvertRGBA.instantiate();
				instancedSegments.put( SegmentType.ConvertRGBA, convert );
				break;
			}

			if ( runBeforeBinding != null )
				runBeforeBinding.accept( segments, instancedSegments, i );

			fp.bind( "intersectBoundingBox", i, sampleVolume );
			fp.bind( "vis", i, accumulate );
			accumulate.bind( "sampleVolume", sampleVolume );
			accumulate.bind( "convert", convert );
			accumulate.bind( "gradientVolume", sampleVolume );
			accumulate.bind( "renderType", convert );
			accumulate.bind( "lightType", convert );

			sampleVolumeSegs[ i ] = sampleVolume;
			convertSegs[ i ] = convert;
			accumulateSegs[ i ] = accumulate;
		}
		fp.insert( "SampleVolume", sampleVolumeSegs );
		fp.insert( "Convert", convertSegs );
		fp.insert( "Accumulate", accumulateSegs );
		
		final int numCaches = caches.size();
		fp.insert( "cachesNumber", SegmentTemplate.fromCode("#define CACHES_NUMBER " + Integer.toString( numCaches )).instantiate() );
		
		builder.fragment( fp );
		prog = builder.build();

		uniformIpv = prog.getUniformMatrix4f( "ipv" );
		uniformViewportSize = prog.getUniform2f( "viewportSize" );
		uniformNw = prog.getUniform1f( "nw" );
		uniformFwnw = prog.getUniform1f( "fwnw" );
		uniformXf = prog.getUniform1f( "xf" );
		
		// Bind caches
		uniformCaches = new UniformSampler[ numCaches  ];
		uniformCacheSize = new Uniform3f[ numCaches ];
		for(int i = 0; i < numCaches; i++ )
		{
			uniformCaches[ i ] = prog.getUniformSampler( "u_Caches[" + Integer.toString( i ) + "]" );
			uniformCaches[ i ].set( caches.get( i ) );
			uniformCacheSize[ i ] = prog.getUniform3f( "cacheSize[" + Integer.toString( i ) + "]" );
			uniformCacheSize[ i ].set( caches.get( i ).texWidth(), caches.get( i ).texHeight(), caches.get( i ).texDepth() );
		}
		
        uniformCacheBlockSize = prog.getUniform3f( "cacheBlockSize" );
        uniformPaddedBlockSize = prog.getUniform3f( "paddedBlockSize" );
        uniformCachePadOffset = prog.getUniform3f( "cachePadOffset" );
    
		final CacheSpec spec = caches.get( 0 ).spec();
		final int[] bs = spec.blockSize();
		final int[] pbs = spec.paddedBlockSize();
		final int[] bo = spec.padOffset();
        uniformCacheBlockSize.set( bs[ 0 ], bs[ 1 ], bs[ 2 ] );
		uniformPaddedBlockSize.set( pbs[ 0 ], pbs[ 1 ], pbs[ 2 ] );
		uniformCachePadOffset.set( bo[ 0 ], bo[ 1 ], bo[ 2 ] );	
		
		uniformGlobalCacheLut = prog.getUniformSampler( "globalCacheLut" );
		uniformGlobalCacheLutSize = prog.getUniform3f( "globalCacheLutSize" );

		uniformGlobalColorLut = prog.getUniformSampler( "globalColorLutArray" );

		
		volumeSegments = new VolumeSegment[ numVolumes ];
		converterSegments = new ConverterSegment[ numVolumes ];
		for ( int i = 0; i < numVolumes; ++i )
		{
			final VolumeShaderSignature.VolumeSignature volumeSignature = signature.getVolumeSignatures().get( i );
			switch ( volumeSignature.getSourceStackType() )
			{
			case SIMPLE:
				volumeSegments[ i ] = new VolumeSimpleSegment( prog, sampleVolumeSegs[ i ] );
				break;
			case MULTIRESOLUTION:
				volumeSegments[ i ] = new VolumeBlocksSegment( prog, sampleVolumeSegs[ i ] );
				break;
				default:
			}
			converterSegments[ i ] = new ConverterSegment( prog, convertSegs[ i ], sampleVolumeSegs[ i ], volumeSignature.getPixelType() );
		}

		uniformTransform = prog.getUniformMatrix4f( "transform" );
		uniformDsp = prog.getUniform2f( "dsp" );

		uniformTransform.set( new Matrix4f() );
		uniformDsp.set( new Vector2f() );

//		final StringBuilder vertexShaderCode = prog.getVertexShaderCode();
//		System.out.println( "vertexShaderCode = " + vertexShaderCode );
//		System.out.println( "\n\n--------------------------------\n\n" );
//		final StringBuilder fragmentShaderCode = prog.getFragmentShaderCode();
//		System.out.println( "fragmentShaderCode = " + fragmentShaderCode );
//		System.out.println( "\n\n--------------------------------\n\n" );
	}

	public static Map< SegmentType, SegmentTemplate > getDefaultSegments( boolean useDepthTexture )
	{
		final HashMap< SegmentType, SegmentTemplate > segments = new HashMap<>();

		segments.put( SegmentType.SampleMultiresolutionVolume, new SegmentTemplate(
				"sample_volume_blocks.frag",
				 "im", "itvm", "sourcemin", "sourcemax","voxelInterpolation", 
				"clipactive", "clipmin", "clipmax", "cliptransform",
				"intersectBoundingBox",
				"blockScales", "lutSize", "lutOffset", "sampleVolume", "cacheType", "cacheLutZOffset",
				"sampleRaw", "gradientVolume" ) );
		segments.put( SegmentType.SampleVolume, new SegmentTemplate(
				"sample_volume_simple.frag",
				"im", "itvm", "sourcemax", "voxelInterpolation",
				"clipactive", "clipmin", "clipmax", "cliptransform",
				"intersectBoundingBox",
				"volume", "sampleVolume",
				"sampleRaw", "gradientVolume" ) );
		segments.put( SegmentType.SampleRGBAVolume, new SegmentTemplate(
				"sample_volume_simple_rgba.frag",
				"im", "sourcemax", "voxelInterpolation",
				"clipactive", "clipmin", "clipmax", "cliptransform",
				"intersectBoundingBox",
				"volume", "sampleVolume" ) ); 				
		segments.put( SegmentType.Convert, new SegmentTemplate(
				"convert.frag",
				"convert", "offset", "scale", "gamma", "alphagamma",
				"renderType", "lightType",
				"sizeColorLut", "colorLutLayer" ) );
		segments.put( SegmentType.ConvertRGBA, new SegmentTemplate(
				"convert_rgba.frag",
				"convert", "offset", "scale", "gamma", "alphagamma") );
		segments.put( SegmentType.MaxDepth, new SegmentTemplate(
				useDepthTexture ? "maxdepthtexture.frag" : "maxdepthone.frag" ) );
		segments.put( SegmentType.VertexShader, new SegmentTemplate( "multi_volume.vert" ) );
		segments.put( SegmentType.FragmentShader, new SegmentTemplate(
				"multi_volume.frag", "cachesNumber",
				"intersectBoundingBox", "vis", "SampleVolume", "Convert", "Accumulate" ) );
		segments.put( SegmentType.AccumulatorMultiresolution, new SegmentTemplate(
				"accumulate_blocks.frag",
				"vis", "sampleVolume", "convert", "gradientVolume",
				"renderType", "lightType" ) );
		segments.put( SegmentType.Accumulator, new SegmentTemplate(
				"accumulate_simple.frag",
				"vis", "sampleVolume", "convert", "gradientVolume", 
				"renderType", "lightType" ) );

		return segments;
	}

	public MultiVolumeShaderMip( VolumeShaderSignature signature, final boolean useDepthTexture, final double degrade, final List<TextureCache> caches )
	{
		this( signature, useDepthTexture, degrade, getDefaultSegments( useDepthTexture ), null, "sceneDepth", caches);
	}

	public void setDepthTexture( Texture2D depth )
	{
		if ( !useDepthTexture )
			throw new UnsupportedOperationException();

		prog.getUniformSampler( sceneDepthTextureName ).set( depth );
	}

	public void setDepthTextureName( String name )
	{
		sceneDepthTextureName = name;
	}
	
	public void setGlobalCacheLutTexture(final GlobalCacheLutTexture globalLutTexture)
	{
		 uniformGlobalCacheLut.set( globalLutTexture );
		 uniformGlobalCacheLutSize.set( globalLutTexture.getSize3f() );
	}
	
	public void setGlobalColorLutTexture(final Texture colorLutTexture)
	{
		uniformGlobalColorLut.set( colorLutTexture );
	}

	public void setConverter( int index, ConverterSetup converter, final int globalLUTLayer )
	{
		//System.out.println("converter index "+ index);
		converterSegments[ index ].setData( converter, globalLUTLayer );
	}

	/**
	 * Set uniform {@code name} to the given {@code value}, where the uniform
	 * type is inferred from the type of {@code value}.
	 * <p>
	 * <em>(added for use by scenery)</em>
	 *
	 * @param index
	 * 		index of the volume
	 * @param name
	 * 		uniform name
	 * @param value
	 * 		value to set for uniform {@code name}
	 */
	public void setUniform( final int index, final String name, final Object value)
	{
		prog.setUniformValueByType( volumeSegments[ index ].volume, name, value );
	}

	/**
	 * Set uniform array {@code name} to the given {@code value}, where the
	 * uniform type is inferred from the type of {@code value}. The {@code
	 * value} type currently must be either {@code float[]} or {@code int[]} and
	 * the value represents vectors of length {@code elementSize} packed into a
	 * single array.
	 * <p>
	 * <em>(added for use by scenery)</em>
	 *
	 * @param index
	 * 		index of the volume
	 * @param name
	 * 		uniform name
	 * @param value
	 * 		value to set for uniform {@code name}
	 */
	public void setUniform( final int index, final String name, final int elementSize, final Object value )
	{
		prog.setUniformValueByType( volumeSegments[ index ].volume, name, elementSize, value );
	}

	public void setVolume( int index, VolumeBlocks volume, final RenderData renderData, final int cacheLutZOffset )
	{
		final VolumeShaderSignature.VolumeSignature vs = signature.getVolumeSignatures().get( index );
		if ( vs.getSourceStackType() != SourceStacks.SourceStackType.MULTIRESOLUTION )
			throw new IllegalArgumentException();

		( ( VolumeBlocksSegment ) volumeSegments[ index ] ).setData( volume, renderData, cacheLutZOffset );
	}

	public void setVolume( int index, SimpleVolume volume, final RenderData renderData )
	{
		final VolumeShaderSignature.VolumeSignature vs = signature.getVolumeSignatures().get( index );
		if ( vs.getSourceStackType() != SourceStacks.SourceStackType.SIMPLE )
			throw new IllegalArgumentException();

		( ( VolumeSimpleSegment ) volumeSegments[ index ] ).setData( volume, renderData );
	}


	public void setDither( DitherBuffer dither, int step )
	{
		uniformViewportSize.set( dither.effectiveViewportWidth(), dither.effectiveViewportHeight() );
		uniformTransform.set( dither.ndcTransform( step ) );
		uniformDsp.set( dither.fragShift( step ) );
	}

	/**
	 * Note that this will only take effect after {@link #setProjectionViewMatrix(Matrix4fc, double)}
	 * <p>
	 * <em>(added for use by scenery)</em>
	 */
	public void setDegrade( Double farPlaneStepSizeDegradation )
	{
		degrade = farPlaneStepSizeDegradation;
	}

	/**
	 * @param minWorldVoxelSize pass {@code 0} if unknown.
	 */
	public void setProjectionViewMatrix( final Matrix4fc pv, final double minWorldVoxelSize )
	{
		final Matrix4f ipv = pv.invert( new Matrix4f() );
		final float dx = ( float ) ( 2.0 / viewportWidth );

		final Vector4f a = ipv.transform( new Vector4f( 0, 0, -1, 1 ) );
		final Vector4f c = ipv.transform( new Vector4f( 0, 0,  1, 1 ) );
		final Vector4f b = ipv.transform( new Vector4f( 0, 0,  0, 1 ) );
		final Vector4f adx = ipv.transform( new Vector4f( dx, 0, -1, 1 ) );
		final Vector4f cdx = ipv.transform( new Vector4f( dx, 0,  1, 1 ) );
		a.div( a.w() );
		b.div( b.w() );
		c.div( c.w() );
		adx.div( adx.w() );
		cdx.div( cdx.w() );

		final double sNear = Math.max( adx.sub( a ).length(), minWorldVoxelSize );
		final double sFar = Math.max( cdx.sub( c ).length(), minWorldVoxelSize );
		final double ac = c.sub( a ).length();
		final double scale = 1.0 / ac;
		final double nw = sNear * scale;
		final double fw = degrade * sFar * scale;
		final double ab = b.sub( a, new Vector4f() ).length();
		final double f = ab / ac;

		uniformIpv.set( ipv );
		uniformNw.set( ( float ) nw );
		uniformFwnw.set( ( float ) ( fw - nw ) );
		uniformXf.set( ( float ) f );
	}

	public void setViewportWidth( int width )
	{
		viewportWidth = width;
	}

	public void setEffectiveViewportSize( int width, int height )
	{
		uniformViewportSize.set( width, height );
	}

	public void use( GpuContext context )
	{
		prog.use( context );
	}

	public void bindSamplers( GpuContext context )
	{
		prog.bindSamplers( context );
	}

	public void setUniforms( GpuContext context )
	{
		prog.setUniforms( context );
	}

	static class ConverterSegment
	{
		private final Uniform4f uniformOffset;
		private final Uniform4f uniformScale;
		private final Uniform1f uniformGamma;
		private final Uniform1f uniformGammaAlpha;
		private final Uniform1i uniformSizeLUT;
		private final Uniform1i uniformRenderType;
		private final Uniform1f uniformLightType;
		private final Uniform1i uniformClipActive;
		private final Uniform1i uniformVoxelInterpolation;
		private final Uniform3f uniformClipMin;
		private final Uniform3f uniformClipMax;
		private final UniformMatrix4f uniformClipTransform;
		private final Uniform1f uniformLutLayer;
		
		private final VolumeShaderSignature.PixelType pixelType;
		private final double rangeScale;

		public ConverterSegment( final SegmentedShader prog, final Segment segmentConv, final Segment segmentVol, final VolumeShaderSignature.PixelType pixelType )
		{
			uniformOffset = prog.getUniform4f( segmentConv, "offset" );
			uniformScale = prog.getUniform4f( segmentConv, "scale" );
			uniformGamma = prog.getUniform1f( segmentConv, "gamma" );
			uniformGammaAlpha = prog.getUniform1f( segmentConv, "alphagamma" );
			uniformRenderType = prog.getUniform1i( segmentConv, "renderType" );
			uniformLightType = prog.getUniform1f( segmentConv, "lightType" );
			uniformVoxelInterpolation = prog.getUniform1i( segmentVol, "voxelInterpolation" );
			uniformSizeLUT = prog.getUniform1i( segmentConv, "sizeColorLut" );
			uniformClipActive = prog.getUniform1i( segmentVol, "clipactive" );
			uniformClipMin = prog.getUniform3f( segmentVol, "clipmin" );
			uniformClipMax = prog.getUniform3f( segmentVol, "clipmax" );
			uniformClipTransform = prog.getUniformMatrix4f( segmentVol, "cliptransform" );
			uniformLutLayer = prog.getUniform1f( segmentConv, "colorLutLayer" );
			this.pixelType = pixelType;
			switch ( pixelType )
			{
			default:
			case USHORT:
				rangeScale = 0xffff;
				break;
			case UBYTE:
			case ARGB:
				rangeScale = 0xff;
				break;
			case SFLOAT:
				rangeScale = 1.0;
				break;
			}

		}

		public void setData( ConverterSetup converter, final int globalLUTLayer )
		{
			GammaConverterSetup gc = (GammaConverterSetup)converter;
			final double fmin = gc.getDisplayRangeMin() / rangeScale;
			final double fmax = gc.getDisplayRangeMax() / rangeScale;
			double fminA = fmin;
			double fmaxA = fmax;

			uniformClipActive.set( 0 );
			uniformGamma.set( 1.0f / (float)gc.getDisplayGamma() );
			uniformGammaAlpha.set( 1.0f / (float)gc.getAlphaGamma() );
			uniformRenderType.set( gc.getRenderType() );
			uniformLightType.set( gc.getLightingType() );
			uniformVoxelInterpolation.set(gc.getVoxelRenderInterpolation());
			fminA = gc.getAlphaRangeMin() / rangeScale;
			fmaxA = gc.getAlphaRangeMax() / rangeScale;
			if( gc.getClipState() != 0 && gc.getClipInterval() != null )
			{
				uniformClipActive.set(gc.getClipState());
				uniformClipMin.set(gc.getClipInterval(),bvvpg.core.shadergen.MinMax.MIN);
				uniformClipMax.set(gc.getClipInterval(),bvvpg.core.shadergen.MinMax.MAX);	
				final AffineTransform3D t = new AffineTransform3D();
				gc.getClipTransform( t );
				t.set( t.inverse() );
				uniformClipTransform.set( MatrixMath.affine( t, new Matrix4f() ) );
			}

			final double s = 1.0 / ( fmax - fmin );
			final double o = -fmin * s;
			final double sA = 1.0 / ( fmaxA - fminA );
			final double oA = -fminA * sA;

			if ( pixelType == VolumeShaderSignature.PixelType.ARGB )
			{
				uniformSizeLUT.set( 0 );
				uniformOffset.set( ( float ) o, ( float ) o, ( float ) o, ( float ) o );
				uniformScale.set( ( float ) s, ( float ) s, ( float ) s, ( float ) s );
			}
			else
			{

				final int nLUTSize = gc.getLUTSize();
				uniformSizeLUT.set( nLUTSize );
				if(nLUTSize > 0)
				{
					uniformLutLayer.set( globalLUTLayer );
					uniformOffset.set(
							( float ) ( o * 1.0 ),
							( float ) ( o * 1.0 ),
							( float ) ( o * 1.0 ),
							( float ) ( oA ) );
					uniformScale.set(
							( float ) ( s * 1.0 ),
							( float ) ( s * 1.0 ),
							( float ) ( s * 1.0 ),
							( float ) ( sA ) );
				}
				else
				{				
					uniformLutLayer.set( 0 );				
					final int color = gc.getColor().get();
					final double r = ARGBType.red( color ) / 255.0;
					final double g = ARGBType.green( color ) / 255.0;
					final double b = ARGBType.blue( color ) / 255.0;

					uniformOffset.set(
							( float ) ( o * r ),
							( float ) ( o * g ),
							( float ) ( o * b ),
							( float ) ( oA ) );
					uniformScale.set(
							( float ) ( s * r ),
							( float ) ( s * g ),
							( float ) ( s * b ),
							( float ) ( sA ) );
				}
			}
		}
	}

	static abstract class VolumeSegment
	{
		final Segment volume;

		public VolumeSegment( final Segment volume )
		{
			this.volume = volume;
		}
	}

	static class VolumeBlocksSegment extends VolumeSegment
	{
		private final UniformMatrix4f uniformIm;
		private final UniformMatrix3f uniformItvm;
		private final Uniform3f uniformSourcemin;
		private final Uniform3f uniformSourcemax;
		private final Uniform1i uniformCacheType;
		private final Uniform3fv uniformBlockScales;
		private final Uniform3f uniformLutOffset;
		private final Uniform3f uniformLutSize;
		private final Uniform1i uniformCacheLutZOffset;

		public VolumeBlocksSegment( final SegmentedShader prog, final Segment volume)
		{
			super( volume );			

			uniformIm = prog.getUniformMatrix4f( volume, "im" );
			uniformItvm = prog.getUniformMatrix3f( volume, "itvm" );
			uniformSourcemin = prog.getUniform3f( volume, "sourcemin" );
			uniformSourcemax = prog.getUniform3f( volume, "sourcemax" );
			
			uniformCacheType = prog.getUniform1i( volume, "cacheType" );
			uniformBlockScales = prog.getUniform3fv( volume, "blockScales" );
			uniformLutSize = prog.getUniform3f( volume, "lutSize" );
			uniformLutOffset = prog.getUniform3f( volume, "lutOffset" );
			uniformCacheLutZOffset = prog.getUniform1i( volume, "cacheLutZOffset" );

		}

		public void setData( VolumeBlocks blocks, final RenderData renderData, final int cacheLutZOffset)
		{

			uniformIm.set( blocks.getIms() );
			final Matrix4f vtm = renderData.getCamview().mul( blocks.getIms(), new Matrix4f() );
			final Matrix4f itvm = vtm.invert( new Matrix4f() ).transpose();
			uniformItvm.set( itvm.get3x3( new Matrix3f() ) );
			uniformSourcemin.set( blocks.getSourceLevelMin() );
			uniformSourcemax.set( blocks.getSourceLevelMax() );
			switch(blocks.getTextureCache().texInternalFormat())
			{
			case R8:
				uniformCacheType.set( 0 );
				break;
			case R16:
				uniformCacheType.set( 1 );
				break;
			case R32F:
				uniformCacheType.set( 2 );
				break;
			default:
			}
			uniformBlockScales.set( blocks.getLutBlockScales( NUM_BLOCK_SCALES ) );
			final LookupTextureARGB lut = blocks.getLookupTexture();
			uniformLutOffset.set( lut.getOffset3f() );
			uniformLutSize.set( lut.getSize3f() );
			uniformCacheLutZOffset.set( cacheLutZOffset );
		}
	}

	static class VolumeSimpleSegment extends VolumeSegment
	{
		private final UniformSampler uniformVolumeSampler;
		private final UniformMatrix4f uniformIm;
		private final UniformMatrix3f uniformItvm;
		private final Uniform3f uniformSourcemax;

		public VolumeSimpleSegment( final SegmentedShader prog, final Segment volume )
		{
			super( volume );
			uniformVolumeSampler = prog.getUniformSampler( volume, "volume" );
			uniformIm = prog.getUniformMatrix4f( volume, "im" );
			uniformItvm = prog.getUniformMatrix3f( volume, "itvm" );
			uniformSourcemax = prog.getUniform3f( volume, "sourcemax" );
		}

		public void setData( SimpleVolume volume, final RenderData renderData )
		{
			uniformVolumeSampler.set( volume.getVolumeTexture() );
			uniformIm.set( volume.getIms() );
			final Matrix4f vtm = renderData.getCamview().mul( volume.getIms(), new Matrix4f() );
			final Matrix4f itvm = vtm.invert( new Matrix4f() ).transpose();
			uniformItvm.set( itvm.get3x3( new Matrix3f() ) );
			uniformSourcemax.set( volume.getSourceMax() );
		}
	}
}
