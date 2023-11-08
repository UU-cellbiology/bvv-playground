package btbvv.btcards;

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
		Component parent = pane.getParent();
		while ( !( parent instanceof JScrollPane ) )
		{
			if ( parent == null )
			{
				return false;
			}
			parent = parent.getParent();
		}
		this.parent = parent;
		return true;
	}
}
