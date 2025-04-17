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
package bvvpg.ui.panels;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.util.Objects;
import java.util.function.Supplier;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

import org.scijava.listeners.Listeners;

import bdv.ui.UIUtils;
import bdv.util.BoundedValueDouble;
import bvvpg.ui.sliders.ValueSlider;

import net.miginfocom.swing.MigLayout;

public class BoundedValuePanelPG extends JPanel {
	private Supplier< JPopupMenu > popup;

	public interface ChangeListener
	{
		void boundedValueChanged();
	}

	private BoundedValueDouble model;
	//private BoundedRange range;

	/**
	 * The range slider.
	 */
	private final JSlider slider;

	/**
	 * Range slider number of steps.
	 */
	private static final int SLIDER_LENGTH = 10000;

	/**
	 * The value spinner.
	 */
	private final JSpinner spinner;


	private final JLabel upperBoundLabel;

	private final JLabel lowerBoundLabel;

	private final Listeners.List< ChangeListener > listeners = new Listeners.SynchronizedList<>();

	/**
	 * Whether the range reflects a set of sources all having the same range
	 */
	private boolean isConsistent = true;

	/**
	 * Panel background if range reflects a set of sources all having the same range
	 */
	private Color consistentBg = Color.WHITE;

	/**
	 * Panel background if range reflects a set of sources with different ranges
	 */
	private Color inConsistentBg = Color.WHITE;

	public BoundedValuePanelPG()
	{
		//this( new BoundedRange( 0, 1, 0, 0.5 ) );
		this( new BoundedValueDouble( 0, 1, 0.5 ) );
	}

	public BoundedValuePanelPG( final BoundedValueDouble model )
	{
		setLayout( new MigLayout( "ins 5 5 5 10, fillx, filly, hidemode 3", "[grow][][]", "[]0[]" ) );
		updateColors();
		spinner = new JSpinner(new SpinnerNumberModel( 0.0, 0.0, 1.0, 1.0 ));
		//minSpinner = new JSpinner( new SpinnerNumberModel( 0.0, 0.0, 1.0, 1.0 ) );
		//maxSpinner = new JSpinner( new SpinnerNumberModel( 1.0, 0.0, 1.0, 1.0 ) );
		//slider = new ValueSlider( 0, SLIDER_LENGTH );
		slider = new ValueSlider( 0, SLIDER_LENGTH );
	
		upperBoundLabel = new JLabel();
		lowerBoundLabel = new JLabel();

		setupSpinner();
		setupSlider();
		setupBoundLabels();
		setupPopupMenu();

		//this.add( minSpinner, "sy 2" );
		this.add( slider, "growx, sy 2" );
		this.add( spinner, "sy 2" );
		this.add( upperBoundLabel, "right, wrap" );
		this.add( lowerBoundLabel, "right" );

		setValue( model );
	}

	@Override
	public void setEnabled( final boolean enabled )
	{
		super.setEnabled( enabled );
		if ( slider != null )
			slider.setEnabled( enabled );
		if ( spinner != null )
			spinner.setEnabled( enabled );
		if ( upperBoundLabel != null )
			upperBoundLabel.setEnabled( enabled );
		if ( lowerBoundLabel != null )
			lowerBoundLabel.setEnabled( enabled );
	}

	@Override
	public void updateUI()
	{
		super.updateUI();
		updateColors();
		if ( !isConsistent )
			setBackground( inConsistentBg );
		if ( popup != null )
		{
			final JPopupMenu menu = popup.get();
			if ( menu != null )
				SwingUtilities.updateComponentTreeUI( menu );
		}
		if ( upperBoundLabel != null )
			updateBoundLabelFonts();
	}
	
	public void setSliderForeground(final Color fg)
	{
		slider.setForeground( fg );
	}

	private void updateColors()
	{
		consistentBg = UIManager.getColor( "Panel.background" );
		inConsistentBg = UIUtils.mix( consistentBg, Color.red, 0.9 );
	}

	public void setConsistent( final boolean isConsistent )
	{
		this.isConsistent = isConsistent;
		setBackground( isConsistent ? consistentBg : inConsistentBg );
	}

	@Override
	public void setBackground( final Color bg )
	{
		super.setBackground( bg );
//		if ( slider != null )
//			slider.setBackground( bg );
		if ( spinner != null )
			spinner.setBackground( bg );
		if ( upperBoundLabel != null )
			upperBoundLabel.setBackground( bg );
		if ( lowerBoundLabel != null )
			lowerBoundLabel.setBackground( bg );
	}

	private static class UnboundedNumberEditor extends JSpinner.NumberEditor
	{
		public UnboundedNumberEditor( final JSpinner spinner )
		{
			super( spinner );
			final JFormattedTextField ftf = getTextField();
			final DecimalFormat format = ( DecimalFormat ) ( ( NumberFormatter ) ftf.getFormatter() ).getFormat();
			final NumberFormatter formatter = new NumberFormatter( format );
			formatter.setValueClass( spinner.getValue().getClass() );
			final DefaultFormatterFactory factory = new DefaultFormatterFactory( formatter );
			ftf.setFormatterFactory( factory );
		}
	}

	private void setupSpinner()
	{
		UIUtils.setPreferredWidth( spinner, 70 );

		spinner.addChangeListener( e -> {
			final double value = ( Double ) spinner.getValue();
			if ( value != model.getCurrentValue())
			{
				updateValue( new BoundedValueDouble(model.getRangeMin(),model.getRangeMax(),value) );
			}
		} );

		spinner.setEditor( new UnboundedNumberEditor( spinner ) );
	}


	private void setupSlider()
	{
		UIUtils.setPreferredWidth( slider, 50 );
		slider.setValue((int)Math.round(0.5*SLIDER_LENGTH) );
		slider.setFocusable( false );

		slider.addChangeListener( e -> {
			//updateRange( range.withMin( posToValue( rangeSlider.getValue() ) ).withMax( posToValue( rangeSlider.getUpperValue() ) ) );
			updateValue( new BoundedValueDouble(model.getRangeMin(),model.getRangeMax(), posToValue( slider.getValue() )));
		} );

		slider.addComponentListener( new ComponentAdapter()
		{
			@Override
			public void componentResized( final ComponentEvent e )
			{
				updateNumberFormat();
			}
		} );
	}

	private void setupBoundLabels()
	{
		upperBoundLabel.setBorder( new EmptyBorder( 0, 0, 0, 0 ) );
		lowerBoundLabel.setBorder( new EmptyBorder( 0, 0, 0, 0 ) );
		updateBoundLabelFonts();
	}

	private void updateBoundLabelFonts()
	{
		final Font labelFont = UIManager.getFont( "Label.font" );
		final Font font = new Font( labelFont.getName(), labelFont.getStyle(), 10 );
		upperBoundLabel.setFont( font );
		lowerBoundLabel.setFont( font );
	}

	private void setupPopupMenu()
	{
		final MouseListener ml = new MouseAdapter()
		{
			@Override
			public void mousePressed( final MouseEvent e )
			{
				if ( e.isPopupTrigger() ||
						( e.getButton() == MouseEvent.BUTTON1 && e.getX() > upperBoundLabel.getX() ) )
					doPop( e );
			}

			@Override
			public void mouseReleased( final MouseEvent e )
			{
				if ( e.isPopupTrigger() )
					doPop( e );
			}

			private void doPop( final MouseEvent e )
			{
				if ( isEnabled() && popup != null )
				{
					final JPopupMenu menu = popup.get();
					if ( menu != null )
						menu.show( e.getComponent(), e.getX(), e.getY() );
				}
			}
		};
		this.addMouseListener( ml );
		slider.addMouseListener( ml );
	}

	/**
	 * Convert range-slider position to value.
	 *
	 * @param pos
	 *            of range-slider
	 */
	private double posToValue( final int pos )
	{
		final double dmin = model.getRangeMin();
		final double dmax = model.getRangeMax();
		return ( pos * ( dmax - dmin ) / SLIDER_LENGTH ) + dmin;
	}

	/**
	 * Convert value to range-slider position.
	 */
	private int valueToPos( final double value )
	{
		final double dmin = model.getRangeMin();
		final double dmax = model.getRangeMax();
		return ( int ) Math.round( ( value - dmin ) * SLIDER_LENGTH / ( dmax - dmin ) );
	}

	private synchronized void updateNumberFormat()
	{
//		if ( userDefinedNumberFormat )
//			return;

		final int sw = slider.getWidth();
		if ( sw > 0 )
		{
			final double vrange = model.getRangeMax() - model.getRangeMin();
			final int digits = ( int ) Math.ceil( Math.log10( sw / vrange ) );

			blockUpdates = true;

			JSpinner.NumberEditor numberEditor = ( ( JSpinner.NumberEditor ) spinner.getEditor() );
			numberEditor.getFormat().setMaximumFractionDigits( digits );
			numberEditor.stateChanged( new ChangeEvent( spinner ) );

			blockUpdates = false;
		}
	}

	private synchronized void updateValue( final BoundedValueDouble newModel )
	{
		if ( !blockUpdates )
			setValue( newModel );
	}

	private boolean blockUpdates = false;

	public synchronized void setValue( final BoundedValueDouble model )
	{
		if ( Objects.equals( this.model, model) )
			return;

		this.model = model;

		blockUpdates = true;

		final double minBound = model.getRangeMin();
		final double maxBound = model.getRangeMax();

		final SpinnerNumberModel spinnerModel = ( SpinnerNumberModel ) spinner.getModel();
		spinnerModel.setMinimum( minBound );
		spinnerModel.setMaximum( maxBound );
		spinnerModel.setValue( model.getCurrentValue() );
		slider.setValue(valueToPos(model.getCurrentValue()));
		//rangeSlider.setRange( valueToPos( range.getMin() ), valueToPos( range.getMax() ) );

		final double frac = Math.max(
				Math.abs( Math.round( minBound ) - minBound ),
				Math.abs( Math.round( maxBound ) - maxBound ) );
		final String format = frac > 0.005 ? "%.2f" : "%.0f";
		upperBoundLabel.setText( String.format( format, maxBound ) );
		lowerBoundLabel.setText( String.format( format, minBound ) );
		this.invalidate();

		blockUpdates = false;

		listeners.list.forEach( ChangeListener::boundedValueChanged );
	}

	public BoundedValueDouble getValue()
	{
		return model;
	}

	public Listeners< ChangeListener > changeListeners()
	{
		return listeners;
	}

	public void setPopup( final Supplier< JPopupMenu > popup )
	{
		this.popup = popup;
	}

//	public void shrinkBoundsToRange()
//	{
//		updateRange( range.withMinBound( range.getMin() ).withMaxBound( range.getMax() ) );
//	}

	public void setBoundsDialog()
	{
		final JPanel panel = new JPanel( new MigLayout( "fillx", "[][grow]", "" ) );
		final JSpinner minSpinner = new JSpinner( new SpinnerNumberModel( 0.0, 0.0, 1.0, 1.0 ) );
		final JSpinner maxSpinner = new JSpinner( new SpinnerNumberModel( 0.0, 0.0, 1.0, 1.0 ) );
		minSpinner.setEditor( new UnboundedNumberEditor( minSpinner ) );
		maxSpinner.setEditor( new UnboundedNumberEditor( maxSpinner ) );
		minSpinner.setValue( model.getRangeMin());
		maxSpinner.setValue( model.getRangeMax());
		minSpinner.addChangeListener( e -> {
			final double value = ( Double ) minSpinner.getValue();
			if ( value > ( Double ) maxSpinner.getValue() )
				maxSpinner.setValue( value );
		} );
		maxSpinner.addChangeListener( e -> {
			final double value = ( Double ) maxSpinner.getValue();
			if ( value < ( Double ) minSpinner.getValue() )
				minSpinner.setValue( value );
		} );
		panel.add( "right", new JLabel( "min" ) );
		panel.add( "growx, wrap", minSpinner );
		panel.add( "right", new JLabel( "max" ) );
		panel.add( "growx", maxSpinner );
		final int result = JOptionPane.showConfirmDialog( null, panel, "Set Bounds", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE );
		if ( result == JOptionPane.YES_OPTION )
		{
			final double min = ( Double ) minSpinner.getValue();
			final double max = ( Double ) maxSpinner.getValue();
			
			//updateValue( range.withMinBound( min ).withMaxBound( max ) );
			updateValue(new BoundedValueDouble(min,max,model.getCurrentValue()));
		}
	}
}
