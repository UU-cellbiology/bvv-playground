package btbvv.btuitools;

import java.util.HashMap;
import java.util.Map;

import bdv.tools.brightness.ConverterSetup;
import bdv.util.Bounds;
import bdv.viewer.SourceToConverterSetupBimap;

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
		else
			return bounds.join( new Bounds( 0.01, 5.0 ) );
	}
}
