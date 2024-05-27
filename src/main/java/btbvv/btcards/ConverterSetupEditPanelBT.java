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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import bdv.ui.sourcegrouptree.SourceGroupTree;
import bdv.util.BoundedValueDouble;
import btbvv.btcards.sourcetable.SourceTableBT;
import btbvv.btuitools.ConverterSetupsBT;
import net.miginfocom.swing.MigLayout;

public class ConverterSetupEditPanelBT extends JPanel
{

	private final ColorPanelBT colorPanel;

	private final BoundedRangePanelBT rangePanel;
	private final BoundedValuePanelBT gammaPanel;
	private final BoundedRangePanelBT rangeAlphaPanel;
	private final BoundedValuePanelBT gammaAlphaPanel;
	
	private final JButton expandButton;
	private final JPanel extendedPanel;
	private final JCheckBox cbSync;
	private final ImageIcon expandIcon; 
	private final ImageIcon collapseIcon; 

	
	private boolean bExpanded = false;

	public ConverterSetupEditPanelBT(
			final SourceGroupTree tree,
			final ConverterSetupsBT converterSetups )
	{
		this();
		new BoundedRangeEditorBT( tree, converterSetups, rangePanel, gammaPanel, rangeAlphaPanel, gammaAlphaPanel, cbSync);
		new ColorEditorBT( tree, converterSetups, colorPanel );
	}

	public ConverterSetupEditPanelBT(
			final SourceTableBT table,
			final ConverterSetupsBT converterSetups )
	{
		this();
		new BoundedRangeEditorBT( table, converterSetups, rangePanel, gammaPanel, rangeAlphaPanel,gammaAlphaPanel, cbSync);
		new ColorEditorBT( table, converterSetups, colorPanel );
	}

	public ConverterSetupEditPanelBT()
	{
		super( new MigLayout( "ins 0, fillx, hidemode 3", "[min!]0[]0[]", "[]2[]2[]2[]2[]" ) );
		colorPanel = new ColorPanelBT();
		rangePanel = new BoundedRangePanelBT();
		gammaPanel = new BoundedValuePanelBT( new BoundedValueDouble(0.01,5.0,1.0));
		rangeAlphaPanel = new BoundedRangePanelBT();
		gammaAlphaPanel = new BoundedValuePanelBT( new BoundedValueDouble(0.01,5.0,1.0));
		cbSync = new JCheckBox();
		cbSync.setSelected(true);

		//( ( MigLayout ) rangePanel.getLayout() ).setLayoutConstraints( "fillx, filly, hidemode 3" );
		//( ( MigLayout ) gammaPanel.getLayout() ).setLayoutConstraints( "ins 5 5 5 10, fillx, filly, hidemode 3" );
		
		String sLayoutConstraints = "ins 0 0 0 5, fillx, filly, hidemode 3" ;
		( ( MigLayout ) rangePanel.getLayout() ).setLayoutConstraints( sLayoutConstraints );
		( ( MigLayout ) gammaPanel.getLayout() ).setLayoutConstraints( sLayoutConstraints );
		( ( MigLayout ) rangeAlphaPanel.getLayout() ).setLayoutConstraints(sLayoutConstraints );
		( ( MigLayout ) gammaAlphaPanel.getLayout() ).setLayoutConstraints(sLayoutConstraints );

		add( new JLabel(" "), "" );
		add( colorPanel, "growy" );
		add( new JLabel(" Color "), "growy" );
		add( cbSync, "growy" );
		add( new JLabel(" Sync LUT -> α "), "growy, wrap" );
		add( new JLabel("LUT"), "" );
		add( rangePanel, "growx, span, wrap" );
		
		extendedPanel = new JPanel();
		extendedPanel.setLayout( new MigLayout( "ins 0 0 0 0, fillx, filly, hidemode 3", "[][grow][]", "[]0[]" ) );
		extendedPanel.add( new JLabel(" γ"), "" );
		extendedPanel.add( gammaPanel, "growx, span, wrap" );
		extendedPanel.add( new JLabel(" α"), "" );
		extendedPanel.add( rangeAlphaPanel, "growx, span, wrap" );
		extendedPanel.add( new JLabel(" γ α"), "" );
		extendedPanel.add( gammaAlphaPanel, "growx, span" );

		extendedPanel.setVisible( false );
		expandButton = new JButton();
		URL icon_path = btbvv.btcards.ConverterSetupEditPanelBT.class.getResource("/icons/expand.png");
		expandIcon = new ImageIcon(icon_path);
		icon_path = btbvv.btcards.ConverterSetupEditPanelBT.class.getResource("/icons/collapse.png");
		collapseIcon = new ImageIcon(icon_path);
		expandButton.setForeground( this.getBackground() );
		expandButton.setIcon( expandIcon );
		expandButton.setPreferredSize( new Dimension(10,14) );
		expandButton.setMinimumSize( new Dimension(10,14) );
		expandButton.setFocusable( false );
		expandButton.setBorderPainted( false );
		expandButton.setOpaque( true );
		expandButton.addActionListener( new ActionListener() 
				{

					@Override
					public void actionPerformed( ActionEvent e )
					{

						bExpanded = !bExpanded;	
						if(bExpanded)	
						{
							expandButton.setIcon( collapseIcon );
						}
						else
						{
							expandButton.setIcon( expandIcon );
						}
						extendedPanel.setVisible( bExpanded  );
						expandButton.setSelected( false );
						
					}
			
				});
		add(expandButton, "growx, span, wrap" );
		add(extendedPanel,"growx, span, wrap");
		
	}
}
