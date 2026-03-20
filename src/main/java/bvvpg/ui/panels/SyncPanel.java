/*-
 * #%L
 * Volume rendering of bdv datasets with gamma and transparency option
 * %%
 * Copyright (C) 2022 - 2026 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
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
