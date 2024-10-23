package btbvv.btuitools;

import java.awt.Frame;

import javax.swing.JDialog;

/**
 * A {@code JDialog} that delays {@code pack()} calls until the dialog is made visible.
 */

public class DelayedPackDialogBT extends JDialog
{
	private volatile boolean packIsPending = false;

	public DelayedPackDialogBT( Frame owner, String title, boolean modal )
	{
		super( owner, title, modal );
	}

	@Override
	public void pack()
	{
		if ( isVisible() )
		{
			packIsPending = false;
			super.pack();
		}
		else
			packIsPending = true;
	}

	@Override
	public void setVisible( boolean visible )
	{
		if ( visible && packIsPending )
		{
			packIsPending = false;
			super.pack();
		}
		super.setVisible( visible );
	}
}
