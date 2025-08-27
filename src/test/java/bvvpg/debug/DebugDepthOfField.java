package bvvpg.debug;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.imglib2.realtransform.AffineTransform3D;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bdv.tools.transformation.TransformedSource;
import bdv.util.BoundedValueDouble;
import bdv.viewer.SourceAndConverter;
import bvvpg.core.VolumeViewerPanel;
import bvvpg.ui.panels.BoundedValuePanelPG;
import bvvpg.vistools.Bvv;
import bvvpg.vistools.BvvFunctions;
import bvvpg.vistools.BvvSource;
import bvvpg.vistools.BvvStackSource;
import mpicbg.spim.data.SpimDataException;

public class DebugDepthOfField
{
	public static void main( final String[] args )
	{
		
		//regular tif init
		/**/

		final String xmlFilename = "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/t1-head.xml";
		SpimDataMinimal spimData = null;
		try {
			spimData = new XmlIoSpimDataMinimal().load( xmlFilename );
		} catch (SpimDataException e) {
			e.printStackTrace();
		}
		List< BvvStackSource< ? > > sources = BvvFunctions.show( spimData );
		
		final BvvSource bvv = sources.get(0);
		bvv.setDisplayRangeBounds( 0, 500 );
		bvv.setDisplayRange(0, 355);
		bvv.setDisplayGamma(0.5);
		bvv.setRenderType( 1 );
		for(int i=0; i<4; i++)
		{
			AffineTransform3D tr = new AffineTransform3D();
			tr.translate( 0,(i+1)*100, 0 );
			sources = BvvFunctions.show( spimData, Bvv.options().addTo( bvv ).sourceTransform( tr ));
			BvvSource source = sources.get(0);
			source.setDisplayRangeBounds( 0, 500 );
			source.setDisplayRange(0, 355);
			source.setDisplayGamma(0.5);
			source.setRenderType( 1 );
		}
		final VolumeViewerPanel panelBVV = bvv.getBvvHandle().getViewerPanel();
		int i=-3;
		for ( SourceAndConverter< ? > sourceC : panelBVV.state().getSources() )
		{
			AffineTransform3D tr = new AffineTransform3D();
			tr.translate( 0.,0., (i+1)*200  );

			(( TransformedSource< ? > ) sourceC.getSpimSource() ).setFixedTransform(tr);
			i++;
		}
		
		JFrame frame = new JFrame("Depth of Field");
		frame.setAlwaysOnTop( true );
		frame.getContentPane().setPreferredSize( new Dimension(450,140) );
		
		final JCheckBox cbEnable = new JCheckBox("Enable DoF");
		cbEnable.addItemListener( new ItemListener() 
		{
			@Override
			public void itemStateChanged( ItemEvent e )
			{
				panelBVV.enableDOF( cbEnable.isSelected());	
			}
		});
		BoundedValueDouble modelDepth = new BoundedValueDouble( 0.0, 1.0, 0.5 );
		BoundedValuePanelPG depthPanel = new BoundedValuePanelPG(modelDepth);
		depthPanel.setConsistent( true );
		
		depthPanel.changeListeners().add(new BoundedValuePanelPG.ChangeListener()
		{

			@Override
			public void boundedValueChanged(  )
			{
				//System.out.print( slSlider.getValue());
				//anglePanel.getValue();
				float fDepth = ( float ) depthPanel.getValue().getCurrentValue();
				panelBVV.setFocalDepth( fDepth );
				//panelBVV.requestRepaint();
			}});
		
		BoundedValueDouble modelRange = new BoundedValueDouble( 0.0, 1.0, 0.5 );
		BoundedValuePanelPG rangePanel = new BoundedValuePanelPG(modelRange);
		rangePanel.setConsistent( true );
		
		rangePanel.changeListeners().add(new BoundedValuePanelPG.ChangeListener()
		{

			@Override
			public void boundedValueChanged(  )
			{
				//System.out.print( slSlider.getValue());
				//anglePanel.getValue();
				float fRange = ( float ) rangePanel.getValue().getCurrentValue();
				panelBVV.setFocalRange( fRange );
				//panelBVV.requestRepaint();
			}});
		BoundedValueDouble modelRadius = new BoundedValueDouble( 1.0, 100.0, 20. );
		BoundedValuePanelPG radiusPanel = new BoundedValuePanelPG(modelRadius);
		radiusPanel.setConsistent( true );
		
		radiusPanel.changeListeners().add(new BoundedValuePanelPG.ChangeListener()
		{

			@Override
			public void boundedValueChanged(  )
			{
				//System.out.print( slSlider.getValue());
				//anglePanel.getValue();
				float fRadius = ( float ) radiusPanel.getValue().getCurrentValue();
				panelBVV.setBlurRadius( fRadius );
				//panelBVV.requestRepaint();
			}});
		

		JPanel allPanels = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		allPanels.setLayout(gridbag);		
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 0.1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		
		allPanels.add( cbEnable,gbc);
		gbc.gridy++;
		allPanels.add( depthPanel,gbc);
		
		gbc.gridy++;
		allPanels.add( rangePanel,gbc);
		gbc.gridy++;
		allPanels.add( radiusPanel,gbc);		
		
		frame.getContentPane().add(allPanels);
		frame.pack();
		frame.setVisible(true);
	}
}
