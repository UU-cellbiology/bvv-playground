package btbvv.btcards;

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

	public ConverterSetupEditPanelBT(
			final SourceGroupTree tree,
			final ConverterSetupsBT converterSetups )
	{
		this();
		new BoundedRangeEditorBT( tree, converterSetups, rangePanel, gammaPanel, rangeAlphaPanel, gammaAlphaPanel);
		new ColorEditorBT( tree, converterSetups, colorPanel );
	}

	public ConverterSetupEditPanelBT(
			final SourceTable table,
			final ConverterSetupsBT converterSetups )
	{
		this();
		new BoundedRangeEditorBT( table, converterSetups, rangePanel, gammaPanel, rangeAlphaPanel,gammaAlphaPanel);
		new ColorEditorBT( table, converterSetups, colorPanel );
	}

	public ConverterSetupEditPanelBT()
	{
		super( new MigLayout( "ins 0, fillx, hidemode 3", "[]0[grow]", "" ) );
		colorPanel = new ColorPanelBT();
		rangePanel = new BoundedRangePanelBT();
		gammaPanel= new BoundedValuePanelBT( new BoundedValueDouble(0.1,5.0,1.0));
		rangeAlphaPanel = new BoundedRangePanelBT();
		gammaAlphaPanel = new BoundedValuePanelBT( new BoundedValueDouble(0.1,5.0,1.0));
		//gammaPanel = new BoundedRangePanelBT();

		//( ( MigLayout ) rangePanel.getLayout() ).setLayoutConstraints( "fillx, filly, hidemode 3" );
		String sLayoutConstraints = "ins 0 5 0 10, fillx, filly, hidemode 3" ;
		( ( MigLayout ) rangePanel.getLayout() ).setLayoutConstraints( sLayoutConstraints );
		( ( MigLayout ) gammaPanel.getLayout() ).setLayoutConstraints( sLayoutConstraints );
		( ( MigLayout ) rangeAlphaPanel.getLayout() ).setLayoutConstraints(sLayoutConstraints );
		( ( MigLayout ) gammaAlphaPanel.getLayout() ).setLayoutConstraints(sLayoutConstraints );
		//( ( MigLayout ) rangePanel.getLayout() ).setLayoutConstraints( sLayoutConstraints );
		//( ( MigLayout ) gammaPanel.getLayout() ).setLayoutConstraints( "ins 5 5 5 10, fillx, filly, hidemode 3" );
		add( colorPanel, "growy" );
		add( rangePanel, "growx, wrap" );
		add( new JLabel(" γ"), "growy" );
		add( gammaPanel, "growx, wrap" );
		add( new JLabel(" α"), "growy" );
		add( rangeAlphaPanel, "growx, wrap" );
		add( new JLabel(" γ α"), "growy" );
		add( gammaAlphaPanel, "growx" );
		
	}
}
