package bvv.tools;

import net.imglib2.display.ColorConverter;

public interface ColorGammaConverter extends ColorConverter
{
	public double getGamma();
	public void setGamma(final double gamma);
	

}
