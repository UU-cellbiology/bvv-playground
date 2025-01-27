/*
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
package bvvpg.source.converters;

import java.util.LinkedHashSet;
import java.util.Set;

import bdv.tools.brightness.ConverterSetup;
import bdv.tools.brightness.MinMaxGroup;
import bdv.util.BoundedIntervalDouble;
import bdv.util.BoundedValueDouble;

/**
 * A {@code double} interval. The {@link #getMinBoundedValue() min} and
 * {@link #getMaxBoundedValue() max} of the interval are stored as
 * {@link BoundedValueDouble}. The min and max can be changed to span any non-empty
 * interval within the range ({@link #getRangeMin()}, {@link #getRangeMax()}).
 * This range can be {@link #setRange(double, double) modified} as well.
 * <p>
 * Some {@link ConverterSetup ConverterSetups} can be
 * {@link #addSetup(ConverterSetup) linked}. They will have their display range
 * set according to {@link #getMinBoundedValue() min} and
 * {@link #getMaxBoundedValue() max} of the interval.
 * <p>
 * An {@link UpdateListener} (usually a GUI component) can be notified about
 * changes.
 *
 * @author Tobias Pietzsch
 */
public class MinMaxGroupPG extends MinMaxGroup
{
	
	private final double fullRangeMin;

	private final double fullRangeMax;
	
	private final double gammaFullRangeMin;
	
	private final double gammaFullRangeMax;
	
	private final double fullAlphaRangeMin;

	private final double fullAlphaRangeMax;
	
	private final double gammaAlphaFullRangeMin;
	
	private final double gammaAlphaFullRangeMax;
	
	public BoundedValueDouble gammaRange; 
	
	public BoundedIntervalDouble alphaRange;
	
	public BoundedValueDouble gammaAlphaRange; 

	public final Set< ConverterSetup > setups;
	
	public boolean bSync = true;

	public interface UpdateListener
	{
		void update();
	}

	private UpdateListener updateListener;

	public MinMaxGroupPG(
			final double fullRangeMin,
			final double fullRangeMax,
			final double rangeMin,
			final double rangeMax,
			final double currentMin,
			final double currentMax,
			final double minIntervalSize )
	{
		super(fullRangeMin,fullRangeMax,rangeMin, rangeMax, currentMin,currentMax, minIntervalSize );
		
		

		alphaRange = new BoundedIntervalDouble( rangeMin,  rangeMax,  currentMin, currentMax,  minIntervalSize )		
		{
			@Override
			public void updateInterval( final double min, final double max  )
			{

				for ( final ConverterSetup setup : setups )
				{
					if(setup instanceof GammaConverterSetup)
					{
						final GammaConverterSetup setupgamma = (GammaConverterSetup)setup;
						setupgamma.setAlphaRange( min, max );
					}
				}

			}
		};
		
		
		//super( rangeMin, rangeMax, currentMin, currentMax, minIntervalSize );
		this.fullRangeMin = fullRangeMin;
		this.fullRangeMax = fullRangeMax;
		this.fullAlphaRangeMin = fullRangeMin;
		this.fullAlphaRangeMax = fullRangeMax;
		
		gammaFullRangeMin = 0.01;
		gammaFullRangeMax = 100.0;
		gammaAlphaFullRangeMin = 0.01;
		gammaAlphaFullRangeMax = 100.0;
		
		gammaRange = new BoundedValueDouble( 0.01, 5.0, 1.0)
		{
			@Override
			public void setCurrentValue( final double value )
			{
				super.setCurrentValue( value );
				updateGamma();	
				if(bSync)
					gammaAlphaRange.setCurrentValue(value);
			}
		};
		
		gammaAlphaRange = new BoundedValueDouble( 0.01, 5.0, 1.0)
		{
			@Override
			public void setCurrentValue( final double value )
			{
				super.setCurrentValue( value );
				updateGammaAlpha();
			}
		};
		setups = new LinkedHashSet<>();
		updateListener = null;
		
	}

	@Override
	protected void updateInterval( final double min, final double max )
	{
		for ( final ConverterSetup setup : setups )
		{
			setup.setDisplayRange( min, max );			
		}
		if(bSync)
		{
			alphaRange.getMinBoundedValue().setCurrentValue(min);
			alphaRange.getMaxBoundedValue().setCurrentValue(max);
		}
	}
	

		
	
	protected void updateGamma()
	{
		for ( final ConverterSetup setup : setups )
		{
			if(setup instanceof GammaConverterSetup)
			{
				((GammaConverterSetup) setup).setDisplayGamma(gammaRange.getCurrentValue());
				//System.out.println(gammaRange.getCurrentValue());
			}
		}
	}
	
	protected void updateGammaAlpha()
	{
		for ( final ConverterSetup setup : setups )
		{
			if(setup instanceof GammaConverterSetup)
			{
				((GammaConverterSetup) setup).setAlphaGamma(gammaAlphaRange.getCurrentValue());
				//System.out.println(gammaRange.getCurrentValue());
			}
		}
	}

	@Override
	public double getFullRangeMin()
	{
		return fullRangeMin;
	}

	@Override
	public double getFullRangeMax()
	{
		return fullRangeMax;
	}
	public double getAlphaFullRangeMin()
	{
		return fullAlphaRangeMin;
	}

	public double getAlphaFullRangeMax()
	{
		return fullAlphaRangeMax;
	}

	public double getGammaFullRangeMin()
	{
		return gammaFullRangeMin;
	}

	public double getGammaFullRangeMax()
	{
		return gammaFullRangeMax;
	}
	
	public double getGammaAlphaFullRangeMin()
	{
		return gammaAlphaFullRangeMin;
	}

	public double getGammaAlphaFullRangeMax()
	{
		return gammaAlphaFullRangeMax;
	}
	/**
	 * Add a {@link ConverterSetup} which will have its
	 * {@link ConverterSetup#setDisplayRange(double, double) display range} updated to
	 * the interval ({@link #getMinBoundedValue()},
	 * {@link #getMaxBoundedValue()}).
	 *
	 * @param setup
	 */
	@Override
	public void addSetup( final ConverterSetup setup )
	{
		setups.add( setup );
//		setup.setDisplayRange( getMinBoundedValue().getCurrentValue(), getMaxBoundedValue().getCurrentValue() );

		if ( updateListener != null )
			updateListener.update();
	}

	/**
	 * Remove a {@link ConverterSetup} from this group.
	 *
	 * @param setup
	 * @return true, if this group is now empty. false otherwise.
	 */
	@Override
	public boolean removeSetup( final ConverterSetup setup )
	{
		setups.remove( setup );

		if ( updateListener != null )
			updateListener.update();

		return setups.isEmpty();
	}

	/**
	 * Set an {@link UpdateListener} (usually a GUI component).
	 */
	public void setUpdateListener( final UpdateListener l )
	{
		updateListener = l;
	}
}
