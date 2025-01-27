package bvvpg.pguitools;


import javax.swing.JSlider;
import javax.swing.UIManager;


/**
 * An extension of JSlider to select a range of values using two thumb controls.
 * The thumb controls are used to select the lower and upper value of a range
 * with predetermined minimum and maximum values.
 * <p>
 * Note that RangeSlider makes use of the default BoundedRangeModel, which
 * supports an inner range defined by a value and an extent. The upper value
 * returned by RangeSlider is simply the lower value plus the extent.
 * <p>
 * This is copied from https://github.com/ernieyu/Swing-range-slider with the
 * following changes:
 * <ul>
 * <li>The slider thumbs push each other. That is, the upper thumb can be
 * dragged below the lower thumb, and it will drag the lower thumb with it, and
 * vice versa</li>
 * <li>The {@link #setRange(int, int)} method was added, which sets both the
 * lower and upper values simultaneously.</li>
 * </ul>
 * <p>
 *
 * <pre>
 * =============================================================================
 *
 * The MIT License Copyright (c) 2010 Ernest Yu. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * </pre>
 */
public class RangeSliderPG extends JSlider
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a RangeSlider with default minimum and maximum values of 0 and
	 * 100.
	 */
	public RangeSliderPG()
	{
		initSlider();
	}

	/**
	 * Constructs a RangeSlider with the specified default minimum and maximum
	 * values.
	 */
	public RangeSliderPG( final int min, final int max )
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
		String currLF = UIManager.getLookAndFeel().getName();
		if(currLF.contains( "FlatLaf" ))
		{
			setUI( new RangeSliderUIFL( this ) );
		}
		else
		{
			setUI( new RangeSliderUIPlain( this ) );
		}
		// Update UI for slider labels. This must be called after updating the
		// UI of the slider. Refer to JSlider.updateUI().
		updateLabelUIs();
	}

	/**
	 * Returns the lower value in the range.
	 */
	@Override
	public int getValue()
	{
		return super.getValue();
	}

	/**
	 * Sets the lower value in the range.
	 */
	@Override
	public void setValue( final int value )
	{
		final int oldValue = getValue();
		if ( oldValue == value )
		{ return; }

		// Compute new value and extent to maintain upper value.
		final int oldExtent = getExtent();
		final int newValue = Math.min( Math.max( getMinimum(), value ), oldValue + oldExtent );
		final int newExtent = oldExtent + oldValue - newValue;

		// Set new value and extent, and fire a single change event.
		getModel().setRangeProperties( newValue, newExtent, getMinimum(),
				getMaximum(), getValueIsAdjusting() );
	}

	/**
	 * Returns the upper value in the range.
	 */
	public int getUpperValue()
	{
		return getValue() + getExtent();
	}

	/**
	 * Sets the upper value in the range.
	 */
	public void setUpperValue( final int value )
	{
		// Compute new extent.
		final int lowerValue = getValue();
		final int newExtent = Math.min( Math.max( 0, value - lowerValue ), getMaximum() - lowerValue );

		// Set extent to set upper value.
		setExtent( newExtent );
	}

	/**
	 * Sets both the lower and upper values in the range.
	 */
	public void setRange( final int lower, final int upper )
	{
		final int newValue = Math.max( getMinimum(), Math.min( getMaximum(), lower ) );
		final int newUpper = Math.max( newValue, Math.min( getMaximum(), upper ) );
		final int newExtent = newUpper - newValue;

		// Set new value and extent, and fire a single change event.
		getModel().setRangeProperties( newValue, newExtent, getMinimum(),
				getMaximum(), getValueIsAdjusting() );
	}
}
