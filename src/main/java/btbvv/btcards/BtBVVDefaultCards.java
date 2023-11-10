package btbvv.btcards;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.border.MatteBorder;
import javax.swing.tree.TreeSelectionModel;

import bdv.ui.CardPanel;
import bdv.ui.convertersetupeditor.ConverterSetupEditPanel;
import bdv.ui.sourcegrouptree.SourceGroupTree;
import bdv.ui.sourcetable.SourceTable;
import bdv.ui.viewermodepanel.DisplaySettingsPanel;
import bdv.viewer.AbstractViewerPanel;
import bdv.viewer.ConverterSetups;
import bdv.viewer.ViewerState;
import btbvv.btuitools.ConverterSetupsBT;

public class BtBVVDefaultCards {
	public static final String DEFAULT_SOURCES_CARD = "default bdv sources card";

	public static final String DEFAULT_SOURCEGROUPS_CARD = "default bdv groups card";

	public static final String DEFAULT_VIEWERMODES_CARD = "default bdv viewer modes card";

	public static void setup( final CardPanel cards, final AbstractViewerPanel viewer, final ConverterSetupsBT converterSetups )
	{
		final ViewerState state = viewer.state();

		// -- Sources table --
		final SourceTable table = new SourceTable( state, converterSetups, viewer.getInputTriggerConfig() );
		table.setPreferredScrollableViewportSize( new Dimension( 300, 200 ) );
		table.setFillsViewportHeight( true );
		table.setDragEnabled( true );
		final ConverterSetupEditPanelBT editPanelTableBT = new ConverterSetupEditPanelBT( table, converterSetups );
		final JPanel tablePanel = new JPanel( new BorderLayout() );
		//final JPanel tablePanel = new JPanel( );
		final JScrollPane scrollPaneTable = new MyScrollPane( table, "Table.background" );
		scrollPaneTable.addMouseWheelListener( new MouseWheelScrollListener( scrollPaneTable ) );
		tablePanel.add( scrollPaneTable, BorderLayout.CENTER );
		tablePanel.add( editPanelTableBT, BorderLayout.SOUTH );
		tablePanel.setPreferredSize( new Dimension( 300, 285 ) );

		// -- Groups tree --
		final SourceGroupTree tree = new SourceGroupTree( state, viewer.getInputTriggerConfig() );
//		tree.setPreferredSize( new Dimension( 300, 200 ) );
		tree.setVisibleRowCount( 10 );
		tree.setEditable( true );
		tree.setSelectionRow( 0 );
		tree.setRootVisible( false );
		tree.setShowsRootHandles( true );
		tree.setExpandsSelectedPaths( true );
		tree.getSelectionModel().setSelectionMode( TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION );
		final ConverterSetupEditPanelBT editPanelTree = new ConverterSetupEditPanelBT( tree, converterSetups );
		//final ConverterSetupEditPanel editPanelTree = new ConverterSetupEditPanel( tree, converterSetups );
		final JPanel treePanel = new JPanel( new BorderLayout() );
		final JScrollPane scrollPaneTree = new MyScrollPane( tree, "Tree.background" );
		scrollPaneTree.addMouseWheelListener( new MouseWheelScrollListener( scrollPaneTree ) );
		treePanel.add( scrollPaneTree, BorderLayout.CENTER );
		treePanel.add( editPanelTree, BorderLayout.SOUTH );
		treePanel.setPreferredSize( new Dimension( 300, 225 ) );

		cards.addCard( DEFAULT_VIEWERMODES_CARD, "Display Modes", new DisplaySettingsPanel( viewer.state() ), true, new Insets( 0, 4, 4, 0 ) );
		cards.addCard( DEFAULT_SOURCES_CARD, "Sources", tablePanel, true, new Insets( 0, 0, 0, 0 ) );
		cards.addCard( DEFAULT_SOURCEGROUPS_CARD, "Groups", treePanel, true, new Insets( 0, 0, 0, 0 ) );
	}

	static class MyScrollPane extends JScrollPane
	{
		private final String bgColorName;

		public MyScrollPane( final Component view, final String bgColorName )
		{
			super( view );
			this.bgColorName = bgColorName;
			updateBorder();
		}

		@Override
		public void updateUI()
		{
			super.updateUI();
			if ( bgColorName != null )
				updateBorder();
		}

		private void updateBorder()
		{
			setBorder( new MatteBorder( 0, 4, 0, 0, UIManager.getColor( bgColorName ) ) );
		}
	}
}
