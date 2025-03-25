package bvvpg.pgcards.sourcetable;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;

import bdv.tools.brightness.ConverterSetup;
import bdv.ui.sourcegrouptree.SourceGroupTree;
import bvvpg.source.converters.ConverterSetupsPG;

public class SourceSelectionWindowState implements FocusListener
{
	private ArrayList<Listener> listeners =	new ArrayList<>();
	
	final SourceTablePG table;
	final SourceGroupTree tree; 
	final ConverterSetupsPG convSetups;
	public static interface Listener 
	{
		public void selectionWindowChanged(int nWindow, List< ConverterSetup > csList);

	}
	public SourceSelectionWindowState(final SourceTablePG table, final SourceGroupTree tree, final ConverterSetupsPG converterSetups)
	{
		this.table = table;
		this.tree = tree;
		this.convSetups = converterSetups;
		
		table.addFocusListener( this );
		tree.addFocusListener( this );
	}
	@Override
	public void focusGained( FocusEvent e )
	{
		e.getSource();
		if(e.getSource() == table)
		{
			for(Listener l : listeners)
				l.selectionWindowChanged(0, table.getSelectedConverterSetups());
		}
		else
		{
			for(Listener l : listeners)
				l.selectionWindowChanged(1,  convSetups.getConverterSetups( tree.getSelectedSources() ));			
		}
		
	}
	@Override
	public void focusLost( FocusEvent e )
	{
		
		
	}
	public void addSourceSelectionWindowStateListener(Listener l) 
	{
        listeners.add(l);
    }
}
