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

import bdv.tools.brightness.ConverterSetup;
import bvvpg.core.render.LutCSTexturePG;

import java.awt.image.IndexColorModel;

public interface GammaConverterSetup extends ConverterSetup, Clippable3D 
{

	void setDisplayGamma( double gamma );
	
	double getDisplayGamma();
	
	void setAlphaRange( double min, double max );
	
	double getAlphaRangeMin();
	
	double getAlphaRangeMax();
		
	void setAlphaGamma( double gamma );
	
	double getAlphaGamma();
	
	String getLUTName();
	
	void setLUT(final IndexColorModel icm_, String sLUTName);
	
	void setLUT(String sLUTName);
	
	IndexColorModel getLutICM();
	
	boolean updateNeededLUT();
	
	LutCSTexturePG getLUTTexture();
	
	void setLUTTexture(LutCSTexturePG lut_);	
	
	public int getLUTSize();	
	
	/** 0 = maximum intensity projection; 1 = "volumetric" **/
	void setRenderType(int nRender);
	
	/** 0 = maximum intensity projection; 1 = "volumetric" **/
	int getRenderType ();
	
	/** 0 = nearest neighbor (cubes); 1 = tri-linear **/
	void setVoxelRenderInterpolation(int nInterpolation);
	
	/** 0 = nearest neighbor (cubes); 1 = tri-linear **/
	int getVoxelRenderInterpolation();
	
}
