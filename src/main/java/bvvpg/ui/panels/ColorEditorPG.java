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
package bvvpg.ui.panels;

import java.awt.image.IndexColorModel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import bdv.tools.brightness.ConverterSetup;
import bdv.ui.sourcegrouptree.SourceGroupTree;
import bdv.viewer.ConverterSetups;
import bvvpg.pgcards.sourcetable.SourceTablePG;
import bvvpg.source.converters.GammaConverterSetup;

import net.imglib2.type.numeric.ARGBType;

public class ColorEditorPG {
	private final Supplier< List< ConverterSetup > > selectedConverterSetups;
	private List<ConverterSetup> singleCS;

	private final ColorPanelPG colorPanel;

	public ColorEditorPG(
			final SourceTablePG table,
			final ConverterSetups converterSetups,
			final ColorPanelPG colorPanel )
	{
		this( table::getSelectedConverterSetups, converterSetups, colorPanel );
		table.getSelectionModel().addListSelectionListener( e -> updateSelection() );
	}
	public ColorEditorPG(
			final ConverterSetup singleCS,
			final ConverterSetups converterSetups,
			final ColorPanelPG colorPanel )
	{
		this( () -> null, converterSetups, colorPanel );

		if(singleCS!=null)
		{
			singleCS.setupChangeListeners().add( converterSetup ->
			{
				updateSelection();
			});
			this.singleCS.add( singleCS );
		}
	}
	public ColorEditorPG(
			final SourceGroupTree tree,
			final ConverterSetups converterSetups,
			final ColorPanelPG colorPanel )
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

	private ColorEditorPG(
			final Supplier< List< ConverterSetup > > selectedConverterSetups,
			final ConverterSetups converterSetups,
			final ColorPanelPG colorPanel )
	{
		this.singleCS = new ArrayList<>();
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
		IndexColorModel icm = colorPanel.getICM();
		for ( final ConverterSetup converterSetup : converterSetups )
		{
			if(icm != null)
			{
				if (converterSetup instanceof GammaConverterSetup)
				{
					final GammaConverterSetup gconverter = ((GammaConverterSetup)converterSetup);
					gconverter.setLUT( icm, colorPanel.getICMName());
				}
			}
			else
			{
				if ( converterSetup.supportsColor() && color!=null)
				{
					converterSetup.setColor( color );
				}
			}
		}

		updateColorPanel();
	}

	synchronized void updateSelection()
	{
		if(singleCS.size()==0)
		{
			converterSetups = selectedConverterSetups.get();
		}
		else
		{
			converterSetups  = singleCS;
		}
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
			boolean allLUTsEqual = true;
			String sLUTName = null;
			IndexColorModel icm_curr = null;
			for ( final ConverterSetup converterSetup : converterSetups )
			{
				if ( converterSetup.supportsColor() )
				{
					if ( color == null )
						color = converterSetup.getColor();
					else
						allColorsEqual &= color.equals( converterSetup.getColor() );
				}
				if (converterSetup instanceof GammaConverterSetup)
				{
					final GammaConverterSetup gconverter = ((GammaConverterSetup)converterSetup);
					if(gconverter.getLUTSize() > 0)
					{
						allColorsEqual = false;	
					
						if(sLUTName == null)
						{
							sLUTName = gconverter.getLUTName();
							icm_curr = gconverter.getLutICM();
						}
						else
						{
							allLUTsEqual  &=sLUTName.equals( gconverter.getLUTName() );
						}
					}
					else
					{
						allLUTsEqual = false;
					}
				}
			}
			final ARGBType finalColor = color;
			final String sLUTFinal = sLUTName;
			final boolean isConsistent = allColorsEqual||allLUTsEqual;
			final boolean bLUTFinal = allLUTsEqual;
			final IndexColorModel icmFinal = icm_curr;
			SwingUtilities.invokeLater( () -> {
				synchronized ( ColorEditorPG.this )
				{
					blockUpdates = true;
					colorPanel.setEnabled( finalColor != null );
					colorPanel.setColor( finalColor );
					colorPanel.setConsistent( isConsistent );
					if(bLUTFinal)
						colorPanel.setICM( icmFinal, sLUTFinal);
						
					blockUpdates = false;
				}
			} );
		}
	}
}
