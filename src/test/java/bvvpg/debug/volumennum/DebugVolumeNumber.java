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
package bvvpg.debug.volumennum;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.img.Img;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import bdv.cache.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import bdv.viewer.ConverterSetups;
import bdv.viewer.SourceAndConverter;
import bvvpg.debug.GenerateVolumes;
import bvvpg.source.converters.GammaConverterSetup;
import bvvpg.vistools.Bvv;
import bvvpg.vistools.BvvFunctions;
import bvvpg.vistools.BvvOptions;

public class DebugVolumeNumber
{
	public static < T extends RealType< T > & NativeType< T >> void main( final String[] args )
	{
		int nMaxVolumesToTry = 6;
		int nVolumeEdge = 10;

        double spreadCoeff = 1.01;
        
		int numThreads = 8;
		int numQueueLevels = 10;
		SharedQueue queue = new SharedQueue( numThreads, numQueueLevels );
		
		//test different image types
		List<Object> types = new ArrayList<>();
		types.add( new UnsignedByteType() );
		types.add( new UnsignedShortType() );
		
		final int [] maxVal = new int[2];
		maxVal[0] = 255;
		maxVal[1] = 65535;
			
		final Bvv bvv = BvvFunctions.show(BvvOptions.options().frameTitle( "Test max number of volumes" ));

		int[] bestGrid = findOptimalGridDimensions(nMaxVolumesToTry);
		int nx = bestGrid[0];
        int ny = bestGrid[1];

        double spacingX = nVolumeEdge * spreadCoeff;
        double spacingY = nVolumeEdge * spreadCoeff;
        double spacingZ = nVolumeEdge * spreadCoeff;

		for (int i = 0; i < nMaxVolumesToTry; i++) {
            int gridX = i % nx;
            int gridY = (i / nx) % ny;
            int gridZ = i / (nx * ny);

            double px = gridX * spacingX;
            double py = gridY * spacingY;
            double pz = gridZ * spacingZ;
            final AffineTransform3D t = new AffineTransform3D();
            t.translate( px, py, pz );
            String sTitle = Integer.toString( gridX ) + " " + Integer.toString( gridY ) + " " + Integer.toString( gridZ );
            
            //make a random type
            int ind = ( int ) Math.round(Math.random());
            @SuppressWarnings( "unchecked" )
			final Img< ? > rai = GenerateVolumes.makeCachedCellImg((T)types.get( ind ), nVolumeEdge, 128, maxVal[ind], false );
                BvvFunctions.show( VolatileViews.wrapAsVolatile(rai, queue), sTitle, 
            		Bvv.options().addTo( bvv ).sourceTransform( t ));       
		}
		//assign random color
		final List< SourceAndConverter< ? > > sacList = bvv.getBvvHandle().getViewerPanel().state().getSources();
		final ConverterSetups convS = bvv.getBvvHandle().getConverterSetups();
		int sN = 0;
		for(final SourceAndConverter< ? > sac : sacList)
		{

			float hue = (float) sN / nMaxVolumesToTry;
		    int rgb = java.awt.Color.HSBtoRGB(hue, 0.8f, 1.0f);
			convS.getConverterSetup( sac ).setColor( new ARGBType(rgb) );
			GammaConverterSetup gammaConverterSetup = (GammaConverterSetup)convS.getConverterSetup( sac );
			gammaConverterSetup.setRenderType( 1 );
		    sN++;
		}
	}
	
	public static int[] findOptimalGridDimensions(int n) {
        int s = (int) Math.ceil(Math.cbrt(n));
        
        int[] bestGrid = new int[]{1, 1, n};
        int minVolumeDiff = Integer.MAX_VALUE;
        int minShapeDiff = Integer.MAX_VALUE;

        // Search dimensions around the cube root boundary
        for (int x = 1; x <= s + 1; x++) {
            for (int y = x; y <= s + 1; y++) {
                int z = (int) Math.ceil((double) n / (x * y));
                if (z < y) continue; // Keep x <= y <= z

                int volume = x * y * z;
                int volDiff = volume - n;
                int shapeDiff = (y - x) + (z - y) + (z - x);

                // Prefer minimum empty space, then most cubic aspect ratio
                if (volDiff < minVolumeDiff || (volDiff == minVolumeDiff && shapeDiff < minShapeDiff)) {
                    minVolumeDiff = volDiff;
                    minShapeDiff = shapeDiff;
                    bestGrid[0] = z;
                    bestGrid[1] = y;
                    bestGrid[2] = x;
                }
            }
        }
        
        return bestGrid;
    }
}
