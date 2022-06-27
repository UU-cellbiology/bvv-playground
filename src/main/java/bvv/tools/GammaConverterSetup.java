package bvv.tools;

import bdv.tools.brightness.ConverterSetup;

public interface GammaConverterSetup extends ConverterSetup {

	void setDisplayGamma( double gamma );
	double getDisplayGamma();
	void setLUT(float [][] lut_in);
	float [][] getLUT();
}
