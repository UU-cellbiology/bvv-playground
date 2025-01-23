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
package bvvpg.pguitools;


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import bdv.util.InvokeOnEDT;
import bdv.viewer.AbstractViewerPanel;
import bdv.viewer.ConverterSetups;
import bdv.viewer.SourceToConverterSetupBimap;
import bdv.viewer.ViewerState;
import bvvpg.core.VolumeViewerFrame;
import bvvpg.pgcards.ConverterSetupEditPanelPG;
import bvvpg.pgcards.sourcetable.SourceTableModelPG.SourceModel;
import bvvpg.pgcards.sourcetable.SourceTableModelPG.StateModel;


public class BrightnessColorDialog extends DelayedPackDialogPG
{
	private final SourceToConverterSetupBimap converters;

	private StateModel model;
    final SourcePanels sourcePanels;
    public static final int NOT_EXPANDED = 94, EXPANDED = 190; 

	public BrightnessColorDialog( final VolumeViewerFrame owner, final AbstractViewerPanel viewer, final ConverterSetups converterSetups)
	{
		super( owner, "brightness and color", false );
		
		final ViewerState state = viewer.state() ;
		this.converters = converterSetups;
		model = new StateModel(state);
		sourcePanels = new SourcePanels( this );
		sourcePanels.recreateContent( model, converterSetups );
		sourcePanels.setPreferredSize(new Dimension( 800,  model.getSources().size()*NOT_EXPANDED));
		
		final Container content = getContentPane();

		content.add( sourcePanels, BorderLayout.NORTH );
		
		final ActionMap am = getRootPane().getActionMap();
		final InputMap im = getRootPane().getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
		final Object hideKey = new Object();
		final Action hideAction = new AbstractAction()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				setVisible( false );
			}

			private static final long serialVersionUID = 3904286091931838921L;
		};
		im.put( KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), hideKey );
		am.put( hideKey, hideAction );

		final AtomicBoolean recreateContentPending = new AtomicBoolean();
		state.changeListeners().add( e ->
		{
			switch ( e )
			{
			case CURRENT_SOURCE_CHANGED:
			case SOURCE_ACTIVITY_CHANGED:
			case NUM_SOURCES_CHANGED:
				SwingUtilities.invokeLater( () -> 
				{
					try
					{
						model = new StateModel( state );
						if ( isVisible() )
						{
							InvokeOnEDT.invokeAndWait( () -> {
								sourcePanels.recreateContent(model, converters);
								recreateContentPending.set( false );
							} );
						}
						else
						{
							recreateContentPending.set( true );
						}
					}
					catch ( InvocationTargetException | InterruptedException e1 )
					{
						e1.printStackTrace();
					}
				
				}
				);
				break;
			default:
				break;
			}
		} );
		
		addComponentListener( new ComponentAdapter()
		{
			@Override
			public void componentShown( final ComponentEvent e )
			{
				if ( recreateContentPending.getAndSet( false ) )
				{
					sourcePanels.recreateContent(model,converters);
				}
			}
		} );
		pack();
	}
	
	public static class SourcePanels extends JPanel implements ActionListener
	{
		private final ArrayList<ConverterSetupEditPanelPG> panels;
		private final JDialog dialog;


		public SourcePanels( final JDialog dialog)
		{
			super();

			panels = new ArrayList<>();
			setLayout(new GridBagLayout());			

			this.dialog = dialog;
		}

		public void recreateContent(StateModel model, final SourceToConverterSetupBimap converters)
		{
			final Dimension panelSize = this.getSize();
			
			int totHeight = 0;

			removeAll();
			panels.clear();
			GridBagConstraints ct = new GridBagConstraints();
			ct.gridx=0;
			ct.gridy=0;
			ct.insets= new Insets(2,2,2,2);
			ct.weightx =0.1;
			ct.fill = GridBagConstraints.HORIZONTAL;
			ct.anchor = GridBagConstraints.NORTH;
			final List< SourceModel > sources = model.getSources();
			for ( final SourceModel src : sources  )
			{
				final ConverterSetupEditPanelPG panel = new ConverterSetupEditPanelPG(
						converters.getConverterSetup( src.getSource()),
						(ConverterSetupsPG)converters );

				panel.setBorder(new CenteredTitle (src.getName()));
				panel.addExpandActionListener( this );
				panels.add( panel );
				add(panel,ct);
				ct.gridy++;
				if(panel.isExpanded())
				{
					totHeight += EXPANDED;
				}
				else
				{
					totHeight += NOT_EXPANDED;
				}

			}	
			for ( final ConverterSetupEditPanelPG panel : panels )
			{
				panel.update();
			}
			invalidate();
			panelSize.height=totHeight;
			this.setPreferredSize( panelSize );
			final Window frame = SwingUtilities.getWindowAncestor( this );
			if ( frame != null )
				frame.pack();
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			final Dimension size = new Dimension();
			size.width = this.getWidth();
			int nHeight = 0;
			for(int i = 0; i< panels.size();i++)
			{
				if(panels.get( i ).isExpanded())
				{
					nHeight += EXPANDED;
					
				}
				else
				{
					nHeight += NOT_EXPANDED;
				}
			}
			size.height = nHeight;
			invalidate();
			this.setPreferredSize( size );
			this.dialog.pack();
			
		}
		
	}


}
