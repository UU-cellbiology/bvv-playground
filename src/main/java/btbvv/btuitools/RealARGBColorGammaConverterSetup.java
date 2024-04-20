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
package btbvv.btuitools;

import java.util.Arrays;
import java.util.List;

import org.scijava.listeners.Listeners;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import net.imglib2.FinalRealInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealInterval;
import net.imglib2.display.ColorConverter;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;

public class RealARGBColorGammaConverterSetup implements GammaConverterSetup {
	private final int id;

	private final List< ColorConverter > converters;

	private final Listeners.List< SetupChangeListener > listeners;
	
	private float [][] lut;
	
	private BTLutTexture chLUT = new BTLutTexture();
	
	/**0 = maximum intensity projection; 1 = transparency **/
	private int nRenderType =0; 
	
	private boolean useLUT = false;
	
	private boolean clipActive = false;
	
	private FinalRealInterval clipInt = null;
	
	private AffineTransform3D clipTransform = new AffineTransform3D();

	public RealARGBColorGammaConverterSetup( final int setupId, final ColorConverter... converters )
	{
		this( setupId, Arrays.asList( converters ) );
	}

	public RealARGBColorGammaConverterSetup( final int setupId, final List< ColorConverter > converters )
	{
		this.id = setupId;
		this.converters = converters;
		this.listeners = new Listeners.SynchronizedList<>();

	}

	@Override
	public Listeners< SetupChangeListener > setupChangeListeners()
	{
		return listeners;
	}

	@Override
	public void setDisplayRange( final double min, final double max )
	{
		boolean changed = false;
		for ( final ColorConverter converter : converters )
		{
			if ( converter.getMin() != min )
			{
				converter.setMin( min );
				changed = true;
			}
			if ( converter.getMax() != max )
			{
				converter.setMax( max );
				changed = true;
			}
		}
		if ( changed )
			listeners.list.forEach( l -> l.setupParametersChanged( this ) );
	}
	
	@Override
	public void setAlphaRange( final double minAlpha, final double maxAlpha )
	{
		boolean changed = false;
		for ( final ColorConverter converter : converters )
		{
			if(converter instanceof ColorGammaConverter)
			{
			
				final ColorGammaConverter convgamma = (ColorGammaConverter)converter;
				if ( convgamma.getMinAlpha() != minAlpha )
				{
					convgamma.setMinAlpha( minAlpha );
					changed = true;
				}
				if ( convgamma.getMaxAlpha() != maxAlpha )
				{
					convgamma.setMaxAlpha( maxAlpha );
					changed = true;
				}
			}
		}
		if ( changed )
			listeners.list.forEach( l -> l.setupParametersChanged( this ) );
	}
	
	
	
	@Override
	public void setDisplayGamma(double gamma) {
		boolean changed = false;
		for ( final ColorConverter converter : converters )
		{
			if(converter instanceof ColorGammaConverter)
			{
				final ColorGammaConverter convgamma = (ColorGammaConverter)converter;
							
				if ( convgamma.getGamma()!= gamma)
				{
					convgamma.setGamma( gamma );
					changed = true;				
				}
			}
		}
		if ( changed )
			listeners.list.forEach( l -> l.setupParametersChanged( this ) );
		
	}
	
	@Override
	public void setAlphaGamma(double gammaAlpha) {
		boolean changed = false;
		for ( final ColorConverter converter : converters )
		{
			if(converter instanceof ColorGammaConverter)
			{
				final ColorGammaConverter convgamma = (ColorGammaConverter)converter;
							
				if ( convgamma.getGammaAlpha()!= gammaAlpha)
				{
					convgamma.setGammaAlpha( gammaAlpha );
					changed = true;				
				}
			}
		}
		if ( changed )
			listeners.list.forEach( l -> l.setupParametersChanged( this ) );
	}


	@Override
	public void setColor( final ARGBType color )
	{
		if ( !supportsColor() )
			return;

		boolean changed = false;
		for ( final ColorConverter converter : converters )
		{
			if ( converter.getColor().get() != color.get() )
			{
				converter.setColor( color );
				changed = true;
			}
			if(converter instanceof ColorGammaConverter)
			{
				useLUT = false;
			}
		}
		if ( changed )
			listeners.list.forEach( l -> l.setupParametersChanged( this ) );
	}
	

	@Override
	public boolean supportsColor()
	{
		return converters.get( 0 ).supportsColor();
	}

	@Override
	public int getSetupId()
	{
		return id;
	}

	@Override
	public double getDisplayRangeMin()
	{
		return converters.get( 0 ).getMin();
	}

	@Override
	public double getDisplayRangeMax()
	{
		return converters.get( 0 ).getMax();
	}
	
	@Override
	public double getAlphaRangeMin()
	{
		if(converters.get(0) instanceof ColorGammaConverter)
			return ((ColorGammaConverter)converters.get( 0 )).getMinAlpha();
		else
			return 0.0;
	}

	@Override
	public double getAlphaRangeMax()
	{
		if(converters.get(0) instanceof ColorGammaConverter)
			return ((ColorGammaConverter)converters.get( 0 )).getMaxAlpha();
		else
			return 1.0;
		
	}
	
	@Override
	public double getDisplayGamma() {
		if(converters.get(0) instanceof ColorGammaConverter)
		{
			final ColorGammaConverter convgamma = (ColorGammaConverter)converters.get(0);
			return convgamma.getGamma();
		}
		else
		{
			return 1.0;
		}
		
	}
	
	@Override
	public double getAlphaGamma() {
		if(converters.get(0) instanceof ColorGammaConverter)
		{
			final ColorGammaConverter convgamma = (ColorGammaConverter)converters.get(0);
			return convgamma.getGammaAlpha();
		}
		else
		{
			return 1.0;
		}
	}

	@Override
	public ARGBType getColor()
	{
		return converters.get( 0 ).getColor();
	}

	@Override
	public void setLUT(float[][] lut_in) {
		
		lut =new float[lut_in.length][];
		for(int i=0;i<lut_in.length;i++)
			lut[i]=lut_in[i].clone();
		useLUT = true;
		
		listeners.list.forEach( l -> l.setupParametersChanged( this ) );
	}
	
	@Override
	public void setchLUT(final RandomAccessibleInterval< ARGBType > rai) 
	{
		
		//lut =new float[lut_in.length][];
		//for(int i=0;i<lut_in.length;i++)
			//lut[i]=lut_in[i].clone();
		//chLUT = new BTLutTexture();
		chLUT.init(rai);
		useLUT = true;
		
		listeners.list.forEach( l -> l.setupParametersChanged( this ) );
	}
	
	@Override
	public BTLutTexture getchLUT() {
		
			return chLUT;
	}

	@Override
	public float[][] getLUT() {
		
		if(!useLUT)
			return null;
		else
			return lut;
	}


	@Override
	public void setRenderType(int nRender) {
		if (nRender >=1)
			nRenderType = 1;
		else
			nRenderType = 0;
		listeners.list.forEach( l -> l.setupParametersChanged( this ) );
	}

	@Override
	public int getRenderType() {
		return nRenderType;
	}

	@Override
	public boolean useLut() {

		return useLUT;
	}

	@Override
	public boolean clipActive() {
		
		return clipActive;
	}

	@Override
	public void setClipInterval(RealInterval clipInt) {
		this.clipInt = new FinalRealInterval(clipInt);
		clipActive = true;
		listeners.list.forEach( l -> l.setupParametersChanged( this ) );
	}

	@Override
	public FinalRealInterval getClipInterval() {
		if(clipActive)
			return clipInt;
		else
			return null;
	}

	@Override
	public AffineTransform3D getClipTransform() {
		
		return clipTransform;
	}

	@Override
	public void setClipTransform(AffineTransform3D t) {
		
		clipTransform = t.copy();
		
	}




}
