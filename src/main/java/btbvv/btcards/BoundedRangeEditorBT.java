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
import btbvv.btuitools.GammaConverterSetup;

public class BoundedRangeEditorBT {
	private final Supplier< List< ConverterSetup > > selectedConverterSetups;

	private final BoundedRangePanelBT rangePanel;
	final BoundedValuePanelBT gammaPanel;
	
	//private double gammaFullRangeMin = 0.1;	
	//private double gammaFullRangeMax = 5.0;

	private final ConverterSetupBounds converterSetupBounds;

	public BoundedRangeEditorBT(
			final SourceTable table,
			final ConverterSetups converterSetups,
			final BoundedRangePanelBT rangePanel,
			final BoundedValuePanelBT gammaPanel,			
			final ConverterSetupBounds converterSetupBounds )
	{
		this( table::getSelectedConverterSetups, converterSetups, rangePanel, gammaPanel, converterSetupBounds );
		table.getSelectionModel().addListSelectionListener( e -> updateSelection() );
	}

	public BoundedRangeEditorBT(
			final SourceGroupTree tree,
			final ConverterSetups converterSetups,
			final BoundedRangePanelBT rangePanel,
			final BoundedValuePanelBT gammaPanel,
			final ConverterSetupBounds converterSetupBounds )
	{
		this(
				() -> converterSetups.getConverterSetups( tree.getSelectedSources() ),
				converterSetups, rangePanel, gammaPanel, converterSetupBounds );
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
			final ConverterSetups converterSetups,
			final BoundedRangePanelBT rangePanel, 
			final BoundedValuePanelBT gammaPanel, 
			final ConverterSetupBounds converterSetupBounds )
	{
		this.selectedConverterSetups = selectedConverterSetups;
		this.rangePanel = rangePanel;
		this.gammaPanel = gammaPanel;
		this.converterSetupBounds = converterSetupBounds;

		rangePanel.changeListeners().add( this::updateConverterSetupRanges );
		gammaPanel.changeListeners().add( this::updateConverterSetupGamma);

		converterSetups.listeners().add( s -> updateRangePanel() );

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
		//menu.add( runnableItem(  "shrink bounds to selection", gammaPanel::shrinkBoundsToRange ) );
	//	gammaFullRangeMin = 0.01;
		//gammaFullRangeMax = 5;
		gammaPanel.setPopup( () -> menuG );
		
		updateGammaPanel();
		
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
