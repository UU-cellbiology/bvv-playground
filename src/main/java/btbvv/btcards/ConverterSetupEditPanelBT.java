package btbvv.btcards;

import javax.swing.JLabel;
import javax.swing.JPanel;

import bdv.tools.brightness.SliderPanelDouble;
import bdv.ui.sourcegrouptree.SourceGroupTree;
import bdv.ui.sourcetable.SourceTable;
import bdv.util.BoundedValueDouble;
import bdv.viewer.ConverterSetups;
import net.miginfocom.swing.MigLayout;

public class ConverterSetupEditPanelBT extends JPanel
{

	private final ColorPanelBT colorPanel;

	private final BoundedRangePanelBT rangePanel;
	private final BoundedValuePanelBT gammaPanel;

	public ConverterSetupEditPanelBT(
			final SourceGroupTree tree,
			final ConverterSetups converterSetups )
	{
		this();
		new BoundedRangeEditorBT( tree, converterSetups, rangePanel, gammaPanel, converterSetups.getBounds() );
		new ColorEditorBT( tree, converterSetups, colorPanel );
	}

	public ConverterSetupEditPanelBT(
			final SourceTable table,
			final ConverterSetups converterSetups )
	{
		this();
		new BoundedRangeEditorBT( table, converterSetups, rangePanel, gammaPanel, converterSetups.getBounds() );
		new ColorEditorBT( table, converterSetups, colorPanel );
	}

	public ConverterSetupEditPanelBT()
	{
		super( new MigLayout( "ins 0, fillx, hidemode 3", "[]0[grow]", "" ) );
		colorPanel = new ColorPanelBT();
		rangePanel = new BoundedRangePanelBT();
		gammaPanel= new BoundedValuePanelBT( new BoundedValueDouble(0.1,5.0,1.0));
		//gammaPanel = new BoundedRangePanelBT();

		//( ( MigLayout ) rangePanel.getLayout() ).setLayoutConstraints( "fillx, filly, hidemode 3" );
		( ( MigLayout ) rangePanel.getLayout() ).setLayoutConstraints( "ins 5 5 5 10, fillx, filly, hidemode 3" );
		//( ( MigLayout ) gammaPanel.getLayout() ).setLayoutConstraints( "ins 5 5 5 10, fillx, filly, hidemode 3" );
		add( colorPanel, "growy" );
		add( rangePanel, "growx, wrap" );
		add( new JLabel("γ LUT"), "growy" );
		add( gammaPanel, "growx" );
	}
}
