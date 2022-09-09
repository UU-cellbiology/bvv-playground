package bvv.tools;

import java.util.Arrays;
import java.util.List;

import org.scijava.listeners.Listeners;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import net.imglib2.display.ColorConverter;
import net.imglib2.display.LinearRange;
import net.imglib2.type.numeric.ARGBType;

public class RealARGBColorGammaConverterSetup implements GammaConverterSetup {
	private final int id;

	private final List< ColorConverter > converters;

	private final Listeners.List< SetupChangeListener > listeners;
	
	private float [][] lut;
	
	/**0 = maximum intensity projection; 1 = transparency **/
	private int nRenderType =0; 
	
	private boolean iniLUT = false;

	public RealARGBColorGammaConverterSetup( final int setupId, final ColorConverter... converters )
	{
		this( setupId, Arrays.asList( converters ) );
	}

	public RealARGBColorGammaConverterSetup( final int setupId, final List< ColorConverter > converters )
	{
		this.id = setupId;
		this.converters = converters;
		this.listeners = new Listeners.SynchronizedList<>();
	}

	@Override
	public Listeners< SetupChangeListener > setupChangeListeners()
	{
		return listeners;
	}

	@Override
	public void setDisplayRange( final double min, final double max )
	{
		boolean changed = false;
		for ( final ColorConverter converter : converters )
		{
			if ( converter.getMin() != min )
			{
				converter.setMin( min );
				changed = true;
			}
			if ( converter.getMax() != max )
			{
				converter.setMax( max );
				changed = true;
			}
		}
		if ( changed )
			listeners.list.forEach( l -> l.setupParametersChanged( this ) );
	}
	
	@Override
	public void setAlphaRange( final double minAlpha, final double maxAlpha )
	{
		boolean changed = false;
		for ( final ColorConverter converter : converters )
		{
			if(converter instanceof ColorGammaConverter)
			{
			
				final ColorGammaConverter convgamma = (ColorGammaConverter)converter;
				if ( convgamma.getMinAlpha() != minAlpha )
				{
					convgamma.setMinAlpha( minAlpha );
					changed = true;
				}
				if ( convgamma.getMaxAlpha() != maxAlpha )
				{
					convgamma.setMaxAlpha( maxAlpha );
					changed = true;
				}
			}
		}
		if ( changed )
			listeners.list.forEach( l -> l.setupParametersChanged( this ) );
	}
	
	
	
	@Override
	public void setDisplayGamma(double gamma) {
		boolean changed = false;
		for ( final ColorConverter converter : converters )
		{
			if(converter instanceof ColorGammaConverter)
			{
				final ColorGammaConverter convgamma = (ColorGammaConverter)converter;
							
				if ( convgamma.getGamma()!= gamma)
				{
					convgamma.setGamma( gamma );
					changed = true;				
				}
			}
		}
		if ( changed )
			listeners.list.forEach( l -> l.setupParametersChanged( this ) );
		
	}
	
	@Override
	public void setAlphaGamma(double gammaAlpha) {
		boolean changed = false;
		for ( final ColorConverter converter : converters )
		{
			if(converter instanceof ColorGammaConverter)
			{
				final ColorGammaConverter convgamma = (ColorGammaConverter)converter;
							
				if ( convgamma.getGammaAlpha()!= gammaAlpha)
				{
					convgamma.setGammaAlpha( gammaAlpha );
					changed = true;				
				}
			}
		}
		if ( changed )
			listeners.list.forEach( l -> l.setupParametersChanged( this ) );
	}


	@Override
	public void setColor( final ARGBType color )
	{
		if ( !supportsColor() )
			return;

		boolean changed = false;
		for ( final ColorConverter converter : converters )
		{
			if ( converter.getColor().get() != color.get() )
			{
				converter.setColor( color );
				changed = true;
			}
			if(converter instanceof ColorGammaConverter)
			{
				lut =new float[256][3];
				int [] val = new int[3];
				val[0] = ARGBType.red(color.get());
				val[1] = ARGBType.green(color.get());
				val[2] = ARGBType.blue(color.get());
				for(int i=0;i<256;i++)
					for(int j=0;j<3;j++)
					{
						lut [i][j]=Math.round(val[j]*((float)i)/255.0f);
					}
				iniLUT = true;
			}
		}
		if ( changed )
			listeners.list.forEach( l -> l.setupParametersChanged( this ) );
	}

	@Override
	public boolean supportsColor()
	{
		return converters.get( 0 ).supportsColor();
	}

	@Override
	public int getSetupId()
	{
		return id;
	}

	@Override
	public double getDisplayRangeMin()
	{
		return converters.get( 0 ).getMin();
	}

	@Override
	public double getDisplayRangeMax()
	{
		return converters.get( 0 ).getMax();
	}
	
	@Override
	public double getAlphaRangeMin()
	{
		if(converters.get(0) instanceof ColorGammaConverter)
			return ((ColorGammaConverter)converters.get( 0 )).getMinAlpha();
		else
			return 0.0;
	}

	@Override
	public double getAlphaRangeMax()
	{
		if(converters.get(0) instanceof ColorGammaConverter)
			return ((ColorGammaConverter)converters.get( 0 )).getMaxAlpha();
		else
			return 1.0;
		
	}
	
	@Override
	public double getDisplayGamma() {
		if(converters.get(0) instanceof ColorGammaConverter)
		{
			final ColorGammaConverter convgamma = (ColorGammaConverter)converters.get(0);
			return convgamma.getGamma();
		}
		else
		{
			return 1.0;
		}
		
	}
	
	@Override
	public double getAlphaGamma() {
		if(converters.get(0) instanceof ColorGammaConverter)
		{
			final ColorGammaConverter convgamma = (ColorGammaConverter)converters.get(0);
			return convgamma.getGammaAlpha();
		}
		else
		{
			return 1.0;
		}
	}

	@Override
	public ARGBType getColor()
	{
		return converters.get( 0 ).getColor();
	}

	@Override
	public void setLUT(float[][] lut_in) {
		
		lut =new float[lut_in.length][];
		for(int i=0;i<lut_in.length;i++)
			lut[i]=lut_in[i].clone();
		iniLUT = true;
	}

	@Override
	public float[][] getLUT() {
		
		//return grayscale
		if(!iniLUT)
		{
			lut =new float[256][3];
			for(int i=0;i<256;i++)
				for(int j=0;j<3;j++)
				{
					lut [i][j]=i/255.0f;
				}
		}
		return lut;
	}

	/** function gets LUT specified by sZLUTName in settings
	 * and returns 256x3 table map in HSB format */
	static public float [][]  getRGBLutTable(String sLUTName)
	{
		/*int i,j;
	
		int [] onepix; 
		float [][] RGBLutTable = new float[256][3];
		ByteProcessor ish = new ByteProcessor(256,1);
		for ( i=0; i<256; i++)
			for (j=0; j<10; j++)
				ish.putPixel(i, j, i);
		ImagePlus ccc = new ImagePlus("test",ish);
		ccc.show();
		IJ.run(sLUTName);
		IJ.run("RGB Color");
		//ipLUT= (ColorProcessor) ccc.getProcessor();
		ccc.setSlice(1);
		for(i=0;i<256;i++)
		{
			
			onepix= ccc.getPixel(i, 0);
			//rgbtable[i]=ccc.getPixel(i, 1);
			//java.awt.Color.RGBtoHSB(onepix[0], onepix[1], onepix[2], hsbvals);
			RGBLutTable[i][0]=(float)(onepix[0]/255.0f);
			RGBLutTable[i][1]=(float)(onepix[1]/255.0f);
			RGBLutTable[i][2]=(float)(onepix[2]/255.0f);
		}

		ccc.changes=false;
		ccc.close();
		*/
		
		float [][] RGBLutTable = new float[256][3];
		for(int i=0;i<256;i++)
			for(int j=0;j<3;j++)
			{
				RGBLutTable [i][j]=i/255.0f;
			}
		return RGBLutTable;
		//return;
	}

	@Override
	public void setRenderType(int nRender) {
		if (nRender >=1)
			nRenderType = 1;
		else
			nRenderType = 0;
	}

	@Override
	public int getRenderType() {
		return nRenderType;
	}




}
