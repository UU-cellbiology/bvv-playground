/*-
 * #%L
 * Volume rendering of bdv datasets with gamma and transparency option
 * %%
 * Copyright (C) 2022 - 2024 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
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

import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.volatiles.VolatileARGBType;

public abstract class ScaledARGBGammaConverter<T> implements ColorGammaConverter, Converter<T, ARGBType>
{
	protected double min = 0;

	protected double max = 1;
	
	private double minAlpha = 0;

	private double maxAlpha = 1;
	
	private double gamma = 1;
	
	private double gammaAlpha = 1;

	protected double scale;
	
	private double scaleA;

	private ScaledARGBGammaConverter( final double min, final double max, final double gamma )
	{
		this.min = min;
		this.max = max;
		this.minAlpha = min;
		this.maxAlpha = max;
		this.gamma = gamma;
		this.gammaAlpha = gamma;
		update();
	}

	@Override
	public double getMin()
	{
		return min;
	}

	@Override
	public double getMax()
	{
		return max;
	}

	@Override
	public void setMax( final double max )
	{
		this.max = max;
		update();
	}

	@Override
	public void setMin( final double min )
	{
		this.min = min;
		update();
	}
	
	@Override
	public double getMinAlpha()
	{
		return minAlpha;
	}

	@Override
	public double getMaxAlpha()
	{
		return maxAlpha;
	}
	
	@Override
	public double getGamma()
	{
		return gamma;
	}
	
	@Override
	public double getGammaAlpha()
	{
		return gammaAlpha;
	}
	
	@Override
	public void setMaxAlpha( final double maxAlpha )
	{
		this.maxAlpha = maxAlpha;
		update();
	}

	@Override
	public void setMinAlpha( final double minAlpha )
	{
		this.minAlpha = minAlpha;
		update();
	}
	
	@Override
	public void setGamma (final double gamma)
	{
		this.gamma = gamma;
		update();
	}
	
	@Override
	public void setGammaAlpha(final double gammaAlpha)
	{
		this.gammaAlpha = gammaAlpha;
		update();
	}

	@Override
	public ARGBType getColor()
	{
		return new ARGBType();
	}

	@Override
	public void setColor( final ARGBType c )
	{}

	@Override
	public boolean supportsColor()
	{
		return false;
	}

	private void update()
	{
		scale = 255.0 / ( max - min );
		scaleA = 255.0 / (maxAlpha - minAlpha);
	}

	int getScaledColor( final int color )
	{
		final int a = Math.min( 255, ( int ) ( scaleA * Math.max( 0, ARGBType.alpha( color ) - minAlpha ) + 0.5 ) );
		//final int a = ARGBType.alpha( color );
		final int r = Math.min( 255, ( int ) ( scale * Math.max( 0, ARGBType.red( color ) - min ) + 0.5 ) );
		final int g = Math.min( 255, ( int ) ( scale * Math.max( 0, ARGBType.green( color ) - min ) + 0.5 ) );
		final int b = Math.min( 255, ( int ) ( scale * Math.max( 0, ARGBType.blue( color ) - min ) + 0.5 ) );
		return ARGBType.rgba( r, g, b, a );
	}

	public static class ARGB extends ScaledARGBGammaConverter< ARGBType >
	{
		public ARGB( final double min, final double max, final double gamma)
		{
			super( min, max, gamma );
		}

		@Override
		public void convert( final ARGBType input, final ARGBType output )
		{
			output.set( getScaledColor( input.get() ) );
		}

	}

	public static class VolatileARGB extends ScaledARGBGammaConverter< VolatileARGBType >
	{
		public VolatileARGB( final double min, final double max, final double gamma)
		{
			super( min, max, gamma );
		}

		@Override
		public void convert( final VolatileARGBType input, final ARGBType output )
		{
			output.set( getScaledColor( input.get().get() ) );
		}


	}
}
