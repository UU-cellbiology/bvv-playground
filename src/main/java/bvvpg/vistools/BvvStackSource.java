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
package bvvpg.vistools;

import bdv.tools.brightness.ConverterSetup;
import bdv.tools.brightness.MinMaxGroup;
import bdv.util.Bounds;
import bdv.viewer.ConverterSetupBounds;
import bdv.viewer.ConverterSetups;
import bdv.viewer.SourceAndConverter;
import bvvpg.core.multires.SourceStacks;
import bvvpg.source.converters.ConverterSetupBoundsAlpha;
import bvvpg.source.converters.ConverterSetupBoundsGamma;
import bvvpg.source.converters.ConverterSetupBoundsGammaAlpha;
import bvvpg.source.converters.ConverterSetupsPG;
import bvvpg.source.converters.GammaConverterSetup;
import bvvpg.source.converters.MinMaxGroupPG;
import bvvpg.source.converters.SetupAssignmentsPG;

import java.awt.image.IndexColorModel;
import java.util.HashSet;
import java.util.List;

import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;

public class BvvStackSource< T > extends BvvSource
{
	private final T type;

	private final List< ConverterSetup > converterSetups;

	private final List< SourceAndConverter< T > > sources;

	protected BvvStackSource(
			final BvvHandle bvv,
			final int numTimepoints,
			final T type,
			final List< ConverterSetup > converterSetups,
			final List< SourceAndConverter< T > > sources )
	{
		super( bvv, numTimepoints );
		this.type = type;
		this.converterSetups = converterSetups;
		this.sources = sources;
	}

	@Override
	public void removeFromBdv()
	{
		getBvvHandle().remove( converterSetups, sources, null, null, null );
		getBvvHandle().removeBvvSource( this );
		setBdvHandle( null );
	}

	@Override
	protected boolean isPlaceHolderSource()
	{
		return false;
	}

	@Override
	public void setColor( final ARGBType color )
	{
		for ( final ConverterSetup setup : converterSetups )
			setup.setColor( color );		
	}

	@Override
	public void setDisplayRange( final double min, final double max )
	{
		for ( final ConverterSetup setup : converterSetups )
			setup.setDisplayRange( min, max );
		// TODO: REMOVE
		final HashSet< MinMaxGroup > groups = new HashSet<>();
		final SetupAssignmentsPG sa = getBvvHandle().getSetupAssignments();
		for ( final ConverterSetup setup : converterSetups )
			groups.add( sa.getMinMaxGroup( setup ) );
		for ( final MinMaxGroup group : groups )
		{
			group.getMinBoundedValue().setCurrentValue(min);
			group.getMaxBoundedValue().setCurrentValue(max);
		}
	}

	@Override
	public void setDisplayRangeBounds( final double min, final double max )
	{
		final ConverterSetupBounds bounds = getBvvHandle().getConverterSetups().getBounds();
		for ( final ConverterSetup setup : converterSetups )
			bounds.setBounds( setup, new Bounds( min, max ) );
		
		// TODO: REMOVE
		final HashSet< MinMaxGroup > groups = new HashSet<>();
		final SetupAssignmentsPG sa = getBvvHandle().getSetupAssignments();
		for ( final ConverterSetup setup : converterSetups )
			groups.add( sa.getMinMaxGroup( setup ) );
		for ( final MinMaxGroup group : groups )
			group.setRange( min, max );
	}

	@Override
	public void setCurrent()
	{
		getBvvHandle().getViewerPanel().getVisibilityAndGrouping().setCurrentSource( sources.get( 0 ).getSpimSource() );
	}

	@Override
	public boolean isCurrent()
	{
		return sources.contains( getBvvHandle().getViewerPanel().state().getCurrentSource() );
	}

	@Override
	public void setActive( final boolean isActive )
	{
		getBvvHandle().getViewerPanel().state().setSourcesActive( sources, isActive );
	}
	
	//additional BVV-playground methods
	
	@Override
	public void setDisplayGamma( final double gamma )
	{
		for ( final ConverterSetup setup : converterSetups )
		{
			if (setup instanceof GammaConverterSetup)
			{
				final GammaConverterSetup gconverter = ((GammaConverterSetup)setup);
				gconverter.setDisplayGamma(gamma);
			}
		}
		
		// TODO: REMOVE
		final HashSet< MinMaxGroup > groups = new HashSet<>();
		final SetupAssignmentsPG sa = getBvvHandle().getSetupAssignments();
		for ( final ConverterSetup setup : converterSetups )
			groups.add( sa.getMinMaxGroup( setup ) );
		for ( final MinMaxGroup group : groups )
		{
			if(group instanceof MinMaxGroupPG)
			{
				((MinMaxGroupPG)group).gammaRange.setCurrentValue(gamma);
			}
		}
	}
	
	@Override
	public void setAlphaRange( final double minAlpha, final double maxAlpha )
	{
		for ( final ConverterSetup setup : converterSetups )
		{
			if (setup instanceof GammaConverterSetup)
			{
				final GammaConverterSetup gconverter = ((GammaConverterSetup)setup);
				gconverter.setAlphaRange(minAlpha, maxAlpha);
			}
		}
		
		// TODO: REMOVE
		final HashSet< MinMaxGroup > groups = new HashSet<>();
		final SetupAssignmentsPG sa = getBvvHandle().getSetupAssignments();
		for ( final ConverterSetup setup : converterSetups )
			groups.add( sa.getMinMaxGroup( setup ) );
		for ( final MinMaxGroup group : groups )
		{
			if(group instanceof MinMaxGroupPG)
			{
				((MinMaxGroupPG)group).alphaRange.getMinBoundedValue().setCurrentValue(minAlpha);
				((MinMaxGroupPG)group).alphaRange.getMaxBoundedValue().setCurrentValue(maxAlpha);
			}
		}
	}
	
	@Override
	public void setAlphaGamma( final double gammaAlpha )
	{
		for ( final ConverterSetup setup : converterSetups )
		{
			if (setup instanceof GammaConverterSetup)
			{
				final GammaConverterSetup gconverter = ((GammaConverterSetup)setup);
				gconverter.setAlphaGamma(gammaAlpha);
			}
		}
		// TODO: REMOVE
		final HashSet< MinMaxGroup > groups = new HashSet<>();
		final SetupAssignmentsPG sa = getBvvHandle().getSetupAssignments();
		for ( final ConverterSetup setup : converterSetups )
			groups.add( sa.getMinMaxGroup( setup ) );
		for ( final MinMaxGroup group : groups )
		{
			if(group instanceof MinMaxGroupPG)
			{
				((MinMaxGroupPG)group).gammaAlphaRange.setCurrentValue(gammaAlpha);
			}
		}
	}
	
	@Override
	public void setRenderType(final int nRenderType)
	{
		for ( final ConverterSetup setup : converterSetups )
		{
			if (setup instanceof GammaConverterSetup)
			{
				final GammaConverterSetup gconverter = ((GammaConverterSetup)setup);
				gconverter.setRenderType(nRenderType);
			}
		}
	}
		
	@Override
	public void setLUT(final IndexColorModel icm_, String sLUTName )
	{
		for ( final ConverterSetup setup : converterSetups )
		{
			if (setup instanceof GammaConverterSetup)
			{
				final GammaConverterSetup gconverter = ((GammaConverterSetup)setup);
				gconverter.setLUT(icm_, sLUTName);
			}
		}
	}
	
	@Override
	public void setLUT( String sLUTName )
	{
		for ( final ConverterSetup setup : converterSetups )
		{
			if (setup instanceof GammaConverterSetup)
			{
				final GammaConverterSetup gconverter = ((GammaConverterSetup)setup);
				gconverter.setLUT(sLUTName);
			}
		}
	}
	
	@Override
	public void setClipState(final int nClipType)
	{
		for ( final ConverterSetup setup : converterSetups )
		{
			if (setup instanceof GammaConverterSetup)
			{
				final GammaConverterSetup gconverter = ((GammaConverterSetup)setup);
				gconverter.setClipState( nClipType );
			}
		}
	}
	
	@Override
	public void setClipInterval(RealInterval clipInt)
	{
		for ( final ConverterSetup setup : converterSetups )
		{
			if (setup instanceof GammaConverterSetup)
			{
				final GammaConverterSetup gconverter = ((GammaConverterSetup)setup);
				gconverter.setClipInterval(clipInt);
			}
		}
	}
	
	@Override
	public void setDisplayGammaRangeBounds( final double min, final double max )
	{
		
		ConverterSetups cSetups = getBvvHandle().getConverterSetups();
		
		if(cSetups instanceof ConverterSetupsPG)
		{
			final ConverterSetupBoundsGamma bounds = ((ConverterSetupsPG)cSetups).getBoundsGamma();
			for ( final ConverterSetup setup : converterSetups )
				bounds.setBounds( setup, new Bounds( min, max ) );
		}
		
		// TODO: REMOVE
		final HashSet< MinMaxGroup > groups = new HashSet<>();
		final SetupAssignmentsPG sa = getBvvHandle().getSetupAssignments();
		for ( final ConverterSetup setup : converterSetups )
			groups.add( sa.getMinMaxGroup( setup ) );
		for ( final MinMaxGroup group : groups )
		{
			if(group instanceof MinMaxGroupPG)
				((MinMaxGroupPG)group).gammaRange.setRange(min, max);
		}
	}
	
	@Override
	public void setAlphaRangeBounds(final double min, final double max)
	{
		ConverterSetups cSetups = getBvvHandle().getConverterSetups();
		
		if(cSetups instanceof ConverterSetupsPG)
		{
			final ConverterSetupBoundsAlpha bounds = ((ConverterSetupsPG)cSetups).getBoundsAlpha();
			for ( final ConverterSetup setup : converterSetups )
				bounds.setBounds( setup, new Bounds( min, max ) );
		}
		
		// TODO: REMOVE
		final HashSet< MinMaxGroup > groups = new HashSet<>();
		final SetupAssignmentsPG sa = getBvvHandle().getSetupAssignments();
		for ( final ConverterSetup setup : converterSetups )
			groups.add( sa.getMinMaxGroup( setup ) );
		for ( final MinMaxGroup group : groups )
		{
			if(group instanceof MinMaxGroupPG)
				((MinMaxGroupPG)group).alphaRange.setRange(min, max);
		}

	}
	
	@Override
	public void setAlphaGammaRangeBounds(final double min, final double max)
	{
		
		ConverterSetups cSetups = getBvvHandle().getConverterSetups();
		
		if(cSetups instanceof ConverterSetupsPG)
		{
			final ConverterSetupBoundsGammaAlpha bounds = ((ConverterSetupsPG)cSetups).getBoundsGammaAlpha();
			for ( final ConverterSetup setup : converterSetups )
				bounds.setBounds( setup, new Bounds( min, max ) );
		}
		
		// TODO: REMOVE
		final HashSet< MinMaxGroup > groups = new HashSet<>();
		final SetupAssignmentsPG sa = getBvvHandle().getSetupAssignments();
		for ( final ConverterSetup setup : converterSetups )
			groups.add( sa.getMinMaxGroup( setup ) );
		for ( final MinMaxGroup group : groups )
		{
			if(group instanceof MinMaxGroupPG)
				((MinMaxGroupPG)group).gammaAlphaRange.setRange(min, max);
		}
	}


	public List< ConverterSetup > getConverterSetups()
	{
		return converterSetups;
	}

	public List< SourceAndConverter< T > > getSources()
	{
		return sources;
	}

	public void invalidate()
	{
		for ( final SourceAndConverter< T > source : sources )
			SourceStacks.invalidate( source.getSpimSource() );

		final BvvHandle bvv = getBvvHandle();
		if ( bvv != null )
			bvv.getViewerPanel().requestRepaint();
	}

	@Override
	public void setClipTransform(AffineTransform3D clipTransform) 
	{
		for ( final ConverterSetup setup : converterSetups )
		{
			if (setup instanceof GammaConverterSetup)
			{
				final GammaConverterSetup gconverter = ((GammaConverterSetup)setup);
				gconverter.setClipTransform(clipTransform);
			}
		}
		
	}

	@Override
	public void setVoxelRenderInterpolation( int nInterpolationType )
	{
		for ( final ConverterSetup setup : converterSetups )
		{
			if (setup instanceof GammaConverterSetup)
			{
				final GammaConverterSetup gconverter = ((GammaConverterSetup)setup);
				gconverter.setVoxelRenderInterpolation( nInterpolationType );
			}
		}
		
	}

}
