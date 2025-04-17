/*-
 * #%L
 * Volume rendering of bdv datasets with gamma and transparency option
 * %%
 * Copyright (C) 2022 - 2025 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
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
	
	public SourceTablePG table = null;
	
	public static interface Listener 
	{
		public void selectionCSChanged(List< ConverterSetup > csList);

	}
	public SourceSelectionState(final SourceTablePG table)
	{
		this( table::getSelectedConverterSetups);
		table.getSelectionModel().addListSelectionListener( e -> updateSelection() );
		this.table = table;
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
