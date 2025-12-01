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
package bvvpg.source.converters;

import java.awt.image.IndexColorModel;
import java.util.Arrays;
import java.util.List;

import org.scijava.listeners.Listeners;

import bvvpg.core.render.LutCSTexturePG;
import ij.plugin.LutLoader;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.display.ColorConverter;

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;

public class RealARGBColorGammaConverterSetup implements GammaConverterSetup 
{
	private final int id;

	private final List< ColorConverter > converters;

	private final Listeners.List< SetupChangeListener > listeners;
	
	private LutCSTexturePG texLUT = new LutCSTexturePG();
	
	/** 0 = maximum intensity projection; 1 = volumetric; 2 = surface **/
	private int nRenderType = 0; 
	
	/** 0 = plain; 1 = shaded; 2 = shiny **/
	private int nLightType = 0; 
	
	/** 0 = nearest neighbor (cubes); 1 = tri-linear **/
	private int nVoxelInterpolation = 1;
	
	private int sizeLUT = 0;
	
	private int clipState = 0;
	
	private FinalRealInterval clipInt = null;
	
	private AffineTransform3D clipTransform = new AffineTransform3D();
	
	private IndexColorModel icm = null;
	
	private String sLUTName = null;
	
	private boolean bUpdateTexture = false;

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
				sizeLUT = 0;
				icm = null;
				bUpdateTexture = true;
				changed = true;
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
		return 0.0;
	}

	@Override
	public double getAlphaRangeMax()
	{
		if(converters.get(0) instanceof ColorGammaConverter)
			return ((ColorGammaConverter)converters.get( 0 )).getMaxAlpha();
		return 1.0;
		
	}
	
	@Override
	public double getDisplayGamma() 
	{
		if(converters.get(0) instanceof ColorGammaConverter)
		{
			final ColorGammaConverter convgamma = (ColorGammaConverter)converters.get(0);
			return convgamma.getGamma();
		}
		return 1.0;
		
	}
	
	@Override
	public double getAlphaGamma() 
	{
		if(converters.get(0) instanceof ColorGammaConverter)
		{
			final ColorGammaConverter convgamma = (ColorGammaConverter)converters.get(0);
			return convgamma.getGammaAlpha();
		}
		return 1.0;
	}

	@Override
	public ARGBType getColor()
	{
		return converters.get( 0 ).getColor();
	}

	
	@Override
	public void setLUT(final IndexColorModel icm_, String sLUTName) 
	{
		
		this.sLUTName = sLUTName;
		icm = icm_;
		sizeLUT = icm.getMapSize();
		bUpdateTexture = true;
		
		listeners.list.forEach( l -> l.setupParametersChanged( this ) );
	}
	
	@Override
	public void setLUT(String sLUTName)
	{
		final IndexColorModel icm_lut = LutLoader.getLut(sLUTName);
		if(icm_lut == null)
		{
			System.err.println("Cannot load ImageJ LUT with the name \""+sLUTName+ "\". Wrong name/not installed?");
			return;
		}
		setLUT(icm_lut, sLUTName);
	}
	
	@Override
	public String getLUTName()
	{
		if(sizeLUT>0)
			return sLUTName;
		
		return null;
	}
	
	@Override
	public LutCSTexturePG getLUTTexture() 
	{		
			return texLUT;
	}

	@Override
	public void setRenderType(int nRender) 
	{
		if (nRender > 3 || nRender < 0)
			nRenderType = 0;
		else
			if(nRenderType != nRender)
			{
				nRenderType = nRender;
				listeners.list.forEach( l -> l.setupParametersChanged( this ) );
			}
	}

	@Override
	public int getRenderType() 
	{
		return nRenderType;
	}
	
	@Override
	public void setLightingType(int nLight) 
	{
		if (nLight > 3 || nLight < 0)
			nLightType = 0;
		else
			if(nLightType != nLight)
			{
				nLightType = nLight;
				listeners.list.forEach( l -> l.setupParametersChanged( this ) );
			}
	}

	@Override
	public int getLightingType() 
	{
		return nLightType;
	}

	@Override
	public int getLUTSize() 
	{
		return sizeLUT;
	}

	@Override
	public int getClipState() {
		
		return clipState;
	}
	
	@Override
	public void setClipState(final int clipType)
	{
		if(clipState != clipType )
		{
			clipState = clipType;
			listeners.list.forEach( l -> l.setupParametersChanged( this ) );
		}
	}
	
	@Override
	public void setClipInterval(final RealInterval clipInt) 
	{
		this.clipInt = new FinalRealInterval(clipInt);
		listeners.list.forEach( l -> l.setupParametersChanged( this ) );
	}

	@Override
	public FinalRealInterval getClipInterval() 
	{
		return clipInt;
	}

	@Override
	public void getClipTransform(final AffineTransform3D t) 
	{	
		t.set( clipTransform );			
		return;
	}

	@Override
	public void setClipTransform(final AffineTransform3D t) 
	{
		clipTransform.set( t );
		listeners.list.forEach( l -> l.setupParametersChanged( this ) );
	}

	@Override
	public IndexColorModel getLutICM()
	{
		return icm;
	}

	@Override
	public boolean updateNeededLUT()
	{
		return bUpdateTexture;
	}

	@Override
	public void setLUTTexture(LutCSTexturePG lut_)
	{
		texLUT = lut_;	
		bUpdateTexture = false;
	}

	@Override
	public void setVoxelRenderInterpolation( int nInterpolation )
	{
		if(nVoxelInterpolation != nInterpolation)
		{
			nVoxelInterpolation = nInterpolation;	
			listeners.list.forEach( l -> l.setupParametersChanged( this ) );
		}
	}

	@Override
	public int getVoxelRenderInterpolation()
	{
		return nVoxelInterpolation;
	}




}
