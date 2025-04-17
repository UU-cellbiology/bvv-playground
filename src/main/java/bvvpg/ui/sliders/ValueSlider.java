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
package bvvpg.ui.sliders;

import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.plaf.SliderUI;


public class ValueSlider extends JSlider
{
	private static final long serialVersionUID = 1L;
	
	public ValueSlider( final int min, final int max )
	{
		super( min, max );
		initSlider();
	}

	/**
	 * Initializes the slider by setting default properties.
	 */
	private void initSlider()
	{
		setOrientation( HORIZONTAL );
	}
	
	
	/**
	 * Overrides the superclass method to install the UI delegate to draw two
	 * thumbs.
	 */
	@Override
	public void updateUI()
	{
		String currLF = UIManager.getLookAndFeel().getName();
		if(currLF.contains( "FlatLaf" ))
		{
			setUI((SliderUI) UIManager.getUI(this));
		}
		else
		{
			setUI( new ValueSliderUI( this ) );
		}
		// Update UI for slider labels. This must be called after updating the
		// UI of the slider. Refer to JSlider.updateUI().
		updateLabelUIs();
	}
	

}
