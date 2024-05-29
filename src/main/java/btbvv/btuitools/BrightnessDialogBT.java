/*
 * #%L
 * Volume rendering of bdv datasets with gamma and transparency option
 * %%
 * Copyright (C) 2022 - 2024 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
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
package btbvv.btuitools;



import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.IndexColorModel;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;


import bdv.util.InvokeOnEDT;
import ij.IJ;
import ij.plugin.LutLoader;
//import bdv.tools.brightness.ColorIcon;
import bdv.tools.brightness.ConverterSetup;
import bdv.tools.brightness.SliderPanel;
import bdv.tools.brightness.SliderPanelDouble;
import bdv.util.BoundedValueDouble;
import bdv.util.DelayedPackDialog;
import btbvv.core.VolumeViewerFrame;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import net.imglib2.type.numeric.ARGBType;


/**
 * Adjust brightness and colors for individual (or groups of) {@link BasicViewSetup setups}.
 *
 * @author Tobias Pietzsch
 */
@Deprecated
public class BrightnessDialogBT extends DelayedPackDialog
{
	ConverterSetupsBT convSet;
	
	public BrightnessDialogBT( final Frame owner, final SetupAssignmentsBT setupAssignments )
	{
		super( owner, "brightness and color", false );

		convSet = ( ConverterSetupsBT ) ((VolumeViewerFrame)owner).getConverterSetups();
		convSet.getBounds();
		final Container content = getContentPane();

		final MinMaxPanelsBT minMaxPanels = new MinMaxPanelsBT( setupAssignments, this, true );
		final ColorsPanel colorsPanel = new ColorsPanel( setupAssignments );
		content.add( minMaxPanels, BorderLayout.NORTH );
		content.add( colorsPanel, BorderLayout.SOUTH );

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

		setupAssignments.setUpdateListener( new SetupAssignmentsBT.UpdateListener()
		{
			@Override
			public void update()
			{
				try
				{
					if ( isVisible() )
					{
						InvokeOnEDT.invokeAndWait( () -> {
							System.out.println( "colorsPanel.recreateContent()" );
							colorsPanel.recreateContent();
							minMaxPanels.recreateContent();
							recreateContentPending.set( false );
						} );
					}
					else
					{
						recreateContentPending.set( true );
					}
				}
				catch ( InvocationTargetException | InterruptedException e )
				{
					e.printStackTrace();
				}
			}
		} );

		addComponentListener( new ComponentAdapter()
		{
			@Override
			public void componentShown( final ComponentEvent e )
			{
				if ( recreateContentPending.getAndSet( false ) )
				{
					colorsPanel.recreateContent();
					minMaxPanels.recreateContent();
				}
			}
		} );

		pack();
	}

	public static class ColorsPanel extends JPanel
	{
		private final SetupAssignmentsBT setupAssignments;

		private final ArrayList< JButton > buttons;

		private final JColorChooser colorChooser;

		public ColorsPanel( final SetupAssignmentsBT assignments )
		{
			super();
			this.setupAssignments = assignments;
			buttons = new ArrayList<>();
			colorChooser = new JColorChooser();

			setLayout( new BoxLayout( this, BoxLayout.LINE_AXIS ) );
			setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );
			recreateContent();
		}

		public void recreateContent()
		{
			removeAll();
			buttons.clear();

			add( new JLabel( "set view colors:" ) );
			for ( final ConverterSetup setup : setupAssignments.getConverterSetups() )
			{
				final JButton button;
				if (setup instanceof GammaConverterSetup)
				{
					final GammaConverterSetup gconverter = ((GammaConverterSetup)setup);
					if(gconverter.useLut())
					{
						button = new JButton( new ColorIconBT( null, LutLoader.getLut(gconverter.getLUTName()) ) );
					}
					else
					{
						button = new JButton( new ColorIconBT( getColor( setup ), null ) );
					}
				}
				else
				{
					button = new JButton( new ColorIconBT( getColor( setup ), null ) );
				}
			
				button.addActionListener( e -> {
					colorChooser.setColor( getColor( setup ) );
					final JDialog d = JColorChooser.createDialog( button, "Choose a color", true, colorChooser, new ActionListener()
					{
						@Override
						public void actionPerformed( final ActionEvent arg0 )
						{
							final Color c = colorChooser.getColor();
							if (c != null)
							{
								button.setIcon( new ColorIconBT( c, null ) );
								setColor( setup, c );
							}
						}
					}, null );
					d.setVisible( true );
				} );
				button.addMouseListener( new MouseAdapter()
				{ 
					@Override
					public void mouseClicked(MouseEvent evt)
					{
					    
					    if (SwingUtilities.isRightMouseButton(evt)) 
					    {
					      JPopupMenu popup = new JPopupMenu();
					      String [] luts = IJ.getLuts();
					      JMenuItem itemMenu;
					      for(int i = 0; i<luts.length;i++)
					      {
					    	itemMenu =  new  JMenuItem(luts[i]);
					    	itemMenu.addActionListener( new ActionListener()
					    			{

										@Override
										public void actionPerformed( ActionEvent arg0 )
										{
											//System.out.println(((JMenuItem)arg0.getSource()).getText());
											String sLUTName = ((JMenuItem)arg0.getSource()).getText();
											IndexColorModel icm = LutLoader.getLut(sLUTName);
											if (setup instanceof GammaConverterSetup)
											{
												final GammaConverterSetup gconverter = ((GammaConverterSetup)setup);
												gconverter.setLUT( sLUTName );

												button.setIcon( new ColorIconBT( null, icm ) );
											}
										}
					    			
					    			});
					    	popup.add(itemMenu);  
					      }
					      //menuItem.addActionListener(this);
					      popup.show( evt.getComponent(), evt.getX(), evt.getY() );
					    }
					}
				});
				button.setEnabled( setup.supportsColor() );
				buttons.add( button );
				add( button );
			}

			invalidate();
			final Window frame = SwingUtilities.getWindowAncestor( this );
			if ( frame != null )
				frame.pack();
		}
		
		private static Color getColor( final ConverterSetup setup )
		{
			if ( setup.supportsColor() )
			{
				final int value = setup.getColor().get();
				return new Color( value );
			}
			return null;
		}

		private static void setColor( final ConverterSetup setup, final Color color )
		{
			setup.setColor( new ARGBType( color.getRGB() | 0xff000000 ) );
		}

		private static final long serialVersionUID = 6408468837346789676L;
	}

	public static class MinMaxPanelsBT extends JPanel
	{
		private final SetupAssignmentsBT setupAssignments;

		private final ArrayList< MinMaxPanelBT > minMaxPanels;

		private final JDialog dialog;

		private final boolean rememberSizes;

		public MinMaxPanelsBT( final SetupAssignmentsBT assignments, final JDialog dialog, final boolean rememberSizes )
		{
			super();
			this.setupAssignments = assignments;
			this.rememberSizes = rememberSizes;
			minMaxPanels = new ArrayList<>();

			setLayout( new BoxLayout( this, BoxLayout.PAGE_AXIS ) );
			setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );
			recreateContent();

			this.dialog = dialog;
		}

		public void recreateContent()
		{
			final Dimension sliderSize = ( rememberSizes && !minMaxPanels.isEmpty() ) ?
					minMaxPanels.get( 0 ).sliders.getPreferredSize() : null;

			removeAll();
			minMaxPanels.clear();
			
			for ( final MinMaxGroupBT group : setupAssignments.getMinMaxGroups() )
			{
				final MinMaxPanelBT panel = new MinMaxPanelBT(group, setupAssignments, this, rememberSizes );
				if ( sliderSize != null )
					panel.sliders.setPreferredSize( sliderSize );
				minMaxPanels.add( panel );
				add( panel );
			}

			for ( final MinMaxPanelBT panel : minMaxPanels )
			{
				panel.update();
				panel.showAdvanced( isShowingAdvanced );
			}

			invalidate();
			final Window frame = SwingUtilities.getWindowAncestor( this );
			if ( frame != null )
				frame.pack();
		}

		private boolean isShowingAdvanced = false;

		public void showAdvanced( final boolean b )
		{
			isShowingAdvanced = b;
			for( final MinMaxPanelBT panel : minMaxPanels )
			{
				panel.storeSliderSize();
				panel.showAdvanced( isShowingAdvanced );
			}
			if ( dialog != null )
				dialog.pack();
		}

		private static final long serialVersionUID = 6538962298579455010L;
	}

	/**
	 * A panel containing min/max {@link SliderPanel SliderPanels}, view setup check-boxes and advanced settings.
	 */
	public static class MinMaxPanelBT extends JPanel implements MinMaxGroupBT.UpdateListener, BoundedValueDouble.UpdateListener
	{
		private final SetupAssignmentsBT setupAssignments;

		private final MinMaxGroupBT minMaxGroup;
		
		BoundedValueDouble gammaRange;

		private final ArrayList< JCheckBox > boxes;

		private final JPanel sliders;

		private final Runnable showAdvanced;

		private final Runnable hideAdvanced;

		private boolean isShowingAdvanced;

		private final boolean rememberSizes;

		public MinMaxPanelBT( final MinMaxGroupBT group, final SetupAssignmentsBT assignments, final MinMaxPanelsBT minMaxPanels, final boolean rememberSizes )
		{
			super();
			setupAssignments = assignments;
			minMaxGroup = group;
			this.rememberSizes = rememberSizes;
//			setBorder( BorderFactory.createCompoundBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ), BorderFactory.createLineBorder( Color.black ) ) );
			setBorder( BorderFactory.createCompoundBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ), BorderFactory.createEtchedBorder() ) );
			setLayout( new BorderLayout( 10, 10 ) );

			sliders = new JPanel();
			sliders.setLayout( new BoxLayout( sliders, BoxLayout.PAGE_AXIS ) );

			
			final double spinnerStepSize = 1;
				
			final RangeSliderPanelDouble lutPanel = new RangeSliderPanelDouble( "LUT range ", group.getMinBoundedValue(), group.getMaxBoundedValue(), spinnerStepSize );
			sliders.add( lutPanel );
			
	
			final SliderPanelDouble gammaPanel = new SliderPanelDouble( "LUT γ", group.gammaRange, 0.02);
			gammaPanel.setBorder( BorderFactory.createEmptyBorder( 0, 10, 10, 10 ) );
			sliders.add( gammaPanel);
			
			final RangeSliderPanelDouble alphaPanel = new RangeSliderPanelDouble( "α range ", group.alphaRange.getMinBoundedValue(), group.alphaRange.getMaxBoundedValue(), spinnerStepSize );
			sliders.add( alphaPanel );
			
			final SliderPanelDouble gammaAlphaPanel = new SliderPanelDouble( "α γ", group.gammaAlphaRange, 0.02);
			gammaAlphaPanel.setBorder( BorderFactory.createEmptyBorder( 0, 10, 10, 10 ) );
			sliders.add( gammaAlphaPanel);
			
			
			if ( rememberSizes && ! minMaxPanels.minMaxPanels.isEmpty() )
			{
				final Dimension dim = minMaxPanels.minMaxPanels.get( 0 ).sliders.getSize();
				if ( dim.width > 0 )
					sliders.setPreferredSize( dim );
			}

			add( sliders, BorderLayout.CENTER );

			boxes = new ArrayList<>();
			final JPanel boxesPanel = new JPanel();
			boxesPanel.setLayout( new BoxLayout( boxesPanel, BoxLayout.LINE_AXIS ) );

			for ( final ConverterSetup setup : assignments.getConverterSetups() )
			{
				final JCheckBox box = new JCheckBox();
				box.addActionListener( new ActionListener()
				{
					@Override
					public void actionPerformed( final ActionEvent arg0 )
					{
						if ( box.isSelected() )
							assignments.moveSetupToGroup( setup, minMaxGroup );
						else if ( !assignments.removeSetupFromGroup( setup, minMaxGroup ) )
							box.setSelected( true );
					}
				} );
				boxesPanel.add( box );
				boxes.add( box );
			}
			
			minMaxGroup.setUpdateListener( this );

			final JPanel advancedPanel = new JPanel();
			//advancedPanel.setLayout( new BoxLayout( advancedPanel, BoxLayout.PAGE_AXIS ) );
			//advancedPanel.setLayout( new GridLayout( 0,2,0,0 ) );
			advancedPanel.setLayout( new GridBagLayout( ) );

			final JSpinner dummy = new JSpinner();
			dummy.setModel( new SpinnerNumberModel( minMaxGroup.getRangeMax(), minMaxGroup.getFullRangeMin(), minMaxGroup.getFullRangeMax(), 1 ) );
			dummy.setBorder( BorderFactory.createEmptyBorder( 0, 10, 10, 10 ) );
			final Dimension ps = dummy.getPreferredSize();
			ps.setSize( ps.getWidth()*0.5, ps.getHeight() );
			
			final MinSpinner spinnerAlphaRangeMin = new MinSpinner(null, minMaxGroup.alphaRange, ps, null, false);
			spinnerAlphaRangeMin.sLimit =()->minMaxGroup.getAlphaFullRangeMin();
			final MinSpinner spinnerRangeMin = new MinSpinner(null, minMaxGroup, ps, spinnerAlphaRangeMin, true);
			spinnerRangeMin.sLimit =()->minMaxGroup.getFullRangeMin();
			
			final MaxSpinner spinnerAlphaRangeMax = new MaxSpinner(null, minMaxGroup.alphaRange, ps, null, false);
			spinnerAlphaRangeMax.sLimit=()->minMaxGroup.getAlphaFullRangeMax();
			final MaxSpinner spinnerRangeMax = new MaxSpinner(null, minMaxGroup, ps, spinnerAlphaRangeMax, true);
			spinnerRangeMax.sLimit=()->minMaxGroup.getFullRangeMax();

			
			final MinSpinner spinnerAlphaGammaRangeMin = new MinSpinner(minMaxGroup.gammaAlphaRange, null, ps, null, false);
			spinnerAlphaGammaRangeMin.sLimit =()->minMaxGroup.getGammaAlphaFullRangeMin();			
			final MinSpinner spinnerGammaRangeMin = new MinSpinner(minMaxGroup.gammaRange, null, ps, spinnerAlphaGammaRangeMin, true);
			spinnerGammaRangeMin.sLimit =()->minMaxGroup.getGammaFullRangeMin();
			
			
			final MaxSpinner spinnerAlphaGammaRangeMax = new MaxSpinner(minMaxGroup.gammaAlphaRange, null, ps, null, false);
			spinnerAlphaGammaRangeMax.sLimit=()->minMaxGroup.getGammaAlphaFullRangeMax();
			final MaxSpinner spinnerGammaRangeMax = new MaxSpinner(minMaxGroup.gammaRange, null, ps, spinnerAlphaGammaRangeMax, true);
			spinnerGammaRangeMax.sLimit=()->minMaxGroup.getGammaFullRangeMax();
					
			final JButton advancedButton = new JButton( ">>" );
			advancedButton.setBorder( BorderFactory.createEmptyBorder( 0, 10, 0, 10 ) );
			isShowingAdvanced = false;
			advancedButton.addActionListener( new ActionListener()
			{
				@Override
				public void actionPerformed( final ActionEvent e )
				{
					minMaxPanels.showAdvanced( ! isShowingAdvanced );
				}
			} );
			boxesPanel.add( advancedButton );

			showAdvanced = new Runnable()
			{
				@Override
				public void run()
				{
					GridBagConstraints cd = new GridBagConstraints();
					cd.gridx = 0;
					cd.gridy = 0;
					advancedPanel.add( spinnerRangeMin,cd );
					cd.gridx ++;
					advancedPanel.add( spinnerRangeMax,cd );
					cd.gridy ++;
					cd.gridx = 0;
					advancedPanel.add( spinnerGammaRangeMin, cd);
					cd.gridx ++;
					advancedPanel.add( spinnerGammaRangeMax, cd );
					cd.gridy ++;
					cd.gridx = 0;
					advancedPanel.add( spinnerAlphaRangeMin, cd );
					cd.gridx ++;
					advancedPanel.add( spinnerAlphaRangeMax, cd );
					cd.gridy ++;
					cd.gridx = 0;
					advancedPanel.add( spinnerAlphaGammaRangeMin, cd );
					cd.gridx ++;
					advancedPanel.add( spinnerAlphaGammaRangeMax, cd );					

					advancedButton.setText( "<<" );
					isShowingAdvanced = true;
				}
			};

			hideAdvanced = new Runnable()
			{
				@Override
				public void run()
				{	
					advancedPanel.remove( spinnerRangeMin );
					advancedPanel.remove( spinnerRangeMax );
					advancedPanel.remove( spinnerGammaRangeMin );
					advancedPanel.remove( spinnerGammaRangeMax );
					advancedPanel.remove( spinnerAlphaRangeMin );
					advancedPanel.remove( spinnerAlphaRangeMax );
					advancedPanel.remove( spinnerAlphaGammaRangeMin );
					advancedPanel.remove( spinnerAlphaGammaRangeMax );		
					advancedButton.setText( ">>" );
					isShowingAdvanced = false;
				}
			};

			addComponentListener( new ComponentAdapter()
			{
				@Override
				public void componentResized( final ComponentEvent e )
				{
					storeSliderSize();
				}
			} );
			
			lutPanel.addRangeListener( () -> spinnerRangeMin.setValue( minMaxGroup.getRangeMin() ) );
			lutPanel.addRangeListener( () -> spinnerRangeMax.setValue( minMaxGroup.getRangeMax() ) );
			gammaPanel.setRangeListener(()-> spinnerGammaRangeMin.setValue(minMaxGroup.gammaRange.getRangeMin()));
			gammaPanel.setRangeListener(()-> spinnerGammaRangeMax.setValue(minMaxGroup.gammaRange.getRangeMax()));
			alphaPanel.addRangeListener( () -> spinnerAlphaRangeMin.setValue( minMaxGroup.alphaRange.getRangeMin() ) );
			alphaPanel.addRangeListener( () -> spinnerAlphaRangeMax.setValue( minMaxGroup.alphaRange.getRangeMax() ) );
			gammaAlphaPanel.setRangeListener(()-> spinnerAlphaGammaRangeMin.setValue(minMaxGroup.gammaAlphaRange.getRangeMin()));
			gammaAlphaPanel.setRangeListener(()-> spinnerAlphaGammaRangeMax.setValue(minMaxGroup.gammaAlphaRange.getRangeMax()));


			final JPanel westPanel = new JPanel();
			westPanel.setLayout( new BoxLayout( westPanel, BoxLayout.LINE_AXIS ) );
			final JCheckBox syncBox = new JCheckBox();
			syncBox.setSelected(group.bSync);
			syncBox.addActionListener(new ActionListener() {

	            @Override
	            public void actionPerformed(ActionEvent e) 
	            {	
	            	final boolean bSelected = syncBox.isSelected();
	            	group.bSync = bSelected;
	            	spinnerRangeMin.bSync = bSelected;
	            	spinnerRangeMax.bSync  = bSelected;
	            	spinnerGammaRangeMin.bSync = bSelected;
	            	spinnerGammaRangeMax.bSync = bSelected;
	            	
	            }
	        });
			
			westPanel.add(syncBox,BorderLayout.CENTER);
			add( westPanel, BorderLayout.WEST );
			final JPanel eastPanel = new JPanel();
			eastPanel.setLayout( new BoxLayout( eastPanel, BoxLayout.LINE_AXIS ) );
			eastPanel.add( boxesPanel, BorderLayout.CENTER );
			eastPanel.add( advancedPanel, BorderLayout.EAST );
			add( eastPanel, BorderLayout.EAST );
		}

		public void showAdvanced( final boolean b )
		{
			if ( b )
				showAdvanced.run();
			else
				hideAdvanced.run();
		}

		public void storeSliderSize()
		{
			if ( rememberSizes )
			{
				final Dimension dim = sliders.getSize();
				if ( dim.width > 0 )
					sliders.setPreferredSize( dim );
			}
		}

		@Override
		public void update()
		{
			for ( int i = 0; i < boxes.size(); ++i )
			{
				final int setupId = setupAssignments.getConverterSetups().get( i ).getSetupId();
				boolean b = false;
				for ( final ConverterSetup s : minMaxGroup.setups )
					if ( s.getSetupId() == setupId )
					{
						b = true;
						break;
					}
				boxes.get( i ).setSelected( b );
			}
		}
		
		

		private static final long serialVersionUID = -5209143847804383789L;
	}

	private static final long serialVersionUID = 7963632306732311403L;
}
