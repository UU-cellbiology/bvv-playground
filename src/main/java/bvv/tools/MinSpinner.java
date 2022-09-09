package bvv.tools;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import bdv.util.BoundedIntervalDouble;
import bdv.util.BoundedValueDouble;

public class MinSpinner extends JSpinner
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1724117364653045101L;
	private BoundedValueDouble boundValue=null;
	private BoundedIntervalDouble boundInterval=null;
	public SpinnerLimit sLimit;

	public MinSpinner(BoundedValueDouble boundValue_, BoundedIntervalDouble boundInterval_,final Dimension ps)
	{
		super();
		boundValue = boundValue_;
		boundInterval = boundInterval_;
		setModel( new SpinnerNumberModel(getRangeMin(), Integer.MIN_VALUE, Integer.MAX_VALUE, 1 ) );
		addChangeListener( new ChangeListener()
		{
			@Override
			public void stateChanged( final ChangeEvent e )
			{
				final double value = ( ( Number ) getValue() ).doubleValue();
				if ( value < sLimit.getFullRangeLimit() )
					setValue( sLimit.getFullRangeLimit());
				else if ( value > getRangeMax() - 1 )
					setValue( getRangeMax() - 1);
				else
					setRange( value, getRangeMax() );
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
	/*public double getFullRangeMin()
	{
		return 0.0;
	}*/
	
	public void setRange(double min, double max)
	{
		if(boundInterval == null)
			boundValue.setRange(min, max);
		else
			boundInterval.setRange(min, max);
	}

}
