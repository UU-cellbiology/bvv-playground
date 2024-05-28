/*-
 * #%L
 * Volume rendering of bdv datasets with gamma and transparency option
 * %%
 * Copyright (C) 2022 - 2024 Cell Biology, Neurobiology and Biophysics
 * Department of Utrecht University.
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
package btbvv.btcards;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.JCheckBox;
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
import btbvv.btcards.sourcetable.SourceTableBT;
import btbvv.btuitools.ConverterSetupBoundsAlpha;
import btbvv.btuitools.ConverterSetupBoundsGammaAlpha;
import btbvv.btuitools.ConverterSetupBoundsGamma;
import btbvv.btuitools.ConverterSetupsBT;
import btbvv.btuitools.GammaConverterSetup;

public class BoundedRangeEditorBT {
	private final Supplier< List< ConverterSetup > > selectedConverterSetups;


	private final BoundedRangePanelBT rangePanel;
	private final BoundedValuePanelBT gammaPanel;
	private final BoundedRangePanelBT rangeAlphaPanel;
	private final BoundedValuePanelBT gammaAlphaPanel;

	private final ConverterSetupBounds converterSetupBounds;
	private final ConverterSetupBoundsGamma converterSetupBoundsGamma;
	private final ConverterSetupBoundsAlpha converterSetupBoundsAlpha;
	private final ConverterSetupBoundsGammaAlpha converterSetupBoundsGammaAlpha;
	
	private List<ConverterSetup> singleCS;
	private final JCheckBox cbSync;
	
	private boolean bSync;


	
	public BoundedRangeEditorBT(
			final SourceTableBT table,
			final ConverterSetupsBT converterSetups,
			final BoundedRangePanelBT rangePanel,
			final BoundedValuePanelBT gammaPanel,
			final BoundedRangePanelBT rangeAlphaPanel,
			final BoundedValuePanelBT gammaAlphaPanel,
			final JCheckBox cbSync)
	{
		this( table::getSelectedConverterSetups, converterSetups, rangePanel, gammaPanel, rangeAlphaPanel, gammaAlphaPanel, cbSync);
		table.getSelectionModel().addListSelectionListener( e -> updateSelection() );
	}
	public BoundedRangeEditorBT(
			final ConverterSetup singleCS,
			final ConverterSetupsBT converterSetups,
			final BoundedRangePanelBT rangePanel,
			final BoundedValuePanelBT gammaPanel,
			final BoundedRangePanelBT rangeAlphaPanel,
			final BoundedValuePanelBT gammaAlphaPanel,
			final JCheckBox cbSync)
	{
		this(  () -> null, converterSetups, rangePanel, gammaPanel, rangeAlphaPanel, gammaAlphaPanel, cbSync);
		if(singleCS != null)
		{
			singleCS.setupChangeListeners().add( converterSetup ->
			{
				updateSelection();
			});
			this.singleCS.add( singleCS );
		}
	}

	public BoundedRangeEditorBT(
			final SourceGroupTree tree,
			final ConverterSetupsBT converterSetups,
			final BoundedRangePanelBT rangePanel,
			final BoundedValuePanelBT gammaPanel,
			final BoundedRangePanelBT rangeAlphaPanel,
			final BoundedValuePanelBT gammaAlphaPanel,
			final JCheckBox cbSync)
	{
		this(
				() -> converterSetups.getConverterSetups( tree.getSelectedSources() ), 
				converterSetups, rangePanel, gammaPanel, rangeAlphaPanel,gammaAlphaPanel, cbSync);
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

	private BoundedRangeEditorBT(
			final Supplier< List< ConverterSetup > > selectedConverterSetups,
			final ConverterSetupsBT converterSetups,
			final BoundedRangePanelBT rangePanel, 
			final BoundedValuePanelBT gammaPanel, 
			final BoundedRangePanelBT rangeAlphaPanel,
			final BoundedValuePanelBT gammaAlphaPanel,
			final JCheckBox cbSync)
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
		this.cbSync = cbSync;
		this.cbSync.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) 
            {	
            	bSync = cbSync.isSelected();
            	
            }
        });
		
		bSync = this.cbSync.isSelected();

		rangePanel.changeListeners().add( this::updateConverterSetupRanges );
		gammaPanel.changeListeners().add( this::updateConverterSetupGamma);
		rangeAlphaPanel.changeListeners().add( this::updateConverterSetupRangesAlpha );
		gammaAlphaPanel.changeListeners().add( this::updateConverterSetupGammaAlpha);

		converterSetups.listeners().add( s -> updateRangePanel() );
		converterSetups.listeners().add( s -> updateGammaPanel() );
		converterSetups.listeners().add( s -> updateRangeAlphaPanel() );
		converterSetups.listeners().add( s -> updateGammaAlphaPanel() );

		final JPopupMenu menu = new JPopupMenu();
		menu.add( runnableItem(  "set bounds ...", rangePanel::setBoundsDialog ) );
		menu.add( setBoundsItem( "set bounds 0..1", 0, 1 ) );
		menu.add( setBoundsItem( "set bounds 0..255", 0, 255 ) );
		menu.add( setBoundsItem( "set bounds 0..65535", 0, 65535 ) );
		menu.add( runnableItem(  "shrink bounds to selection", rangePanel::shrinkBoundsToRange ) );
		rangePanel.setPopup( () -> menu );
		updateRangePanel();
		
		final JPopupMenu menuG = new JPopupMenu();
		menuG.add( runnableGammaItem(  "set bounds ...", gammaPanel::setBoundsDialog ) );
		menuG.add( setBoundsGammaItem( "set bounds 0.01..5", 0.01, 5 ) );
		menuG.add( setBoundsGammaItem( "set bounds 0.1..50", 0.1, 50 ) );
		gammaPanel.setPopup( () -> menuG );
		updateGammaPanel();
		
		final JPopupMenu menuA = new JPopupMenu();
		menuA.add( runnableAlphaItem(  "set bounds ...", rangeAlphaPanel::setBoundsDialog ) );
		menuA.add( setBoundsAlphaItem( "set bounds 0..1", 0, 1 ) );
		menuA.add( setBoundsAlphaItem( "set bounds 0..255", 0, 255 ) );
		menuA.add( setBoundsAlphaItem( "set bounds 0..65535", 0, 65535 ) );
		menuA.add( runnableAlphaItem(  "shrink bounds to selection", rangeAlphaPanel::shrinkBoundsToRange ) );
		rangeAlphaPanel.setPopup( () -> menuA );
		updateRangeAlphaPanel();
		
		final JPopupMenu menuGA = new JPopupMenu();
		menuGA.add( runnableGammaAlphaItem(  "set bounds ...", gammaAlphaPanel::setBoundsDialog ) );
		menuGA.add( setBoundsGammaAlphaItem( "set bounds 0.01..5", 0.01, 5 ) );
		menuGA.add( setBoundsGammaAlphaItem( "set bounds 0.1..50", 0.1, 50 ) );
		gammaAlphaPanel.setPopup( () -> menuGA );
		updateGammaAlphaPanel();
		
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
		}

		updateRangePanel();
		if(bSync)
		{
			for ( final ConverterSetup converterSetup : converterSetups )
			{
				converterSetupBoundsAlpha.setBounds( converterSetup, bounds );
			}

			updateRangeAlphaPanel();
		}
	}
	private synchronized void setBoundsGamma( final Bounds bounds )
	{
		if ( converterSetups == null || converterSetups.isEmpty() )
			return;
		
		for ( final ConverterSetup converterSetup : converterSetups )
		{
			converterSetupBoundsGamma.setBounds( converterSetup, bounds );
		}
		
		updateGammaPanel();
		if(bSync)
		{
			for ( final ConverterSetup converterSetup : converterSetups )
			{
				converterSetupBoundsGammaAlpha.setBounds( converterSetup, bounds );
			}
			
			updateGammaAlphaPanel();
		}
	}
	private synchronized void setBoundsAlpha( final Bounds bounds )
	{
		if ( converterSetups == null || converterSetups.isEmpty() )
			return;

		for ( final ConverterSetup converterSetup : converterSetups )
		{
			converterSetupBoundsAlpha.setBounds( converterSetup, bounds );
		}

		updateRangeAlphaPanel();
	}
	
	private synchronized void setBoundsGammaAlpha( final Bounds bounds )
	{
		if ( converterSetups == null || converterSetups.isEmpty() )
			return;
		
		for ( final ConverterSetup converterSetup : converterSetups )
		{
			converterSetupBoundsGammaAlpha.setBounds( converterSetup, bounds );
		}
		
		updateGammaAlphaPanel();
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
		}

		updateRangePanel();
		if(bSync)
		{
			for ( final ConverterSetup converterSetup : converterSetups )
			{
				((GammaConverterSetup)converterSetup).setAlphaRange( range.getMin(), range.getMax() );
				converterSetupBoundsAlpha.setBounds( converterSetup, range.getBounds() );
			}

			updateRangeAlphaPanel();
		}
	}
	
	private synchronized void updateConverterSetupGamma()
	{
		if ( blockUpdates || converterSetups == null || converterSetups.isEmpty() )
			return;
		
		//boolean bSync = cbSync.isSelected();

		final BoundedValueDouble model = gammaPanel.getValue();

		for ( final ConverterSetup converterSetup : converterSetups )
		{
			if(converterSetup instanceof GammaConverterSetup)
			{		
				((GammaConverterSetup)converterSetup).setDisplayGamma(model.getCurrentValue());
				converterSetupBoundsGamma.setBounds(converterSetup, new Bounds(model.getRangeMin(),model.getRangeMax()));
			}
		}

		updateGammaPanel();
		if(bSync)
		{
			for ( final ConverterSetup converterSetup : converterSetups )
			{
				if(converterSetup instanceof GammaConverterSetup)
				{		
					((GammaConverterSetup)converterSetup).setAlphaGamma(model.getCurrentValue());
					converterSetupBoundsGammaAlpha.setBounds(converterSetup, new Bounds(model.getRangeMin(),model.getRangeMax()));
				}
			}

			updateGammaAlphaPanel();
		}
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

		updateRangeAlphaPanel();
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

		updateGammaAlphaPanel();
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
		updateRangePanel();
		updateGammaPanel();
		updateRangeAlphaPanel();
		updateGammaAlphaPanel();
	}

	private synchronized void updateRangePanel()
	{
		if ( converterSetups == null || converterSetups.isEmpty() )
		{
			SwingUtilities.invokeLater( () -> {
				rangePanel.setEnabled( false );
				rangePanel.setConsistent( true );
			} );
		}
		else
		{
			BoundedRange range = null;
			boolean allRangesEqual = true;
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
			}
			final BoundedRange finalRange = range;
			final boolean isConsistent = allRangesEqual;
			SwingUtilities.invokeLater( () -> {
				synchronized ( BoundedRangeEditorBT.this )
				{
					blockUpdates = true;
					rangePanel.setEnabled( true );
					rangePanel.setRange( finalRange );
					rangePanel.setConsistent( isConsistent );
					blockUpdates = false;
				}
			} );
		}
	}
	
	private synchronized void updateRangeAlphaPanel()
	{
		if ( converterSetups == null || converterSetups.isEmpty() )
		{
			SwingUtilities.invokeLater( () -> {
				rangeAlphaPanel.setEnabled( false );
				rangeAlphaPanel.setConsistent( true );
			} );
		}
		else
		{
			BoundedRange range = null;
			boolean allRangesEqual = true;
			for ( final ConverterSetup converterSetup : converterSetups )
			{
				final Bounds bounds = converterSetupBoundsAlpha.getBounds( converterSetup );
				final double minBound = bounds.getMinBound();
				final double maxBound = bounds.getMaxBound();
				final double min = ((GammaConverterSetup)converterSetup).getAlphaRangeMin();
				final double max = ((GammaConverterSetup)converterSetup).getAlphaRangeMax();

				final BoundedRange converterSetupRange = new BoundedRange( minBound, maxBound, min, max );
				if ( range == null )
					range = converterSetupRange;
				else
				{
					allRangesEqual &= range.equals( converterSetupRange );
					range = range.join( converterSetupRange );
				}
			}
			final BoundedRange finalRange = range;
			final boolean isConsistent = allRangesEqual;
			SwingUtilities.invokeLater( () -> {
				synchronized ( BoundedRangeEditorBT.this )
				{
					blockUpdates = true;
					rangeAlphaPanel.setEnabled( true );
					rangeAlphaPanel.setRange( finalRange );
					rangeAlphaPanel.setConsistent( isConsistent );
					blockUpdates = false;
				}
			} );
		}
	}
	
	private synchronized void updateGammaPanel()
	{
		if ( converterSetups == null || converterSetups.isEmpty() )
		{
			SwingUtilities.invokeLater( () -> {
				gammaPanel.setEnabled(false);
				gammaPanel.setConsistent( true );
			} );
		}
		else
		{
			BoundedValueDouble model = null;
			boolean allValuesEqual = true;
			double aver_gamma = 0.0;
			int nCScount = 0;
			for ( final ConverterSetup converterSetup : converterSetups )
			{
				if(converterSetup instanceof GammaConverterSetup)
				{
					final Bounds bounds = converterSetupBoundsGamma.getBounds( converterSetup );
					final double gamma = ((GammaConverterSetup)converterSetup).getDisplayGamma();
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
				}
			}
			if(model==null)
			{
				//no gamma converters
			}
			else
			{
				final BoundedValueDouble finalModel = model;				
				final boolean isConsistent = allValuesEqual;
				SwingUtilities.invokeLater( () -> {
					synchronized ( BoundedRangeEditorBT.this )
					{
						blockUpdates = true;
						gammaPanel.setEnabled( true );
						gammaPanel.setValue( finalModel );
						gammaPanel.setConsistent( isConsistent );
						blockUpdates = false;
					}
				} );
			}
		}
	}
	private synchronized void updateGammaAlphaPanel()
	{
		if ( converterSetups == null || converterSetups.isEmpty() )
		{
			SwingUtilities.invokeLater( () -> {
				gammaAlphaPanel.setEnabled(false);
				gammaAlphaPanel.setConsistent( true );
			} );
		}
		else
		{
			BoundedValueDouble model = null;
			boolean allValuesEqual = true;
			double aver_gamma = 0.0;
			int nCScount = 0;
			for ( final ConverterSetup converterSetup : converterSetups )
			{
				if(converterSetup instanceof GammaConverterSetup)
				{
					final Bounds bounds = converterSetupBoundsGammaAlpha.getBounds( converterSetup );
					final double gamma = ((GammaConverterSetup)converterSetup).getAlphaGamma();
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
				}
			}
			if(model==null)
			{
				//no gamma converters
			}
			else
			{
				final BoundedValueDouble finalModel = model;				
				final boolean isConsistent = allValuesEqual;
				SwingUtilities.invokeLater( () -> {
					synchronized ( BoundedRangeEditorBT.this )
					{
						blockUpdates = true;
						gammaAlphaPanel.setEnabled( true );
						gammaAlphaPanel.setValue( finalModel );
						gammaAlphaPanel.setConsistent( isConsistent );
						blockUpdates = false;
					}
				} );
			}
		}
	}
}
