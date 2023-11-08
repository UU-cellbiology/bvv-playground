package btbvv.btuitools;

import net.imglib2.display.ColorConverter;

public interface ColorGammaConverter extends ColorConverter
{
	//gamma for LUT
	public double getGamma();
	public void setGamma(final double gamma);
	
	public double getMinAlpha();
	public double getMaxAlpha();
	public void setMinAlpha( double minAlpha );
	public void setMaxAlpha( double maxAlpha );
	
	//gamma for alpha channel
	public double getGammaAlpha();
	public void setGammaAlpha(final double gammaAlpha);

}
