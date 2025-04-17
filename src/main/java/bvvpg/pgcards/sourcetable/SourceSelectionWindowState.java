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
