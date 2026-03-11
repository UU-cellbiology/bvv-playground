package bvvpg.ui.panels;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.UIManager;

import bdv.ui.UIUtils;

public class SyncPanel extends JPanel
{	
	private Color consistentBg = Color.WHITE;

	private Color inConsistentBg = Color.WHITE;
	
	final JCheckBox cbSync = new JCheckBox(); 
	
	public SyncPanel( )
	{
		super(new GridBagLayout ());
		cbSync.setSelected( true );
		this.add( cbSync );
		updateColors();
	}

	private void updateColors()
	{
		consistentBg = UIManager.getColor( "Panel.background" );
		inConsistentBg = UIUtils.mix( consistentBg, Color.red, 0.9 );
	}
	
	public void setConsistent( final boolean isConsistent )
	{
		setBackground( isConsistent ? consistentBg : inConsistentBg );
	}
	
	public void setSelected (final boolean bSelected)
	{
		cbSync.setSelected( bSelected );
	}
	
	public boolean isSelected ()
	{
		return cbSync.isSelected();
	}
	
	public void addCheckboxListener(final ActionListener al)
	{
		cbSync.addActionListener( al );
	}
	
	public void removeCheckboxListener(final ActionListener al)
	{
		cbSync.removeActionListener( al );
	}
	
	@Override
	public void setEnabled(boolean isEnabled)
	{
		cbSync.setEnabled( isEnabled );
	}
	@Override
	public void setToolTipText(String text)
	{
		cbSync.setToolTipText( text );
	}
}
