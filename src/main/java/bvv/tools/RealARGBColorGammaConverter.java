package bvv.tools;

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
		
		private double gamma = 1;

		private final ARGBType color = new ARGBType( ARGBType.rgba( 255, 255, 255, 255 ) );

		//private int A;

		private double scaleR;

		private double scaleG;

		private double scaleB;
		
		private double scaleA;

		private int black;

		public Imp( final double min, final double max, final double gamma)
		{
			this.min = min;
			this.max = max;
			this.gamma = gamma;
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
		public double getGamma()
		{
			return gamma;
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
		public void setGamma (final double gamma)
		{
			this.gamma = gamma;
			update();
		}

		private void update()
		{
			final double scale = 1.0 / ( max - min );
			final int value = color.get();
			scaleA = ARGBType.alpha( value );
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
				final int a0 = ( int ) ( scaleA * Math.pow(v, gamma) + 0.5 );
				final int r = Math.min( 255, r0 );
				final int g = Math.min( 255, g0 );
				final int b = Math.min( 255, b0 );
				final int a = Math.min( 255, a0 );
				output.set( ARGBType.rgba( r, g, b, a ) );
			}
		}
	}
}
