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
package bvvpg.vistools;

import java.awt.image.IndexColorModel;

import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;

public abstract class BvvSource implements Bvv
{
	private BvvHandle bvv;

	// so that we can fix the bdv numTimepoints when removing sources.
	private final int numTimepoints;

	protected BvvSource( final BvvHandle bvv, final int numTimepoints )
	{
		this.bvv = bvv;
		this.numTimepoints = numTimepoints;
	}

	// invalidates this BvvSource completely
	// closes bdv if it was the last source
	public abstract void removeFromBdv();

	public abstract void setDisplayRange( final double min, final double max );

	public abstract void setDisplayRangeBounds( final double min, final double max );

	public abstract void setColor( final ARGBType color );

	public abstract void setCurrent();

	public abstract boolean isCurrent();

	public abstract void setActive( final boolean isActive );
	
	//addition of BVV-playground
	
	public abstract void setDisplayGamma( final double gamma );

	public abstract void setDisplayGammaRangeBounds( final double min, final double max );
	
	public abstract void setAlphaRange(final double minAlpha, final double maxAlpha);
	
	public abstract void setAlphaRangeBounds(final double min, final double max);
	
	public abstract void setAlphaGamma( final double gammaAlpha );
	
	public abstract void setAlphaGammaRangeBounds(final double min, final double max);
	
	/** 0 = maximum intensity projection; 1 = volumetric; 2 = surface**/
	public abstract void setRenderType(final int nRenderType);
	
	/** Light type for volumetric and surface render types
	 * 0 = plain; 1 = shaded; 2 = shiny**/
	public abstract void setLightingType(final int nLightType);
	
	/** 0 = no interpolation; 1 = trilinear **/
	public abstract void setVoxelRenderInterpolation(final int nInterpolationType);
	
	/** applies the LUT specified by IndexColorModel. The name can be arbirary **/
	public abstract void setLUT(final IndexColorModel icm_, String sLUTName);
	
	/** tries to load a LUT with this name from available ImageJ/Fiji LUTs **/
	public abstract void setLUT(String sLUTName);
	
	/** set clip state 0 - no clipping, 1 - clipping inside, 2 - clipping outside **/
	public abstract void setClipState(final int nClipType);
	
	/** provided 3D interval, volumetric data outside of it will be clipped. setClipTransform can be applied **/
	public abstract void setClipInterval(RealInterval clipInt);
	
	/** set transform applied to the clip interval  **/
	public abstract void setClipTransform(AffineTransform3D clipTransform);

	@Override
	public BvvHandle getBvvHandle()
	{
		return bvv;
	}

	protected void setBdvHandle( final BvvHandle bdv )
	{
		this.bvv = bdv;
	}

	protected abstract boolean isPlaceHolderSource();

	protected int getNumTimepoints()
	{
		return numTimepoints;
	}
}
