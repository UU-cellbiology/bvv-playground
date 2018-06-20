package tpietzsch.shadergen;

import net.imglib2.RealInterval;
import org.joml.Vector3fc;

import static tpietzsch.shadergen.MinMax.MIN;

public interface Uniform3fv
{
	void set( float[] value );

	/*
	 * DEFAULT METHODS
	 */

	default void set( float[][] v )
	{
		final int elemSize = 3;
		final float[] data = new float[ elemSize * v.length ];
		int j = 0;
		for ( int i = 0; i < v.length; ++i )
			for ( int d = 0; d < elemSize; ++d )
				data[ j++ ] = v[ i ][ d ];
		set( data );
	}
//	default void set( final Vector3fc... v )
}
