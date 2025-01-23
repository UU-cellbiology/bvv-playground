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
package bvvpg.pguitools;

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
	private JSpinner syncSpinner;
	public SpinnerLimit sLimit;
	public boolean bSync;

	public MinSpinner(BoundedValueDouble boundValue_, BoundedIntervalDouble boundInterval_,final Dimension ps, final JSpinner syncSpinner_, final boolean bSync_)
	{
		super();
		boundValue = boundValue_;
		boundInterval = boundInterval_;
		syncSpinner = syncSpinner_;
		bSync = bSync_;
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
				if(bSync)
					syncSpinner.setValue(getValue());
				
			}
		} );
		setPreferredSize( ps );
		setBorder( BorderFactory.createEmptyBorder( 0, 10, 10, 10 ) );
	}
	
	
	public double getRangeMin()
	{
		if(boundInterval == null)
			return boundValue.getRangeMin();
		return boundInterval.getRangeMin(); 
	}
	public double getRangeMax()
	{
		if(boundInterval == null)
			return boundValue.getRangeMax();
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
