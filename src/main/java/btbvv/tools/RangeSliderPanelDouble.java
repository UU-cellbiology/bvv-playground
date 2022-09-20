/*
 * #%L
 * BigDataViewer core classes with minimal dependencies.
 * %%
 * Copyright (C) 2012 - 2021 BigDataViewer developers.
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
package btbvv.tools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jidesoft.swing.RangeSlider;

import bdv.util.BoundedValue;
import bdv.util.BoundedValueDouble;


/**
 * A {@link JSlider} with a {@link JSpinner} next to it, both modifying the same
 * {@link BoundedValue value}.
 */
public class RangeSliderPanelDouble extends JPanel implements BoundedValueDouble.UpdateListener
{
	private static final long serialVersionUID = 6444334522127424416L;

	private static final int sliderLength = 10000;

	//private final JSlider slider;
	RangeSlider slider;

	private final JSpinner spinnerMin;
	private final JSpinner spinnerMax;

	private final BoundedValueDouble modelMin;
	private final BoundedValueDouble modelMax;

	private double dmin;

	private double dmax;

	private boolean userDefinedNumberFormat = false;
	
	private ArrayList<RangeListener> rangeListeners = new ArrayList<RangeListener>();


	public interface RangeListener
	{
		void rangeChanged();
	}

	/**
	 * Create a {@link SliderPanelDouble} to modify a given {@link BoundedValueDouble value}.
	 *
	 * @param name
	 *            label to show next to the slider.
	 * @param model
	 *            the value that is modified.
	 * @param spinnerStepSize
	 */
	public RangeSliderPanelDouble(
			final String name,
			final BoundedValueDouble modelMin,
			final BoundedValueDouble modelMax,
			final double spinnerStepSize )
	{
		super();
		//setLayout( new BorderLayout( 10, 10 ) );
		//setLayout( new GridLayout( 0, 4,0,0 ) );
		setLayout( new GridBagLayout() );

		dmin = modelMin.getRangeMin();
		dmax = modelMax.getRangeMax();

		slider = new RangeSlider(0, sliderLength, toSlider(modelMin.getCurrentValue()),toSlider(modelMax.getCurrentValue()));
		//slider = new JSlider( SwingConstants.HORIZONTAL, 0, sliderLength, toSlider( model.getCurrentValue() ) );
		spinnerMin = new JSpinner();
		//spinnerMin.setModel( new SpinnerNumberModel( modelMin.getCurrentValue(), dmin, dmax, spinnerStepSize ) );
		spinnerMin.setModel( new SpinnerNumberModel( modelMin.getCurrentValue(), dmin, dmax, spinnerStepSize ) );
		spinnerMax = new JSpinner();
		//spinnerMax.setModel( new SpinnerNumberModel( modelMax.getCurrentValue(), dmin, dmax, spinnerStepSize ) );
		spinnerMax.setModel( new SpinnerNumberModel( modelMax.getCurrentValue(), dmin, dmax, spinnerStepSize ) );

		slider.addChangeListener( new ChangeListener()
		{
			@Override
			public void stateChanged( final ChangeEvent e )
			{
				final int valueLow = slider.getLowValue();
				final int valueHigh = slider.getHighValue();
				modelMin.setCurrentValue( fromSlider( valueLow ) );
				modelMax.setCurrentValue( fromSlider( valueHigh ) );				
				


			}
		} );

		slider.addComponentListener( new ComponentAdapter()
		{
			@Override
			public void componentResized( final ComponentEvent e )
			{
				updateNumberFormat();
			}
		} );

		spinnerMin.addChangeListener( new ChangeListener()
		{
			@Override
			public void stateChanged( final ChangeEvent e )
			{
				final double value = ( ( Double ) spinnerMin.getValue() ).doubleValue();
				modelMin.setCurrentValue( value );
			}
		} );
		spinnerMax.addChangeListener( new ChangeListener()
		{
			@Override
			public void stateChanged( final ChangeEvent e )
			{
				final double value = ( ( Double ) spinnerMax.getValue() ).doubleValue();
				modelMax.setCurrentValue( value );
				update();
			}
		} );
		
		GridBagConstraints cv = new GridBagConstraints();
		cv.gridx=0;
		cv.gridy=0;
		if ( name != null )
		{
			final JLabel label = new JLabel( name);
			//final JLabel label = new JLabel( name, SwingConstants.CENTER );
			label.setMinimumSize(label.getPreferredSize());
			//label.setAlignmentX( Component.CENTER_ALIGNMENT );
			add( label, cv);
			cv.gridx++;
		}

		add(spinnerMin,cv);
		cv.gridx++;
		cv.fill = GridBagConstraints.HORIZONTAL;
		cv.weightx = 0.99;
		add( slider,cv);
		cv.fill = GridBagConstraints.NONE;
		cv.weightx = 0.0;
		cv.gridx++;
		add( spinnerMax,cv);
		//add( spinnerMin, BorderLayout.CENTER );
		//add( slider, BorderLayout.CENTER );
		//add( spinnerMax, BorderLayout.EAST );

		this.modelMin = modelMin;
		modelMin.setUpdateListener( this );
		this.modelMax = modelMax;
		modelMax.setUpdateListener( this );
	}

	public void setDecimalFormat( final String pattern )
	{
		if ( pattern == null )
		{
			userDefinedNumberFormat = false;
			updateNumberFormat();
		}
		else
		{
			userDefinedNumberFormat = true;
			( ( JSpinner.NumberEditor ) spinnerMin.getEditor() ).getFormat().applyPattern( pattern );
			( ( JSpinner.NumberEditor ) spinnerMax.getEditor() ).getFormat().applyPattern( pattern );
		}
	}

	public void setNumColummns( final int cols )
	{
		( ( JSpinner.NumberEditor ) spinnerMin.getEditor() ).getTextField().setColumns( cols );
		( ( JSpinner.NumberEditor ) spinnerMax.getEditor() ).getTextField().setColumns( cols );
	}

	@Override
	public void update()
	{
		final double valueMin = modelMin.getCurrentValue();
		final double valueMax = modelMax.getCurrentValue();
		final double min = modelMin.getRangeMin();
		final double max = modelMax.getRangeMax();

		final boolean rangeChanged = ( dmax != max || dmin != min );
		if ( rangeChanged )
		{
			dmin = min;
			dmax = max;
			final SpinnerNumberModel spinnerModelMin = ( SpinnerNumberModel ) spinnerMin.getModel();
			spinnerModelMin.setMinimum( min );
			spinnerModelMin.setMaximum( max );
			final SpinnerNumberModel spinnerModelMax = ( SpinnerNumberModel ) spinnerMax.getModel();
			spinnerModelMax.setMinimum( min);
			spinnerModelMax.setMaximum( max );
		}
		slider.setLowValue(toSlider( valueMin ) );
		slider.setHighValue(toSlider( valueMax ) );
		spinnerMin.setValue( valueMin );
		spinnerMax.setValue( valueMax );

		if ( rangeChanged )
			updateNumberFormat();

		if ( rangeChanged && rangeListeners.size()>0 )
		{
			for(RangeListener rl : rangeListeners)
				rl.rangeChanged();
		}

	}

	public void addRangeListener( final RangeListener listener )
	{
		rangeListeners.add(listener);
		//this.rangeListener = listener;
	}

	private void updateNumberFormat()
	{
		if ( userDefinedNumberFormat )
			return;

		final int sw = slider.getWidth();
		if ( sw > 0 )
		{
			final double range = dmax - dmin;
			final int digits = ( int ) Math.ceil( Math.log10( sw / range ) );
			final NumberEditor numberEditorMin = ( ( JSpinner.NumberEditor ) spinnerMin.getEditor() );
			numberEditorMin.getFormat().setMaximumFractionDigits( digits );
			numberEditorMin.stateChanged( new ChangeEvent( spinnerMin ) );
			final NumberEditor numberEditorMax = ( ( JSpinner.NumberEditor ) spinnerMax.getEditor() );
			numberEditorMax.getFormat().setMaximumFractionDigits( digits );
			numberEditorMax.stateChanged( new ChangeEvent( spinnerMax ) );

		}
	}

	private int toSlider( final double value )
	{
		return ( int ) Math.round( ( value - dmin ) * sliderLength / ( dmax - dmin ) );
	}

	private double fromSlider( final int value )
	{
		return ( value * ( dmax - dmin ) / sliderLength ) + dmin;
	}
}
