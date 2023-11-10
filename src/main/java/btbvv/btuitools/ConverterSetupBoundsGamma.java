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

public class ConverterSetupBoundsGamma {
	private final SourceToConverterSetupBimap bimap;

	private final Map< ConverterSetup, Bounds > setupToBounds = new HashMap<>();

	ConverterSetupBoundsGamma( final SourceToConverterSetupBimap bimap )
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
			final double gamma = ((GammaConverterSetup)setup).getDisplayGamma();
			final double min = bounds.getMinBound();
			final double max = bounds.getMaxBound();
			if(gamma<min)
			{
				((GammaConverterSetup)setup).setDisplayGamma(min);
			}
			if(gamma>max)
			{
				((GammaConverterSetup)setup).setDisplayGamma(max);
			}
		}
	}

	private Bounds getDefaultBounds( final ConverterSetup setup )
	{
		Bounds bounds;
		if(setup instanceof GammaConverterSetup)
		{
			double gamma = ((GammaConverterSetup)setup).getDisplayGamma();
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
			return bounds.join( new Bounds( ((GammaConverterSetup)setup).getDisplayGamma(), ((GammaConverterSetup)setup).getDisplayGamma()) );
		}
		else
			return bounds.join( new Bounds( 0.01, 5.0 ) );
	}
}
