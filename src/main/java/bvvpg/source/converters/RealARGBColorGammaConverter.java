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

import net.imglib2.converter.Converter;
import net.imglib2.loops.ClassCopyProvider;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

public interface RealARGBColorGammaConverter< R extends RealType< ? > > extends ColorGammaConverter, Converter< R, ARGBType >
{
	static < R extends RealType< ? > > RealARGBColorGammaConverter< R > create( final R type, final double min, final double max, final double gamma)
	{
		return Instances.create( type, min, max, gamma);
	}
}

class Instances
{
	@SuppressWarnings( "rawtypes" )
	private static ClassCopyProvider< RealARGBColorGammaConverter > provider;

	@SuppressWarnings( "unchecked" )
	public static < R extends RealType< ? > > RealARGBColorGammaConverter< R > create( final R type, final double min, final double max, final double gamma )
	{
		if ( provider == null )
		{
			synchronized ( Instances.class )
			{
				if ( provider == null )
					provider = new ClassCopyProvider<>( Imp.class, RealARGBColorGammaConverter.class, double.class, double.class, double.class );
			}
		}
		return provider.newInstanceForKey( type.getClass(), min, max, gamma );
	}

	public static class Imp< R extends RealType< ? > > implements RealARGBColorGammaConverter< R >
	{
		private double min = 0;

		private double max = 1;
		
		private double minAlpha = 0;

		private double maxAlpha = 1;
		
		private double gamma = 1;
		
		private double gammaAlpha = 1;

		private final ARGBType color = new ARGBType( ARGBType.rgba( 255, 255, 255, 255 ) );

		private double scaleR;

		private double scaleG;

		private double scaleB;
		
		private double scaleA;

		private int black;

		public Imp( final double min, final double max, final double gamma)
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
		public ARGBType getColor()
		{
			return color.copy();
		}

		@Override
		public void setColor( final ARGBType c )
		{
			color.set( c );
			update();
		}

		@Override
		public boolean supportsColor()
		{
			return true;
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


		private void update()
		{
			final double scale = 1.0 / ( max - min );
			final double scaleAlpha = 1.0 / ( maxAlpha - minAlpha );
			final int value = color.get();
			scaleA = ARGBType.alpha( value ) * scaleAlpha;
			scaleR = ARGBType.red( value ) * scale;
			scaleG = ARGBType.green( value ) * scale;
			scaleB = ARGBType.blue( value ) * scale;
			black = ARGBType.rgba( 0, 0, 0, 255 );
		}

		@Override
		public void convert( final R input, final ARGBType output )
		{
			final double v = input.getRealDouble() - min;
			if ( v < 0 )
			{
				output.set( black );
			}
			else
			{
				final int r0 = ( int ) ( scaleR * Math.pow(v, gamma) + 0.5 );
				final int g0 = ( int ) ( scaleG * Math.pow(v, gamma) + 0.5 );
				final int b0 = ( int ) ( scaleB * Math.pow(v, gamma) + 0.5 );
				final int a0 = ( int ) ( scaleA * Math.pow(v, gammaAlpha) + 0.5 );
				final int r = Math.min( 255, r0 );
				final int g = Math.min( 255, g0 );
				final int b = Math.min( 255, b0 );
				final int a = Math.min( 255, a0 );
				output.set( ARGBType.rgba( r, g, b, a ) );
			}
		}
	}
}
