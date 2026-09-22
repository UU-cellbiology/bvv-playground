/*-
 * #%L
 * Volume rendering of bdv datasets with gamma and transparency option
 * %%
 * Copyright (C) 2022 - 2026 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
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

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import bdv.util.volatiles.VolatileViews;
import bvvpg.vistools.Bvv;
import bvvpg.vistools.BvvFunctions;
import bvvpg.vistools.BvvOptions;
import bvvpg.vistools.BvvStackSource;

public class TestHalfPixel
{
	public static void main( final String[] args )
	{
		Bvv bvv = BvvFunctions.show( BvvOptions.options().frameTitle( "Test half pixel" ));
		UnsignedByteType type = new UnsignedByteType();
		final int nFillValue = 255;

		//UnsignedShortType type = new UnsignedShortType();
		//final int nFillValue = 65535;

		int nEdge = 3;
		//make a 5x5 constant intensity blocks
		final RandomAccessibleInterval< ? > rai = GenerateVolumes.makeSimpleRAI(type, nEdge, 0, nFillValue, true );
		final RandomAccessibleInterval< ? > cachedRai = GenerateVolumes.makeCachedCellImg(type, nEdge, 0, nFillValue, true );
		
		BvvStackSource< ? > raiCS = BvvFunctions.show( rai, 
				"simple RAI", 
				BvvOptions.options().addTo( bvv ));
		AffineTransform3D t = new AffineTransform3D();
		t.translate( nEdge, 0.0, 0.0 );
		
		BvvStackSource< ? > cachedRaiCS = BvvFunctions.show( VolatileViews.wrapAsVolatile(cachedRai), 
				"cached RAI", 
				BvvOptions.options().addTo( bvv ).sourceTransform( t ));
		AffineTransform3D viewT = bvv.getBvvHandle().getViewerPanel().state().getViewerTransform();
		viewT.scale( 0.5 );
		viewT.translate( 80., 140.0, 0.0 );
		bvv.getBvvHandle().getViewerPanel().state().setViewerTransform( viewT );
		
		raiCS.setVoxelRenderInterpolation( 1 );
		cachedRaiCS.setVoxelRenderInterpolation( 1 );

	}
}
