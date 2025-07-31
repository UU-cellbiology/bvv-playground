package bvvpg.source.converters;

import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;

public interface Clippable3D
{
	/** current clip state 0 - no clipping, 1 - clipping inside, 2 - clipping outside **/
	int getClipState();
	
	/** set clip state 0 - no clipping, 1 - clipping inside, 2 - clipping outside **/
	void setClipState(final int clipType);

	/** clip interval without application of clip transform **/
	void setClipInterval(final RealInterval clipInt);
		
	/** clip interval without application of clip transform **/
	RealInterval getClipInterval();
	
	void getClipTransform(final AffineTransform3D t);
	
	void setClipTransform(final AffineTransform3D t);
}
