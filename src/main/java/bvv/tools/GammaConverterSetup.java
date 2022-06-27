package bvv.tools;

import bdv.tools.brightness.ConverterSetup;

public interface GammaConverterSetup extends ConverterSetup {

	void setDisplayGamma( double gamma );
	double getDisplayGamma();
}
