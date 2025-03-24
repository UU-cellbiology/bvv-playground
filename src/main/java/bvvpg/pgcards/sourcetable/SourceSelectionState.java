package bvvpg.pgcards.sourcetable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import bdv.tools.brightness.ConverterSetup;
import bdv.ui.sourcegrouptree.SourceGroupTree;
import bvvpg.source.converters.ConverterSetupsPG;

public class SourceSelectionState
{
	private final Supplier< List< ConverterSetup > > selectedConverterSetups;

	private ArrayList<Listener> listeners =	new ArrayList<>();
	
	public static interface Listener 
	{
		public void selectionCSChanged(List< ConverterSetup > csList);

	}
	public SourceSelectionState( final SourceTablePG table)
	{
		this( table::getSelectedConverterSetups);
		table.getSelectionModel().addListSelectionListener( e -> updateSelection() );
	}
	public SourceSelectionState( final SourceGroupTree tree, final ConverterSetupsPG converterSetups)
	{
		this(() -> converterSetups.getConverterSetups( tree.getSelectedSources() ));
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
	private SourceSelectionState(final Supplier< List< ConverterSetup > > selectedConverterSetups)
	{
		this.selectedConverterSetups = selectedConverterSetups;
	}
	public synchronized void updateSelection()
	{
		for(Listener l : listeners)
			l.selectionCSChanged(selectedConverterSetups.get());
	}
	public void addSourceSelectionStateListener(Listener l) 
	{
        listeners.add(l);
    }
}
