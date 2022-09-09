package bvv.tools;

import bdv.tools.brightness.ConverterSetup;

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
	void setRenderType(int nRender);
	int getRenderType ();
}
