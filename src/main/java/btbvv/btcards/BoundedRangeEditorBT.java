package btbvv.btcards;

import java.util.List;
import java.util.function.Supplier;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import bdv.tools.brightness.ConverterSetup;
import bdv.ui.sourcegrouptree.SourceGroupTree;
import bdv.ui.sourcetable.SourceTable;
import bdv.util.BoundedRange;
import bdv.util.BoundedValueDouble;
import bdv.util.Bounds;
import bdv.viewer.ConverterSetupBounds;
import bdv.viewer.ConverterSetups;
import btbvv.btuitools.ConverterSetupBoundsAlphaBT;
import btbvv.btuitools.ConverterSetupsBT;
import btbvv.btuitools.GammaConverterSetup;

public class BoundedRangeEditorBT {
	private final Supplier< List< ConverterSetup > > selectedConverterSetups;

	private final BoundedRangePanelBT rangePanel;
	private final BoundedValuePanelBT gammaPanel;
	private final BoundedRangePanelBT rangeAlphaPanel;

	private final ConverterSetupBounds converterSetupBounds;

	private final ConverterSetupBoundsAlphaBT converterSetupBoundsAlpha;

	public BoundedRangeEditorBT(
			final SourceTable table,
			final ConverterSetupsBT converterSetups,
			final BoundedRangePanelBT rangePanel,
			final BoundedValuePanelBT gammaPanel,
			final BoundedRangePanelBT rangeAlphaPanel,
			final ConverterSetupBounds converterSetupBounds,
			final ConverterSetupBoundsAlphaBT converterSetupBoundsAlpha )
	{
		this( table::getSelectedConverterSetups, converterSetups, rangePanel, gammaPanel, rangeAlphaPanel,
				converterSetupBounds, converterSetupBoundsAlpha );
		table.getSelectionModel().addListSelectionListener( e -> updateSelection() );
	}

	public BoundedRangeEditorBT(
			final SourceGroupTree tree,
			final ConverterSetupsBT converterSetups,
			final BoundedRangePanelBT rangePanel,
			final BoundedValuePanelBT gammaPanel,
			final BoundedRangePanelBT rangeAlphaPanel,
			final ConverterSetupBounds converterSetupBounds,
			final ConverterSetupBoundsAlphaBT converterSetupBoundsAlpha)
	{
		this(
				() -> converterSetups.getConverterSetups( tree.getSelectedSources() ),
				converterSetups, rangePanel, gammaPanel, rangeAlphaPanel, 
				converterSetupBounds, converterSetupBoundsAlpha );
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
			final ConverterSetupBounds converterSetupBounds,
			final ConverterSetupBoundsAlphaBT converterSetupBoundsAlpha)
	{
		this.selectedConverterSetups = selectedConverterSetups;
		this.rangePanel = rangePanel;
		this.gammaPanel = gammaPanel;
		this.rangeAlphaPanel = rangeAlphaPanel;
		this.converterSetupBounds = converterSetupBounds;
		this.converterSetupBoundsAlpha = converterSetupBoundsAlpha;

		rangePanel.changeListeners().add( this::updateConverterSetupRanges );
		gammaPanel.changeListeners().add( this::updateConverterSetupGamma);
		rangeAlphaPanel.changeListeners().add( this::updateConverterSetupRangesAlpha );

		converterSetups.listeners().add( s -> updateRangePanel() );
		converterSetups.listeners().add( s -> updateGammaPanel() );
		converterSetups.listeners().add( s -> updateRangeAlphaPanel() );

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
	}
	private synchronized void setBoundsGamma( final Bounds bounds )
	{
		if ( converterSetups == null || converterSetups.isEmpty() )
			return;

		double gammaFullRangeMin = bounds.getMinBound();
		double gammaFullRangeMax = bounds.getMaxBound();
		gammaPanel.setValue(new BoundedValueDouble(gammaFullRangeMin,gammaFullRangeMax,gammaPanel.getValue().getCurrentValue()));
		for ( final ConverterSetup converterSetup : converterSetups )
		{
			if(converterSetup instanceof GammaConverterSetup)
			{
		
				final double gamma = ((GammaConverterSetup)converterSetup).getDisplayGamma();
				if(gamma<gammaFullRangeMin)
					((GammaConverterSetup)converterSetup).setDisplayGamma(gammaFullRangeMin);
				if(gamma>gammaFullRangeMax)
					((GammaConverterSetup)converterSetup).setDisplayGamma(gammaFullRangeMax);
			}
		}
		
		updateGammaPanel();
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
	private synchronized void updateConverterSetupRanges()
	{
		if ( blockUpdates || converterSetups == null || converterSetups.isEmpty() )
			return;

		final BoundedRange range = rangePanel.getRange();

		for ( final ConverterSetup converterSetup : converterSetups )
		{
			converterSetup.setDisplayRange( range.getMin(), range.getMax() );
			converterSetupBounds.setBounds( converterSetup, range.getBounds() );
		}

		updateRangePanel();
	}
	private synchronized void updateConverterSetupRangesAlpha()
	{
		if ( blockUpdates || converterSetups == null || converterSetups.isEmpty() )
			return;

		final BoundedRange range = rangeAlphaPanel.getRange();

		for ( final ConverterSetup converterSetup : converterSetups )
		{
			//converterSetup.setDisplayRange( range.getMin(), range.getMax() );
			((GammaConverterSetup)converterSetup).setAlphaRange( range.getMin(), range.getMax() );
			converterSetupBoundsAlpha.setBounds( converterSetup, range.getBounds() );
		}

		updateRangeAlphaPanel();
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
				((GammaConverterSetup)converterSetup).setDisplayGamma(model.getCurrentValue());
			}
		}

		updateGammaPanel();
	}

	private synchronized void updateSelection()
	{
		converterSetups = selectedConverterSetups.get();
		updateRangePanel();
		updateGammaPanel();
		updateRangeAlphaPanel();
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
			//ArrayList<Double> gammavals = new ArrayList<Double>(); 
			double aver_gamma = 0.0;
			int nCScount = 0;
			double gammaFullRangeMin = gammaPanel.getValue().getRangeMin();
			double gammaFullRangeMax = gammaPanel.getValue().getRangeMax();
			for ( final ConverterSetup converterSetup : converterSetups )
			{
				if(converterSetup instanceof GammaConverterSetup)
				{
			
					final double gamma = ((GammaConverterSetup)converterSetup).getDisplayGamma();
					aver_gamma += gamma;
					nCScount++;
					if(gamma<gammaFullRangeMin)
					{
						gammaFullRangeMin = gamma*0.5;
					}
					if(gamma>gammaFullRangeMax)
					{
						gammaFullRangeMax = gamma*2.0;
					}
					//final double max = converterSetup.getDisplayRangeMax();
	
					final BoundedValueDouble converterSetupGammaRange = new BoundedValueDouble( gammaFullRangeMin, gammaFullRangeMax, gamma);
					if ( model == null )
						model = converterSetupGammaRange;
					//else
					//{
					//	allRangesEqual &= range.equals( converterSetupRange );
					//	range = range.join( converterSetupRange );
				//	}
				}
			}
			if(model==null)
			{
				//no gamma converters
			}
			else
			{
				aver_gamma /= nCScount;				
				model.setCurrentValue(aver_gamma);
				final BoundedValueDouble finalModel = model;
				
				final boolean isConsistent = true;
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
}
