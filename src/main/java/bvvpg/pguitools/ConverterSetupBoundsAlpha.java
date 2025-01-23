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
package bvvpg.pguitools;

import java.util.HashMap;
import java.util.Map;

import bdv.tools.brightness.ConverterSetup;
import bdv.util.BoundedRange;
import bdv.util.Bounds;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.SourceToConverterSetupBimap;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.volatiles.VolatileARGBType;

public class ConverterSetupBoundsAlpha 
{
	private final SourceToConverterSetupBimap bimap;

	private final Map< ConverterSetup, Bounds > setupToBounds = new HashMap<>();

	ConverterSetupBoundsAlpha( final SourceToConverterSetupBimap bimap )
	{
		this.bimap = bimap;
	}

	public Bounds getBounds( final ConverterSetup setup )
	{
		return setupToBounds.compute( setup, this::getExtendedBounds );
	}

	public void setBounds( final ConverterSetup setup, final Bounds bounds )
	{
		setupToBounds.put( setup, bounds );

		if(setup instanceof GammaConverterSetup)
		{
			GammaConverterSetup gsetup = (GammaConverterSetup)setup;
			final double min = gsetup.getAlphaRangeMin();
			final double max = gsetup.getAlphaRangeMax();
			final BoundedRange range = new BoundedRange( min, max, min, max ).withMinBound( bounds.getMinBound() ).withMaxBound( bounds.getMaxBound() );
			if ( range.getMin() != min || range.getMax() != max )
				gsetup.setAlphaRange(range.getMin(), range.getMax());
				//setup.setDisplayRange( range.getMin(), range.getMax() );
		}
	}

	private Bounds getDefaultBounds( final ConverterSetup setup )
	{
		Bounds bounds;
		if(setup instanceof GammaConverterSetup)
		{
			bounds = new Bounds( ((GammaConverterSetup)setup).getAlphaRangeMin(), ((GammaConverterSetup)setup).getAlphaRangeMax() );
		}
		else
		{
			bounds = new Bounds( setup.getDisplayRangeMin(), setup.getDisplayRangeMax() );	
		}
		
		final SourceAndConverter< ? > source = bimap.getSource( setup );
		if ( source != null )
		{
			final Object type = source.getSpimSource().getType();

			if ( type instanceof ARGBType || type instanceof VolatileARGBType )
			{
				bounds = bounds.join( new Bounds( 0, 255 ) );
			}
			else if ( type instanceof IntegerType )
			{
				final IntegerType< ? > integerType = ( IntegerType< ? > ) type;
				bounds = bounds.join( new Bounds( integerType.getMinValue(), integerType.getMaxValue() ) );
			}
			else
			{
				bounds = bounds.join( new Bounds( 0, 1 ) );
			}
		}
		return bounds;
	}

	private Bounds getExtendedBounds( final ConverterSetup setup, Bounds bounds )
	{
		if ( bounds == null )
			bounds = getDefaultBounds( setup );
		if(setup instanceof GammaConverterSetup)
		{
			return bounds.join( new Bounds( ((GammaConverterSetup)setup).getAlphaRangeMin(), ((GammaConverterSetup)setup).getAlphaRangeMax() ) );
		}
		return bounds.join( new Bounds( setup.getDisplayRangeMin(), setup.getDisplayRangeMax() ) );
	}
}
