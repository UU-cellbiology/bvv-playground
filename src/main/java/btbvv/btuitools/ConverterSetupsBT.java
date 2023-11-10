package btbvv.btuitools;

import bdv.viewer.ConverterSetups;
import bdv.viewer.ViewerState;

public class ConverterSetupsBT extends ConverterSetups {

	private final ConverterSetupBoundsGamma boundsGamma;
	private final ConverterSetupBoundsAlpha boundsAlpha;
	private final ConverterSetupBoundsGammaAlpha boundsGammaAlpha;
	
	public ConverterSetupsBT( final ViewerState state )
	{
		super(state);
		boundsAlpha = new ConverterSetupBoundsAlpha( this );
		boundsGamma = new ConverterSetupBoundsGamma();
		boundsGammaAlpha = new ConverterSetupBoundsGammaAlpha();
	}

	public ConverterSetupBoundsGamma getBoundsGamma()
	{
		return boundsGamma;
	}
	public ConverterSetupBoundsAlpha getBoundsAlpha()
	{
		return boundsAlpha;
	}
	
	public ConverterSetupBoundsGammaAlpha getBoundsGammaAlpha()
	{
		return boundsGammaAlpha;
	}
	
}
