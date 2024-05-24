/*-
 * #%L
 * Volume rendering of bdv datasets with gamma and transparency option
 * %%
 * Copyright (C) 2022 - 2024 Cell Biology, Neurobiology and Biophysics
 * Department of Utrecht University.
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
package btbvv.btcards;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.IndexColorModel;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.scijava.listeners.Listeners;

import bdv.ui.UIUtils;
import btbvv.btuitools.ColorIconBT;
import ij.IJ;
import ij.plugin.LutLoader;

import net.imglib2.type.numeric.ARGBType;
import net.miginfocom.swing.MigLayout;

public class ColorPanelBT extends JPanel 
{
	private final JButton colorButton;

	private final ARGBType color = new ARGBType();
	
	private IndexColorModel icm = null;
	
	private String icmName = null;

	public interface ChangeListener
	{
		void colorChanged();
	}

	private final Listeners.List< ChangeListener > listeners = new Listeners.SynchronizedList<>();

	/**
	 * Whether the color reflects a set of sources all having the same color
	 */
	private boolean isConsistent = true;

	/**
	 * Panel background if color reflects a set of sources all having the same color
	 */
	private Color consistentBg = Color.WHITE;

	/**
	 * Panel background if color reflects a set of sources with different colors
	 */
	private Color inConsistentBg = Color.WHITE;

	public ColorPanelBT()
	{
		setLayout( new MigLayout( "ins 0, fillx, filly, hidemode 3", "[grow]", "" ) );
		updateColors();

		colorButton = new JButton();
		this.add( colorButton, "center" );

		colorButton.addActionListener( e -> chooseColor() );

		colorButton.setBorderPainted( false );
		colorButton.setFocusPainted( false );
		colorButton.setContentAreaFilled( false );
		//colorButton.setMinimumSize( new Dimension( 46, 42 ) );
		//colorButton.setPreferredSize( new Dimension( 46, 42 ) );
		colorButton.setMinimumSize( new Dimension( 32, 30 ) );
		colorButton.setPreferredSize( new Dimension( 32, 30 ) );

		colorButton.addMouseListener( new MouseAdapter()
		{ 
			@Override
			public void mouseClicked(MouseEvent evt)
			{
			    
			    if (SwingUtilities.isRightMouseButton(evt)&& colorButton.isEnabled()) 
			    {
			    	JPopupMenu popup = new JPopupMenu();
			    	String [] luts = IJ.getLuts();
			    	JMenuItem itemMenu;
			    	for(int i = 0; i<luts.length;i++)
			    	{
			    		itemMenu =  new  JMenuItem(luts[i]);
			    		itemMenu.addActionListener( new ActionListener()
			    		{

			    			@Override
			    			public void actionPerformed( ActionEvent arg0 )
			    			{
			    				//System.out.println(((JMenuItem)arg0.getSource()).getText());
			    				String sLUTName = ((JMenuItem)arg0.getSource()).getText();
			    				setICMbyName(sLUTName);
			    				listeners.list.forEach( ChangeListener::colorChanged );

			    			}

			    		});
			    		popup.add(itemMenu);  
			    	}
			    	//menuItem.addActionListener(this);
			    	popup.show( evt.getComponent(), evt.getX(), evt.getY() );
			    }
			}
		});
		
		
		setColor( null );
	}

	@Override
	public void setEnabled( final boolean enabled )
	{
		super.setEnabled( enabled );
		if ( colorButton != null )
			colorButton.setEnabled( enabled );
	}

	@Override
	public void updateUI()
	{
		super.updateUI();
		updateColors();
		if ( !isConsistent )
			setBackground( inConsistentBg );
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

	private void chooseColor()
	{
		final Color newColor = JColorChooser.showDialog( null, "Set Source Color", new Color( color.get() ) );
		if ( newColor == null )
			return;
		setColor( new ARGBType(  newColor.getRGB() | 0xff000000 ) );
		listeners.list.forEach( ChangeListener::colorChanged );
	}

	public Listeners< ChangeListener > changeListeners()
	{
		return listeners;
	}

	public synchronized void setColor( final ARGBType color )
	{
		if ( color == null )
			this.color.set( 0xffaaaaaa );
		else
			this.color.set( color );
		icm = null;
		icmName = null;
		//colorButton.setIcon( new ColorIcon( new Color( this.color.get() ), 30, 30, 10, 10, true ) );
		colorButton.setIcon( new ColorIconBT( new Color( this.color.get() ), null, 21, 21, 7, 7, true ) );
	}

	public void setICM(IndexColorModel icm_, String icmName_)
	{
		this.icm = icm_;
		this.icmName = icmName_;
		colorButton.setIcon( new ColorIconBT( null, icm, 21, 21, 7, 7, true ) );
	}
	public synchronized void setICMbyName(String icmName)
	{
		setICM(LutLoader.getLut(icmName), icmName);
	}

	public ARGBType getColor()
	{
		return color.copy();
	}
	public IndexColorModel getICM()
	{
		return icm;
	}
	public String getICMName()
	{
		return icmName;
	}
}
