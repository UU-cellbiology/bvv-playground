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
package bvvpg.source.converters;

import java.util.HashMap;
import java.util.Map;

import bdv.tools.brightness.ConverterSetup;
import bdv.util.Bounds;

public class ConverterSetupBoundsGammaAlpha {

	private final Map< ConverterSetup, Bounds > setupToBounds = new HashMap<>();

	ConverterSetupBoundsGammaAlpha( )
	{

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
			final double gamma = ((GammaConverterSetup)setup).getAlphaGamma();
			final double min = bounds.getMinBound();
			final double max = bounds.getMaxBound();
			if(gamma<min)
			{
				((GammaConverterSetup)setup).setAlphaGamma(min);
			}
			if(gamma>max)
			{
				((GammaConverterSetup)setup).setAlphaGamma(max);
			}

		}
	}

	private Bounds getDefaultBounds( final ConverterSetup setup )
	{
		Bounds bounds;
		if(setup instanceof GammaConverterSetup)
		{
			double gamma = ((GammaConverterSetup)setup).getAlphaGamma();
			bounds = new Bounds( Math.min(0.01, gamma*0.5), Math.max(5.0, gamma*2));	
		}
		else
		{
			bounds = new Bounds( 0.01, 5.0);	
		}
		
		return bounds;
	}

	private Bounds getExtendedBounds( final ConverterSetup setup, Bounds bounds )
	{
		if ( bounds == null )
			bounds = getDefaultBounds( setup );
		if(setup instanceof GammaConverterSetup)
		{
			return bounds.join( new Bounds( ((GammaConverterSetup)setup).getAlphaGamma(), ((GammaConverterSetup)setup).getAlphaGamma()) );
		}
		return bounds.join( new Bounds( 0.01, 5.0 ) );
	}
}
