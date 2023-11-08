package btbvv.btuitools;

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

public class ConverterSetupBoundsAlphaBT 
{
	private final SourceToConverterSetupBimap bimap;

	private final Map< ConverterSetup, Bounds > setupToBounds = new HashMap<>();

	ConverterSetupBoundsAlphaBT( final SourceToConverterSetupBimap bimap )
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
		else
			return bounds.join( new Bounds( setup.getDisplayRangeMin(), setup.getDisplayRangeMax() ) );
	}
}
