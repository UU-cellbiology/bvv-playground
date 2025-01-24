package bvvpg.pguitools;

import javax.swing.JSlider;


public class ValueSlider extends JSlider
{
	private static final long serialVersionUID = 1L;
	
	public ValueSlider( final int min, final int max )
	{
		super( min, max );
		initSlider();
	}

	/**
	 * Initializes the slider by setting default properties.
	 */
	private void initSlider()
	{
		setOrientation( HORIZONTAL );
	}
	
	
	/**
	 * Overrides the superclass method to install the UI delegate to draw two
	 * thumbs.
	 */
	@Override
	public void updateUI()
	{
		setUI( new ValueSliderUI( this ) );
		// Update UI for slider labels. This must be called after updating the
		// UI of the slider. Refer to JSlider.updateUI().
		updateLabelUIs();
	}
	

}
