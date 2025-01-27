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
package bvvpg.core.render;

import static bvvpg.core.backend.Texture.InternalFormat.R16;
import static bvvpg.core.multires.SourceStacks.SourceStackType.MULTIRESOLUTION;
import static bvvpg.core.multires.SourceStacks.SourceStackType.SIMPLE;
import static bvvpg.core.render.VolumeRenderer.RepaintType.DITHER;
import static bvvpg.core.render.VolumeRenderer.RepaintType.FULL;
import static bvvpg.core.render.VolumeRenderer.RepaintType.LOAD;
import static bvvpg.core.render.VolumeRenderer.RepaintType.NONE;
import static bvvpg.core.render.VolumeShaderSignature.PixelType.ARGB;
import static bvvpg.core.render.VolumeShaderSignature.PixelType.UBYTE;
import static bvvpg.core.render.VolumeShaderSignature.PixelType.USHORT;
import static com.jogamp.opengl.GL.GL_ALWAYS;
import static com.jogamp.opengl.GL.GL_BLEND;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_ONE_MINUS_SRC_ALPHA;
import static com.jogamp.opengl.GL.GL_SRC_ALPHA;
import static com.jogamp.opengl.GL.GL_UNPACK_ALIGNMENT;

import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL2ES3;
import com.jogamp.opengl.GL3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import org.joml.Matrix4f;

import bdv.tools.brightness.ConverterSetup;
import bvvpg.core.backend.jogl.JoglGpuContext;
import bvvpg.core.blocks.TileAccess;
import bvvpg.core.cache.CacheSpec;
import bvvpg.core.cache.FillTask;
import bvvpg.core.cache.PboChain;
import bvvpg.core.cache.ProcessFillTasks;
import bvvpg.core.cache.TextureCache;
import bvvpg.core.dither.DitherBuffer;
import bvvpg.core.multires.MultiResolutionStack3D;
import bvvpg.core.multires.SimpleStack3D;
import bvvpg.core.multires.Stack3D;
import bvvpg.core.offscreen.OffScreenFrameBufferWithDepth;
import bvvpg.core.render.VolumeShaderSignature.VolumeSignature;
import bvvpg.core.util.DefaultQuad;
import bvvpg.source.converters.GammaConverterSetup;

public class VolumeRenderer
{
	private final int renderWidth;

	private final int renderHeight;

	// ... RenderState ...
//	final List< MultiResolutionStack3D< VolatileUnsignedShortType > > renderStacks;
//	final List< ConverterSetup > renderConverters;
//	final Matrix4f pv,

	// ... repainting ...

	public enum RepaintType
	{
		NONE,
		SCENE,
		DITHER,
		LOAD,
		FULL;
	}

	private static class Repaint
	{
		RepaintType type;

		Repaint()
		{
			this.type = NONE;
		}

		void request( final RepaintType type )
		{
			if ( this.type.ordinal() < type.ordinal() )
				this.type = type;
		}
	}

	private final Repaint nextRequestedRepaint = new Repaint();

	private int ditherStep = 0;

	private int targetDitherSteps = 0;

	/**
	 * Currently used volume shader program.
	 * This is used when redrawing without changing {@code RenderState}.
	 */
	private MultiVolumeShaderMip progvol;

	// ... dithering ...

	private final DitherBuffer dither;

	private final int numDitherSteps;

	// ... gpu cache ...
	// TODO This could be packaged into one class and potentially shared between renderers?
	private final CacheSpec cacheSpec; // TODO remove

	private final TextureCache textureCache;

	private final PboChain pboChain;

	private final ForkJoinPool forkJoinPool;

	/**
	 * Shader programs for rendering multiple cached and/or simple volumes.
	 */
	private final HashMap< VolumeShaderSignature, MultiVolumeShaderMip > progvols;

	/**
	 * VolumeBlocks for one volume each.
	 * These have associated lookup textures, so we keep them around and reuse them so that we do not create new textures all the time.
	 * And deleting textures is not yet in the backend... (TODO)
	 */
	private final ArrayList< VolumeBlocks > volumes;

	/**
	 * provides SimpleVolumes for SimpleStacks.
	 */
	private final SimpleStackManager simpleStackManager = new DefaultSimpleStackManager();
	
	/** handles LUTs (GPU upload and check if they are expired)
	 * for converter Setups **/
	private final SimpleLUTTextureManager simpleLUTManager = new SimpleLUTTextureManager();

	private final DefaultQuad quad;

//	private boolean bShowInfo = true;


	public VolumeRenderer(
			final int renderWidth,
			final int renderHeight,
			final int ditherWidth,
			final int ditherStep,
			final int numDitherSamples,
			final int[] cacheBlockSize,
			final int maxCacheSizeInMB )
	{
		this.renderWidth = renderWidth;
		this.renderHeight = renderHeight;

		// set up gpu cache
		// TODO This could be packaged into one class and potentially shared between renderers?
		cacheSpec = new CacheSpec( R16, cacheBlockSize );
		final int[] cacheGridDimensions = TextureCache.findSuitableGridSize( cacheSpec, maxCacheSizeInMB );
		textureCache = new TextureCache( cacheGridDimensions, cacheSpec );
		pboChain = new PboChain( 5, 100, textureCache );
		final int parallelism = Math.max( 1, Runtime.getRuntime().availableProcessors() / 2 );
		forkJoinPool = new ForkJoinPool( parallelism );



		// set up dither buffer (or null)
		if ( ditherWidth <= 1 )
		{
			dither = null;
			numDitherSteps = 1;
		}
		else
		{
			dither = new DitherBuffer( renderWidth, renderHeight, ditherWidth, ditherStep, numDitherSamples );
			numDitherSteps = dither.numSteps();
		}


		volumes = new ArrayList<>();
		progvols = new HashMap<>();
		progvols.put( new VolumeShaderSignature( Collections.emptyList() ), null );
		quad = new DefaultQuad();
	}

	/**
	 * Make sure that we can deal with at least {@code n} blocked volumes.
	 * I.e., add VolumeBlock luts if necessary.
	 *
	 * @param n
	 * 		number of blocked volumes that shall be rendered
	 */
	private void needAtLeastNumBlockVolumes( final int n )
	{
		while ( volumes.size() < n )
			volumes.add( new VolumeBlocks( textureCache ) );
	}

	private MultiVolumeShaderMip createMultiVolumeShader( final VolumeShaderSignature signature )
	{
		final MultiVolumeShaderMip progvol = new MultiVolumeShaderMip( signature, true, 1.0 );
		progvol.setTextureCache( textureCache );
		return progvol;
	}

	public void init( final GL3 gl )
	{
		gl.glPixelStorei( GL_UNPACK_ALIGNMENT, 1 );
	}

	/**
	 * @param maxAllowedStepInVoxels
	 * 		Set to {@code 0} to base step size purely on pixel width of render target
	 */
	// TODO rename paint() like in MultiResolutionRenderer?
	public RepaintType draw(
			final GL3 gl,
			final RepaintType type,
			final OffScreenFrameBufferWithDepth sceneBuf,
			final List< Stack3D< ? > > renderStacks,
			final List< ConverterSetup > renderConverters,
			final Matrix4f pv,
			final int maxRenderMillis,
			final double maxAllowedStepInVoxels )
	{
		final long maxRenderNanoTime = System.nanoTime() + 1_000_000L * maxRenderMillis;
		final JoglGpuContext context = JoglGpuContext.get( gl );
		nextRequestedRepaint.type = NONE;
		if ( renderStacks.isEmpty() )
			return nextRequestedRepaint.type;

		gl.glEnable( GL_DEPTH_TEST );
		gl.glDepthFunc( GL_ALWAYS );
		
//		if(bShowInfo)
//		{
//	
//			final int[] val = new int[1];
//			
//			gl.glGetIntegerv( GL2ES2.GL_MAX_TEXTURE_IMAGE_UNITS, val, 0 );
//			System.out.println("GL_MAX_TEXTURE_IMAGE_UNITS "+ Integer.toString( val[0]));
//			gl.glGetIntegerv( GL2ES2.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS, val, 0 );
//			System.out.println("GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS "+ Integer.toString( val[0]));
//			gl.glGetIntegerv( GL2ES3.GL_MAX_FRAGMENT_UNIFORM_BLOCKS, val, 0 );
//			System.out.println("GL_MAX_FRAGMENT_UNIFORM_BLOCKS "+ Integer.toString(val[0]));
//			gl.glGetIntegerv( GL2ES3.GL_MAX_COMBINED_UNIFORM_BLOCKS, val, 0 );
//			System.out.println("GL_MAX_COMBINED_UNIFORM_BLOCKS "+ Integer.toString(val[0]));
//			gl.glGetIntegerv(  GL2ES3.GL_MAX_FRAGMENT_UNIFORM_COMPONENTS, val, 0 );
//			System.out.println("GL_MAX_FRAGMENT_UNIFORM_COMPONENTS "+ Integer.toString(val[0]));
//
//			bShowInfo = false;
//		}
		

		if ( type == FULL )
		{
			ditherStep = 0;
			targetDitherSteps = numDitherSteps;
		}
		else if ( type == LOAD )
		{
			targetDitherSteps = ditherStep + numDitherSteps;
		}

		if ( type == FULL || type == LOAD )
		{
			final List< VolumeSignature > volumeSignatures = new ArrayList<>();
			final List< MultiResolutionStack3D< ? > > multiResStacks = new ArrayList<>();
			for ( int i = 0; i < renderStacks.size(); i++ )
			{
				final Stack3D< ? > stack = renderStacks.get( i );
				if ( stack instanceof MultiResolutionStack3D )
				{
					if ( !TileAccess.isSupportedType( stack.getType() ) )
						throw new IllegalArgumentException();
					multiResStacks.add( ( MultiResolutionStack3D< ? > ) stack );
					volumeSignatures.add( new VolumeSignature( MULTIRESOLUTION, USHORT ) );
				}
				else if ( stack instanceof SimpleStack3D )
				{
					final Object pixelType = stack.getType();
					if ( pixelType instanceof UnsignedShortType )
						volumeSignatures.add( new VolumeSignature( SIMPLE, USHORT ) );
					else if ( pixelType instanceof UnsignedByteType )
						volumeSignatures.add( new VolumeSignature( SIMPLE, UBYTE ) );
					else if ( pixelType instanceof ARGBType )
						volumeSignatures.add( new VolumeSignature( SIMPLE, ARGB ) );
					else
						throw new IllegalArgumentException();
				}
				else
					throw new IllegalArgumentException();
			}
			needAtLeastNumBlockVolumes( multiResStacks.size() );
			updateBlocks( context, multiResStacks, pv );

			double minWorldVoxelSize = Double.POSITIVE_INFINITY;
			progvol = progvols.computeIfAbsent( new VolumeShaderSignature( volumeSignatures ), this::createMultiVolumeShader );
			if ( progvol != null )
			{
				int mri = 0;
				for ( int i = 0; i < renderStacks.size(); i++ )
				{
					final GammaConverterSetup gc = ( GammaConverterSetup ) renderConverters.get( i );
					
					simpleLUTManager.processTextureLUT( context, gc );
					progvol.setConverter( i, gc );
					if ( volumeSignatures.get( i ).getSourceStackType() == MULTIRESOLUTION )
					{
						final VolumeBlocks volume = volumes.get( mri++ );
						progvol.setVolume( i, volume );
						minWorldVoxelSize = Math.min( minWorldVoxelSize, volume.getBaseLevelVoxelSizeInWorldCoordinates() );
					}
					else
					{
						final SimpleStack3D< ? > simpleStack3D = ( SimpleStack3D< ? > ) renderStacks.get( i );
						final SimpleVolume volume = simpleStackManager.getSimpleVolume( context, simpleStack3D );
						progvol.setVolume( i, volume );
						minWorldVoxelSize = Math.min( minWorldVoxelSize, volume.getVoxelSizeInWorldCoordinates() );
					}
				}
				progvol.setDepthTexture( sceneBuf.getDepthTexture() );
				progvol.setViewportWidth( renderWidth );
				progvol.setProjectionViewMatrix( pv, maxAllowedStepInVoxels * minWorldVoxelSize );
			}

			simpleStackManager.freeUnusedSimpleVolumes( context );
			simpleLUTManager.freeUnusedLUTs( context );
		}

		if ( progvol != null )
		{
			if ( dither != null )
			{
				if ( ditherStep != targetDitherSteps )
				{
					dither.bind( gl );
					progvol.use( context );
					progvol.bindSamplers( context );
					gl.glDisable( GL_BLEND );
					while ( ditherStep < targetDitherSteps )
					{
						progvol.setDither( dither, ditherStep % numDitherSteps );
						progvol.setUniforms( context );
						quad.draw( gl );
						gl.glFinish();
						++ditherStep;
						if ( System.nanoTime() > maxRenderNanoTime )
							break;
					}
					dither.unbind( gl );
				}

				gl.glEnable( GL_BLEND );
				gl.glBlendFunc( GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA );
				final int stepsCompleted = Math.min( ditherStep, numDitherSteps );
				dither.dither( gl, stepsCompleted, renderWidth, renderHeight );
//				dither.getStitchBuffer().drawQuad( gl );
//				dither.getDitherBuffer().drawQuad( gl );

				if ( ditherStep != targetDitherSteps )
					nextRequestedRepaint.request( DITHER );
			}
			else // no dithering
			{
				gl.glEnable( GL_BLEND );
				gl.glBlendFunc( GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA );
				progvol.use( context );
				progvol.bindSamplers( context );
				progvol.setEffectiveViewportSize( renderWidth, renderHeight );
				progvol.setUniforms( context );
				quad.draw( gl );
			}
		}

		return nextRequestedRepaint.type;
	}

	static class VolumeAndTasks
	{
		private final List< FillTask > tasks;
		private final VolumeBlocks volume;
		private final int maxLevel;

		int numTasks()
		{
			return tasks.size();
		}

		VolumeAndTasks( final List< FillTask > tasks, final VolumeBlocks volume, final int maxLevel )
		{
			this.tasks = new ArrayList<>( tasks );
			this.volume = volume;
			this.maxLevel = maxLevel;
		}
	}

	private void updateBlocks(
			final JoglGpuContext context,
			final List< ? extends MultiResolutionStack3D< ? > > multiResStacks,
			final Matrix4f pv )
	{
		final List< VolumeAndTasks > tasksPerVolume = new ArrayList<>();
		int numTasks = 0;
		for ( int i = 0; i < multiResStacks.size(); i++ )
		{
			final MultiResolutionStack3D< ? > stack = multiResStacks.get( i );
			final VolumeBlocks volume = volumes.get( i );
			volume.init( stack, renderWidth, pv );
			final List< FillTask > tasks = volume.getFillTasks();
			numTasks += tasks.size();
			tasksPerVolume.add( new VolumeAndTasks( tasks, volume, stack.resolutions().size() - 1 ) );
		}

		A:
		while ( numTasks > textureCache.getMaxNumTiles() )
		{
			tasksPerVolume.sort( Comparator.comparingInt( VolumeAndTasks::numTasks ).reversed() );
			for ( final VolumeAndTasks vat : tasksPerVolume )
			{
				final int baseLevel = vat.volume.getBaseLevel();
				if ( baseLevel < vat.maxLevel )
				{
					vat.volume.setBaseLevel( baseLevel + 1 );
					numTasks -= vat.numTasks();
					vat.tasks.clear();
					vat.tasks.addAll( vat.volume.getFillTasks() );
					numTasks += vat.numTasks();
					continue A;
				}
			}
			break;
		}

		final ArrayList< FillTask > fillTasks = new ArrayList<>();
		for ( final VolumeAndTasks vat : tasksPerVolume )
			fillTasks.addAll( vat.tasks );
		if ( fillTasks.size() > textureCache.getMaxNumTiles() )
			fillTasks.subList( textureCache.getMaxNumTiles(), fillTasks.size() ).clear();

		try
		{
			ProcessFillTasks.parallel( textureCache, pboChain, context, forkJoinPool, fillTasks );
		}
		catch ( final InterruptedException e )
		{
			e.printStackTrace();
		}

		boolean needsRepaint = false;
		final int timestamp = textureCache.nextTimestamp();
		for ( int i = 0; i < multiResStacks.size(); i++ )
		{
			final VolumeBlocks volume = volumes.get( i );
			final boolean complete = volume.makeLut( timestamp );
			if ( !complete )
				needsRepaint = true;
			volume.getLookupTexture().upload( context );
		}

		if ( needsRepaint )
			nextRequestedRepaint.request( LOAD );
	}
}
