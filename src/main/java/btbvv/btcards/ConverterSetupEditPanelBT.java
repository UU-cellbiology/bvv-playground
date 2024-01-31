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

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import bdv.ui.sourcegrouptree.SourceGroupTree;
import bdv.ui.sourcetable.SourceTable;
import bdv.util.BoundedValueDouble;
import btbvv.btuitools.ConverterSetupsBT;
import net.miginfocom.swing.MigLayout;

public class ConverterSetupEditPanelBT extends JPanel
{

	private final ColorPanelBT colorPanel;

	private final BoundedRangePanelBT rangePanel;
	private final BoundedValuePanelBT gammaPanel;
	private final BoundedRangePanelBT rangeAlphaPanel;
	private final BoundedValuePanelBT gammaAlphaPanel;
	private final JCheckBox cbSync;

	public ConverterSetupEditPanelBT(
			final SourceGroupTree tree,
			final ConverterSetupsBT converterSetups )
	{
		this();
		new BoundedRangeEditorBT( tree, converterSetups, rangePanel, gammaPanel, rangeAlphaPanel, gammaAlphaPanel, cbSync);
		new ColorEditorBT( tree, converterSetups, colorPanel );
	}

	public ConverterSetupEditPanelBT(
			final SourceTable table,
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
		gammaPanel= new BoundedValuePanelBT( new BoundedValueDouble(0.01,5.0,1.0));
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
		add( new JLabel(" γ"), "" );
		add( gammaPanel, "growx, span, wrap" );
		add( new JLabel(" α"), "" );
		add( rangeAlphaPanel, "growx, span, wrap" );
		add( new JLabel(" γ α"), "" );
		add( gammaAlphaPanel, "growx, span" );
		
	}
}
