package bvv.tools;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import bdv.util.BoundedIntervalDouble;
import bdv.util.BoundedValueDouble;

public class MaxSpinner extends JSpinner 
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8267094624266181857L;
	private BoundedValueDouble boundValue=null;
	private BoundedIntervalDouble boundInterval=null;
	public SpinnerLimit sLimit;

	public MaxSpinner(final BoundedValueDouble boundValue_, final BoundedIntervalDouble boundInterval_, final Dimension ps)
	{
		super();
		boundValue = boundValue_;
		boundInterval = boundInterval_;
		setModel( new SpinnerNumberModel(getRangeMax(), Integer.MIN_VALUE, Integer.MAX_VALUE, 1 ) );
		addChangeListener( new ChangeListener()
		{
			@Override
			public void stateChanged( final ChangeEvent e )
			{
				final double value = ( ( Number ) getValue() ).doubleValue();
				if ( value < getRangeMin() + 1 )
					setValue( getRangeMin() + 1 );
				else if ( value > sLimit.getFullRangeLimit() )
					setValue( sLimit.getFullRangeLimit() );
				else
					setRange( getRangeMin(), value );
			}
		} );
		setPreferredSize( ps );
		setBorder( BorderFactory.createEmptyBorder( 0, 10, 10, 10 ) );
	}
	
	
	public double getRangeMin()
	{
		if(boundInterval == null)
			return boundValue.getRangeMin();
		else
			return boundInterval.getRangeMin(); 
	}
	public double getRangeMax()
	{
		if(boundInterval == null)
			return boundValue.getRangeMax();
		else
			return boundInterval.getRangeMax(); 
	}
	
	public void setRange(double min, double max)
	{
		if(boundInterval == null)
			boundValue.setRange(min, max);
		else
			boundInterval.setRange(min, max);
	}
}