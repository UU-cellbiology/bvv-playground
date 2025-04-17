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
package bvvpg.debug;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bdv.tools.transformation.TransformedSource;
import bdv.util.BoundedValueDouble;
import bdv.viewer.SourceAndConverter;
import bvvpg.core.VolumeViewerPanel;
import bvvpg.ui.panels.BoundedValuePanelPG;
import bvvpg.vistools.BvvFunctions;
import bvvpg.vistools.BvvSource;
import bvvpg.vistools.BvvStackSource;
import mpicbg.spim.data.SpimDataException;

import net.imglib2.realtransform.AffineTransform3D;

public class DebugLLSShearIllustration {
	public static void main( final String[] args )
	{
		
		final String xmlFilename = "/home/eugene/Desktop/projects/BigTrace/LLS_interp/LLS_interpolation.xml";
		SpimDataMinimal spimData = null;
		try {
			spimData = new XmlIoSpimDataMinimal().load( xmlFilename );
		} catch (SpimDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}				
		
		//make a deskew transform
		AffineTransform3D deskew = makeLLS7Transform(Math.PI/6.0);
		
		
		final List< BvvStackSource< ? > > sources = BvvFunctions.show( spimData );
		final BvvSource source = sources.get(0);

		final VolumeViewerPanel panel = source.getBvvHandle().getViewerPanel();

		for ( SourceAndConverter< ? > sourceC : panel.state().getSources() )
		{
			(( TransformedSource< ? > ) sourceC.getSpimSource() ).setFixedTransform(deskew);
		}	
		
		source.setDisplayRange( 0, 400 );
		//source.setVoxelRenderInterpolation( 0 );

		AffineTransform3D viewSideAT = source.getBvvHandle().getViewerPanel().state().getViewerTransform();
		viewSideAT.scale( 0.65 );

		viewSideAT.rotate(1, Math.PI/2 );
		viewSideAT.rotate(2, (-1.0)*Math.PI/2 );
		viewSideAT.translate( new double [] {-0.0,250.0,0.0} );
		//rotate view so we see the stripes
		source.getBvvHandle().getViewerPanel().state().setViewerTransform( viewSideAT );
		source.setDisplayGamma( 2.064 );
		
		JFrame frame = new JFrame("shear angle");
		frame.setAlwaysOnTop( true );
		frame.getContentPane().setPreferredSize( new Dimension(450,40) );
		BoundedValueDouble model = new BoundedValueDouble( 0.1, 90., 30. );
		BoundedValuePanelPG anglePanel = new BoundedValuePanelPG(model);
		anglePanel.setConsistent( true );
		//anglePanel.getValue().setCurrentValue( 0.2 );
		//anglePanel.updateUI();
		//model.setCurrentValue( 2.0/6.0 );
		anglePanel.changeListeners().add(new BoundedValuePanelPG.ChangeListener()
		{

			@Override
			public void boundedValueChanged(  )
			{
				//System.out.print( slSlider.getValue());
				//anglePanel.getValue();
				AffineTransform3D deskew = makeLLS7Transform(anglePanel.getValue().getCurrentValue()*Math.PI/180.0);
				for ( SourceAndConverter< ? > sourceC : panel.state().getSources() )
				{
					(( TransformedSource< ? > ) sourceC.getSpimSource() ).setFixedTransform(deskew);
				}
				panel.requestRepaint();
			}});

		frame.getContentPane().add(anglePanel);
		frame.pack();
		frame.setVisible(true);
		
	}
	
	/** function generates new LLS7 transform (rotation/shear/z-scaling **/
	static AffineTransform3D makeLLS7Transform(double angle)
	{
		AffineTransform3D afDataTransform = new AffineTransform3D();
		AffineTransform3D tShear = new AffineTransform3D();
		AffineTransform3D tRotate = new AffineTransform3D();
			
		//rotate 30 degrees
		//tRotate.rotate(0, (-1.0)*Math.PI/6.0);
		tRotate.rotate(0, (-1.0)*angle);
		tRotate.rotate(1,(-1.0)* Math.PI);
		//shearing transform
		tShear.set(1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0/Math.tan( angle ), 0.0, 0.0, 0.0, 1.0, 0.0);
		//Z-step adjustment transform
		afDataTransform.set(1.0, 0.0, 0.0, 0.0, 
								0.0,1.0, 0.0, 0.0, 
								0.0, 0.0,Math.sin( angle  ), 0.0);
		
		afDataTransform = tShear.concatenate(afDataTransform);
		afDataTransform = tRotate.concatenate(afDataTransform);
		return afDataTransform;
	}
}
