package btbvv.btuitools;

import bdv.tools.brightness.ConverterSetup;
import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;

public interface GammaConverterSetup extends ConverterSetup {

	void setDisplayGamma( double gamma );
	double getDisplayGamma();
	
	void setAlphaRange( double min, double max );
	double getAlphaRangeMin();
	double getAlphaRangeMax();
	
	void setAlphaGamma( double gamma );
	double getAlphaGamma();
	
	void setLUT(float [][] lut_in);
	float [][] getLUT();	
	boolean useLut();
	
	boolean clipActive();
	void setClipInterval(RealInterval clipInt);
	FinalRealInterval getClipInterval();
	
	AffineTransform3D getClipTransform();
	void setClipTransform(AffineTransform3D t);
	
	
	void setRenderType(int nRender);
	int getRenderType ();
	
}
