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
package bvvpg.pgcards;

import java.awt.Component;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class MouseWheelScrollListener implements MouseWheelListener {

	private final JScrollPane pane;

	private int previousValue;

	private boolean parentSearched = false;

	private Component parent = null;

	public MouseWheelScrollListener( JScrollPane pane )
	{
		this.pane = pane;
		previousValue = pane.getVerticalScrollBar().getValue();
	}

	@Override
	public void mouseWheelMoved( MouseWheelEvent e )
	{

		if ( !parentSearched )
		{
			if ( !searchParentScrollPane() )
				return;
		}
		if ( parent == null )
			return;
		JScrollBar bar = pane.getVerticalScrollBar();
		int limit = e.getWheelRotation() < 0 ? 0 : bar.getMaximum() - bar.getVisibleAmount();
		if ( previousValue == limit && bar.getValue() == limit )
		{
			parent.dispatchEvent( SwingUtilities.convertMouseEvent( pane, e, parent ) );
		}
		previousValue = bar.getValue();
	}

	private boolean searchParentScrollPane()
	{
		parentSearched = true;
		Component parent1 = pane.getParent();
		while ( !( parent1 instanceof JScrollPane ) )
		{
			if ( parent1 == null )
			{
				return false;
			}
			parent1 = parent1.getParent();
		}
		this.parent = parent1;
		return true;
	}
}
