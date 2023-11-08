package btbvv.btuitools;

import bdv.viewer.ConverterSetups;
import bdv.viewer.ViewerState;

public class ConverterSetupsBT extends ConverterSetups {

	private final ConverterSetupBoundsAlphaBT boundsAlpha;
	
	public ConverterSetupsBT( final ViewerState state )
	{
		super(state);
		boundsAlpha = new ConverterSetupBoundsAlphaBT( this );
	}
	
	public ConverterSetupBoundsAlphaBT getBoundsAlpha()
	{
		return boundsAlpha;
	}
	
}
