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
		gammaPanel= new BoundedValuePanelBT( new BoundedValueDouble(0.1,5.0,1.0));
		rangeAlphaPanel = new BoundedRangePanelBT();
		gammaAlphaPanel = new BoundedValuePanelBT( new BoundedValueDouble(0.1,5.0,1.0));
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
