package btbvv.btcards;

import java.util.List;
import java.util.function.Supplier;

import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import bdv.tools.brightness.ConverterSetup;
import bdv.ui.sourcegrouptree.SourceGroupTree;
import bdv.ui.sourcetable.SourceTable;
import bdv.viewer.ConverterSetups;
import net.imglib2.type.numeric.ARGBType;

public class ColorEditorBT {
	private final Supplier< List< ConverterSetup > > selectedConverterSetups;

	private final ColorPanelBT colorPanel;

	public ColorEditorBT(
			final SourceTable table,
			final ConverterSetups converterSetups,
			final ColorPanelBT colorPanel )
	{
		this( table::getSelectedConverterSetups, converterSetups, colorPanel );
		table.getSelectionModel().addListSelectionListener( e -> updateSelection() );
	}

	public ColorEditorBT(
			final SourceGroupTree tree,
			final ConverterSetups converterSetups,
			final ColorPanelBT colorPanel )
	{
		this(
				() -> converterSetups.getConverterSetups( tree.getSelectedSources() ),
				converterSetups, colorPanel );
		tree.getSelectionModel().addTreeSelectionListener( e -> updateSelection() );
		tree.getModel().addTreeModelListener( new TreeModelListener()
		{
			@Override
			public void treeNodesChanged( final TreeModelEvent e )
			{
				updateSelection();
			}

			@Override
			public void treeNodesInserted( final TreeModelEvent e )
			{
				updateSelection();
			}

			@Override
			public void treeNodesRemoved( final TreeModelEvent e )
			{
				updateSelection();
			}

			@Override
			public void treeStructureChanged( final TreeModelEvent e )
			{
				updateSelection();
			}
		} );
	}

	private ColorEditorBT(
			final Supplier< List< ConverterSetup > > selectedConverterSetups,
			final ConverterSetups converterSetups,
			final ColorPanelBT colorPanel )
	{
		this.selectedConverterSetups = selectedConverterSetups;
		this.colorPanel = colorPanel;

		colorPanel.changeListeners().add( this::updateConverterSetupColors );
		converterSetups.listeners().add( s -> updateColorPanel() );
	}

	private boolean blockUpdates = false;

	private List< ConverterSetup > converterSetups;

	private synchronized void updateConverterSetupColors()
	{
		if ( blockUpdates || converterSetups == null || converterSetups.isEmpty() )
			return;

		ARGBType color = colorPanel.getColor();

		for ( final ConverterSetup converterSetup : converterSetups )
		{
			if ( converterSetup.supportsColor() )
				converterSetup.setColor( color );
		}

		updateColorPanel();
	}

	private synchronized void updateSelection()
	{
		converterSetups = selectedConverterSetups.get();
		updateColorPanel();
	}

	private synchronized void updateColorPanel()
	{
		if ( converterSetups == null || converterSetups.isEmpty() )
		{
			SwingUtilities.invokeLater( () -> {
				colorPanel.setEnabled( false );
				colorPanel.setColor( null );
				colorPanel.setConsistent( true );
			} );
		}
		else
		{
			ARGBType color = null;
			boolean allColorsEqual = true;
			for ( final ConverterSetup converterSetup : converterSetups )
			{
				if ( converterSetup.supportsColor() )
				{
					if ( color == null )
						color = converterSetup.getColor();
					else
						allColorsEqual &= color.equals( converterSetup.getColor() );
				}
			}
			final ARGBType finalColor = color;
			final boolean isConsistent = allColorsEqual;
			SwingUtilities.invokeLater( () -> {
				synchronized ( ColorEditorBT.this )
				{
					blockUpdates = true;
					colorPanel.setEnabled( finalColor != null );
					colorPanel.setColor( finalColor );
					colorPanel.setConsistent( isConsistent );
					blockUpdates = false;
				}
			} );
		}
	}
}
