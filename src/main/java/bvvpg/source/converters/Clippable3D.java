package bvvpg.source.converters;

import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;

public interface Clippable3D
{
	/** whether the source's clipping is active **/
	boolean clipActive();

	void setClipActive(boolean bEnabled);

	/** clip interval without application of clip transform **/
	void setClipInterval(final RealInterval clipInt);
		
	/** clip interval without application of clip transform **/
	RealInterval getClipInterval();
	
	void getClipTransform(final AffineTransform3D t);
	
	void setClipTransform(final AffineTransform3D t);
}
