/*-
 * #%L
 * Volume rendering of bdv datasets with gamma and transparency option
 * %%
 * Copyright (C) 2022 - 2026 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
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
package bvvpg.ui.panels;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import bdv.tools.brightness.ConverterSetup;
import bdv.ui.sourcegrouptree.SourceGroupTree;
import bdv.util.BoundedRange;
import bdv.util.BoundedValueDouble;
import bdv.util.Bounds;
import bdv.viewer.ConverterSetupBounds;
import bvvpg.pgcards.sourcetable.SourceTablePG;
import bvvpg.source.converters.ConverterSetupBoundsAlpha;
import bvvpg.source.converters.ConverterSetupBoundsGamma;
import bvvpg.source.converters.ConverterSetupBoundsGammaAlpha;
import bvvpg.source.converters.ConverterSetupSyncState;
import bvvpg.source.converters.ConverterSetupsPG;
import bvvpg.source.converters.GammaConverterSetup;

public class BoundedRangeEditorPG {
	
	private final Supplier< List< ConverterSetup > > selectedConverterSetups;

	private final BoundedRangePanelPG rangePanel;
	private final BoundedValuePanelPG gammaPanel;
	private final BoundedRangePanelPG rangeAlphaPanel;
	private final BoundedValuePanelPG gammaAlphaPanel;

	private final ConverterSetupBounds converterSetupBounds;
	private final ConverterSetupBoundsGamma converterSetupBoundsGamma;
	private final ConverterSetupBoundsAlpha converterSetupBoundsAlpha;
	private final ConverterSetupBoundsGammaAlpha converterSetupBoundsGammaAlpha;
	private final ConverterSetupSyncState converterSetupSyncState;
	
	private List< ConverterSetup > singleCS;
	
	private final SyncPanel syncPanel;

	public BoundedRangeEditorPG(
			final SourceTablePG table,
			final ConverterSetupsPG converterSetups,
			final BoundedRangePanelPG rangePanel,
			final BoundedValuePanelPG gammaPanel,
			final BoundedRangePanelPG rangeAlphaPanel,
			final BoundedValuePanelPG gammaAlphaPanel,
			final SyncPanel syncPanel)
	{
		this( table::getSelectedConverterSetups, converterSetups, rangePanel, gammaPanel, rangeAlphaPanel, gammaAlphaPanel, syncPanel);
		table.getSelectionModel().addListSelectionListener( e -> updateSelection() );
	}
	public BoundedRangeEditorPG(
			final ConverterSetup singleCS,
			final ConverterSetupsPG converterSetups,
			final BoundedRangePanelPG rangePanel,
			final BoundedValuePanelPG gammaPanel,
			final BoundedRangePanelPG rangeAlphaPanel,
			final BoundedValuePanelPG gammaAlphaPanel,
			final SyncPanel syncPanel)
	{
		this(  () -> null, converterSetups, rangePanel, gammaPanel, rangeAlphaPanel, gammaAlphaPanel, syncPanel);
		if(singleCS != null)
		{
			singleCS.setupChangeListeners().add( converterSetup ->
			{
				updateSelection();
			});
			this.singleCS.add( singleCS );
		}
	}

	public BoundedRangeEditorPG(
			final SourceGroupTree tree,
			final ConverterSetupsPG converterSetups,
			final BoundedRangePanelPG rangePanel,
			final BoundedValuePanelPG gammaPanel,
			final BoundedRangePanelPG rangeAlphaPanel,
			final BoundedValuePanelPG gammaAlphaPanel,
			final SyncPanel syncPanel)
	{
		this(
				() -> converterSetups.getConverterSetups( tree.getSelectedSources() ), 
				converterSetups, rangePanel, gammaPanel, rangeAlphaPanel,gammaAlphaPanel, syncPanel);
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

	private BoundedRangeEditorPG(
			final Supplier< List< ConverterSetup > > selectedConverterSetups,
			final ConverterSetupsPG converterSetups,
			final BoundedRangePanelPG rangePanel, 
			final BoundedValuePanelPG gammaPanel, 
			final BoundedRangePanelPG rangeAlphaPanel,
			final BoundedValuePanelPG gammaAlphaPanel,
			final SyncPanel syncPanel)
	{
		this.singleCS = new ArrayList<>();
		this.selectedConverterSetups = selectedConverterSetups;
		
		this.rangePanel = rangePanel;
		this.gammaPanel = gammaPanel;
		this.rangeAlphaPanel = rangeAlphaPanel;
		this.gammaAlphaPanel = gammaAlphaPanel;
		this.converterSetupBounds = converterSetups.getBounds();
		this.converterSetupBoundsGamma = converterSetups.getBoundsGamma();
		this.converterSetupBoundsAlpha = converterSetups.getBoundsAlpha();
		this.converterSetupBoundsGammaAlpha = converterSetups.getBoundsGammaAlpha();
		this.converterSetupSyncState = converterSetups.getSyncStateMap();
		this.syncPanel = syncPanel;
		this.syncPanel.addCheckboxListener( (e)-> updateConverterSetupSyncState());

		rangePanel.changeListeners().add( this::updateConverterSetupRanges );
		gammaPanel.changeListeners().add( this::updateConverterSetupGamma);
		rangeAlphaPanel.changeListeners().add( this::updateConverterSetupRangesAlpha );
		gammaAlphaPanel.changeListeners().add( this::updateConverterSetupGammaAlpha);

		converterSetups.listeners().add( s -> updateRangesPanels() );
		converterSetups.listeners().add( s -> updateGammasPanels() );
		converterSetups.listeners().add( s -> updateSyncPanel() );

		final JPopupMenu menu = new JPopupMenu();
		menu.add( runnableItem(  "set bounds ...", rangePanel::setBoundsDialog ) );
		menu.add( setBoundsItem( "set bounds 0..1", 0, 1 ) );
		menu.add( setBoundsItem( "set bounds 0..255", 0, 255 ) );
		menu.add( setBoundsItem( "set bounds 0..65535", 0, 65535 ) );
		menu.add( runnableItem(  "shrink bounds to selection", rangePanel::shrinkBoundsToRange ) );
		rangePanel.setPopup( () -> menu );
		
		final JPopupMenu menuG = new JPopupMenu();
		menuG.add( runnableGammaItem(  "set bounds ...", gammaPanel::setBoundsDialog ) );
		menuG.add( setBoundsGammaItem( "set bounds 0.01..5", 0.01, 5 ) );
		menuG.add( setBoundsGammaItem( "set bounds 0.1..50", 0.1, 50 ) );
		gammaPanel.setPopup( () -> menuG );
		
		final JPopupMenu menuA = new JPopupMenu();
		menuA.add( runnableAlphaItem(  "set bounds ...", rangeAlphaPanel::setBoundsDialog ) );
		menuA.add( setBoundsAlphaItem( "set bounds 0..1", 0, 1 ) );
		menuA.add( setBoundsAlphaItem( "set bounds 0..255", 0, 255 ) );
		menuA.add( setBoundsAlphaItem( "set bounds 0..65535", 0, 65535 ) );
		menuA.add( runnableAlphaItem(  "shrink bounds to selection", rangeAlphaPanel::shrinkBoundsToRange ) );
		rangeAlphaPanel.setPopup( () -> menuA );

		
		final JPopupMenu menuGA = new JPopupMenu();
		menuGA.add( runnableGammaAlphaItem(  "set bounds ...", gammaAlphaPanel::setBoundsDialog ) );
		menuGA.add( setBoundsGammaAlphaItem( "set bounds 0.01..5", 0.01, 5 ) );
		menuGA.add( setBoundsGammaAlphaItem( "set bounds 0.1..50", 0.1, 50 ) );
		gammaAlphaPanel.setPopup( () -> menuGA );
		
		updateRangesPanels();
		updateGammasPanels();
		updateSyncPanel();
		
	}

	private JMenuItem setBoundsItem( final String text, final double min, final double max )
	{
		final JMenuItem item = new JMenuItem( text );
		item.addActionListener( e -> setBounds( new Bounds( min, max ) ) );
		return item;
	}

	private JMenuItem runnableItem( final String text, final Runnable action )
	{
		final JMenuItem item = new JMenuItem( text );
		item.addActionListener( e -> action.run() );
		return item;
	}

	private JMenuItem setBoundsGammaItem( final String text, final double min, final double max )
	{
		final JMenuItem item = new JMenuItem( text );
		item.addActionListener( e -> setBoundsGamma( new Bounds( min, max ) ) );
		return item;
	}

	private JMenuItem runnableGammaItem( final String text, final Runnable action )
	{
		final JMenuItem item = new JMenuItem( text );
		item.addActionListener( e -> action.run() );
		return item;
	}
	
	private JMenuItem setBoundsAlphaItem( final String text, final double min, final double max )
	{
		final JMenuItem item = new JMenuItem( text );
		item.addActionListener( e -> setBoundsAlpha( new Bounds( min, max ) ) );
		return item;
	}

	private JMenuItem runnableAlphaItem( final String text, final Runnable action )
	{
		final JMenuItem item = new JMenuItem( text );
		item.addActionListener( e -> action.run() );
		return item;
	}
	
	private JMenuItem setBoundsGammaAlphaItem( final String text, final double min, final double max )
	{
		final JMenuItem item = new JMenuItem( text );
		item.addActionListener( e -> setBoundsGammaAlpha( new Bounds( min, max ) ) );
		return item;
	}

	private JMenuItem runnableGammaAlphaItem( final String text, final Runnable action )
	{
		final JMenuItem item = new JMenuItem( text );
		item.addActionListener( e -> action.run() );
		return item;
	}
	
	private boolean blockUpdates = false;

	private List< ConverterSetup > converterSetups;

	private synchronized void setBounds( final Bounds bounds )
	{
		if ( converterSetups == null || converterSetups.isEmpty() )
			return;

		for ( final ConverterSetup converterSetup : converterSetups )
		{
			converterSetupBounds.setBounds( converterSetup, bounds );
			if( converterSetupSyncState.getSyncState( converterSetup ) )
			{
				converterSetupBoundsAlpha.setBounds( converterSetup, bounds );
			}
		}
		updateRangesPanels();	
	}
	
	private synchronized void setBoundsGamma( final Bounds bounds )
	{
		if ( converterSetups == null || converterSetups.isEmpty() )
			return;
		
		for ( final ConverterSetup converterSetup : converterSetups )
		{
			converterSetupBoundsGamma.setBounds( converterSetup, bounds );
			if( converterSetupSyncState.getSyncState(converterSetup) )
			{
				converterSetupBoundsGammaAlpha.setBounds( converterSetup, bounds );
			}
		}
		updateGammasPanels();
	}
	
	private synchronized void setBoundsAlpha( final Bounds bounds )
	{
		if ( converterSetups == null || converterSetups.isEmpty() )
			return;

		for ( final ConverterSetup converterSetup : converterSetups )
		{
			converterSetupBoundsAlpha.setBounds( converterSetup, bounds );
		}
		updateRangesPanels();
	}
	
	private synchronized void setBoundsGammaAlpha( final Bounds bounds )
	{
		if ( converterSetups == null || converterSetups.isEmpty() )
			return;
		
		for ( final ConverterSetup converterSetup : converterSetups )
		{
			converterSetupBoundsGammaAlpha.setBounds( converterSetup, bounds );
		}		
		updateGammasPanels();
	}
	
	private synchronized void updateConverterSetupRanges()
	{
		if ( blockUpdates || converterSetups == null || converterSetups.isEmpty() )
			return;

		final BoundedRange range = rangePanel.getRange();
		//boolean bSync = cbSync.isSelected();

		for ( final ConverterSetup converterSetup : converterSetups )
		{
			converterSetup.setDisplayRange( range.getMin(), range.getMax() );
			converterSetupBounds.setBounds( converterSetup, range.getBounds() );
			if( converterSetupSyncState.getSyncState(converterSetup) )
			{
				((GammaConverterSetup)converterSetup).setAlphaRange( range.getMin(), range.getMax() );
				converterSetupBoundsAlpha.setBounds( converterSetup, range.getBounds() );
			}
		}
		updateRangesPanels();
	}
	
	private synchronized void updateConverterSetupGamma()
	{
		if ( blockUpdates || converterSetups == null || converterSetups.isEmpty() )
			return;

		final BoundedValueDouble model = gammaPanel.getValue();

		for ( final ConverterSetup converterSetup : converterSetups )
		{
			if(converterSetup instanceof GammaConverterSetup)
			{		
				final GammaConverterSetup gcs = (GammaConverterSetup)converterSetup; 
				gcs.setDisplayGamma(model.getCurrentValue());
				converterSetupBoundsGamma.setBounds(converterSetup, new Bounds(model.getRangeMin(),model.getRangeMax()));
				if( converterSetupSyncState.getSyncState(converterSetup) )
				{
					gcs.setAlphaGamma(model.getCurrentValue());
					converterSetupBoundsGammaAlpha.setBounds(converterSetup, new Bounds(model.getRangeMin(),model.getRangeMax()));
				}
			}
		}
		updateGammasPanels();
	}
	
	private synchronized void updateConverterSetupRangesAlpha()
	{
		if ( blockUpdates || converterSetups == null || converterSetups.isEmpty() )
			return;

		final BoundedRange range = rangeAlphaPanel.getRange();

		for ( final ConverterSetup converterSetup : converterSetups )
		{
			((GammaConverterSetup)converterSetup).setAlphaRange( range.getMin(), range.getMax() );
			converterSetupBoundsAlpha.setBounds( converterSetup, range.getBounds() );
		}
		updateRangesPanels();
	}
	
	private synchronized void updateConverterSetupGammaAlpha()
	{
		if ( blockUpdates || converterSetups == null || converterSetups.isEmpty() )
			return;

		final BoundedValueDouble model = gammaAlphaPanel.getValue();

		for ( final ConverterSetup converterSetup : converterSetups )
		{
			if(converterSetup instanceof GammaConverterSetup)
			{		
				((GammaConverterSetup)converterSetup).setAlphaGamma(model.getCurrentValue());
				converterSetupBoundsGammaAlpha.setBounds(converterSetup, new Bounds(model.getRangeMin(),model.getRangeMax()));
			}
		}
		updateGammasPanels();
	}
	
	private synchronized void updateConverterSetupSyncState()
	{
		if ( blockUpdates || converterSetups == null || converterSetups.isEmpty() )
			return;
		
		final boolean bSyncState = syncPanel.isSelected();
		GammaConverterSetup gcs = null;
		for ( final ConverterSetup converterSetup : converterSetups )
		{
			converterSetupSyncState.setSyncState( converterSetup, bSyncState );
			if(gcs == null)
				gcs = (GammaConverterSetup)converterSetup;
		}
		if(gcs != null)
		{
			gcs.fireParametersChanged();
		}
//		updateSyncPanel();
	}
	
	private synchronized void updateSyncPanel()
	{
		if ( converterSetups == null || converterSetups.isEmpty() )
		{
			SwingUtilities.invokeLater( () -> {
				syncPanel.setEnabled( false );
				syncPanel.setConsistent( true );
			} );
		}
		else
		{
			boolean allSyncSame = true;
			boolean bFirst = true;
			boolean bSync = true;	
			for ( final ConverterSetup converterSetup : converterSetups )
			{
				if(bFirst)
				{
					bSync = converterSetupSyncState.getSyncState( converterSetup );
					bFirst = false;
				}
				else
				{
					allSyncSame &= (bSync == converterSetupSyncState.getSyncState( converterSetup )); 
				}
			}
			final boolean isConsistent = allSyncSame;
			final boolean syncFin = bSync;
			SwingUtilities.invokeLater( () -> {
				synchronized ( BoundedRangeEditorPG.this )
				{
					blockUpdates = true;
					syncPanel.setEnabled( true );
					syncPanel.setConsistent( isConsistent );
					syncPanel.setSelected( syncFin );
					blockUpdates = false;
				}
			} );
		}
	}
	
	synchronized void updateSelection()
	{
		if(singleCS.size() == 0)
		{
			converterSetups = selectedConverterSetups.get();
		}
		else
		{
			converterSetups = singleCS;
		}
		updateRangesPanels();
		updateGammasPanels();
		updateSyncPanel();
	}

	private synchronized void updateRangesPanels()
	{
		if ( converterSetups == null || converterSetups.isEmpty() )
		{
			SwingUtilities.invokeLater( () -> {
				rangePanel.setEnabled( false );
				rangePanel.setConsistent( true );
				rangeAlphaPanel.setEnabled( false );
				rangeAlphaPanel.setConsistent( true );
			} );
		}
		else
		{
			BoundedRange range = null;
			boolean allRangesEqual = true;
			BoundedRange rangeAlpha = null;
			boolean allAlphaRangesEqual = true;
			for ( final ConverterSetup converterSetup : converterSetups )
			{
				final Bounds bounds = converterSetupBounds.getBounds( converterSetup );
				final double minBound = bounds.getMinBound();
				final double maxBound = bounds.getMaxBound();
				final double min = converterSetup.getDisplayRangeMin();
				final double max = converterSetup.getDisplayRangeMax();

				final BoundedRange converterSetupRange = new BoundedRange( minBound, maxBound, min, max );
				if ( range == null )
					range = converterSetupRange;
				else
				{
					allRangesEqual &= range.equals( converterSetupRange );
					range = range.join( converterSetupRange );
				}
				
				final Bounds boundsAlpha = converterSetupBoundsAlpha.getBounds( converterSetup );
				final double minBoundAlpha = boundsAlpha.getMinBound();
				final double maxBoundAlpha = boundsAlpha.getMaxBound();
				final double minA = ((GammaConverterSetup)converterSetup).getAlphaRangeMin();
				final double maxA = ((GammaConverterSetup)converterSetup).getAlphaRangeMax();

				final BoundedRange converterSetupRangeAlpha = new BoundedRange( minBoundAlpha, maxBoundAlpha, minA, maxA );
				if ( rangeAlpha == null )
					rangeAlpha = converterSetupRangeAlpha;
				else
				{
					allAlphaRangesEqual &= rangeAlpha.equals( converterSetupRangeAlpha );
					rangeAlpha = rangeAlpha.join( converterSetupRangeAlpha );
				}

			}
			final BoundedRange finalRange = range;
			final boolean isConsistent = allRangesEqual;
			final BoundedRange finalRangeAlpha = rangeAlpha;
			final boolean isConsistentAlpha = allAlphaRangesEqual;
			SwingUtilities.invokeLater( () -> {
				synchronized ( BoundedRangeEditorPG.this )
				{
					blockUpdates = true;
					rangePanel.setEnabled( true );
					rangePanel.setRange( finalRange );
					rangePanel.setConsistent( isConsistent );
					rangeAlphaPanel.setEnabled( true );
					rangeAlphaPanel.setRange( finalRangeAlpha );
					rangeAlphaPanel.setConsistent( isConsistentAlpha );
					blockUpdates = false;
				}
			} );
		}
	}
	
	private synchronized void updateGammasPanels()
	{
		if ( converterSetups == null || converterSetups.isEmpty() )
		{
			SwingUtilities.invokeLater( () -> {
				gammaPanel.setEnabled(false);
				gammaPanel.setConsistent( true );
				gammaAlphaPanel.setEnabled( false );
				gammaAlphaPanel.setConsistent( true );
			} );
		}
		else
		{
			BoundedValueDouble model = null;
			boolean allValuesEqual = true;
			double aver_gamma = 0.0;
			int nCScount = 0;
			
			BoundedValueDouble modelAlpha = null;
			boolean allValuesEqualAlpha = true;
			double aver_gamma_alpha = 0.0;
			int nCScountAlpha = 0;
			
			for ( final ConverterSetup converterSetup : converterSetups )
			{
				if(converterSetup instanceof GammaConverterSetup)
				{
					GammaConverterSetup gcs = (GammaConverterSetup)converterSetup;
					final Bounds bounds = converterSetupBoundsGamma.getBounds( converterSetup );
					final double gamma = gcs.getDisplayGamma();
					aver_gamma += gamma;
					nCScount++;
					final BoundedValueDouble converterSetupGammaModel = new BoundedValueDouble( bounds.getMinBound(),bounds.getMaxBound(), gamma);
					if(model == null)
					{
						model = converterSetupGammaModel;
					}
					else
					{
						allValuesEqual &= (Math.abs((aver_gamma/nCScount)-gamma)<0.00001);
						model = new BoundedValueDouble(Math.min(model.getRangeMin(),converterSetupGammaModel.getRangeMin()),
								Math.max(model.getRangeMax(),converterSetupGammaModel.getRangeMax()),
								aver_gamma/nCScount);
						//range = range.join( converterSetupRange );
					}
					
					final Bounds boundsAlpha = converterSetupBoundsGammaAlpha.getBounds( converterSetup );
					final double gammaAlpha = gcs.getAlphaGamma();
					aver_gamma_alpha += gammaAlpha;
					nCScountAlpha++;
					final BoundedValueDouble converterSetupGammaModelAlpha = new BoundedValueDouble( boundsAlpha.getMinBound(),boundsAlpha.getMaxBound(), gammaAlpha);
					if(modelAlpha == null)
					{
						modelAlpha = converterSetupGammaModelAlpha;
					}
					else
					{
						allValuesEqualAlpha &= (Math.abs((aver_gamma_alpha/nCScountAlpha)-gammaAlpha)<0.00001);
						modelAlpha = new BoundedValueDouble(Math.min(modelAlpha.getRangeMin(),converterSetupGammaModelAlpha.getRangeMin()),
								Math.max(modelAlpha.getRangeMax(),converterSetupGammaModelAlpha.getRangeMax()),
								aver_gamma_alpha/nCScountAlpha);
					}
				}
			}
			if(model == null)
			{
				//no gamma converters
			}
			else
			{
				final BoundedValueDouble finalModel = model;				
				final boolean isConsistent = allValuesEqual;
				
				final BoundedValueDouble finalModelAlpha = modelAlpha;				
				final boolean isConsistentAlpha = allValuesEqualAlpha;
				
				SwingUtilities.invokeLater( () -> {
					synchronized ( BoundedRangeEditorPG.this )
					{
						blockUpdates = true;
						gammaPanel.setEnabled( true );
						gammaPanel.setValue( finalModel );
						gammaPanel.setConsistent( isConsistent );
						
						gammaAlphaPanel.setEnabled( true );
						gammaAlphaPanel.setValue( finalModelAlpha );
						gammaAlphaPanel.setConsistent( isConsistentAlpha );
						blockUpdates = false;
					}
				} );
			}
		}
	}
}
