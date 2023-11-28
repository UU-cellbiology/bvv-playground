package btbvv.btuitools;

import java.util.Arrays;
import java.util.List;

import org.scijava.listeners.Listeners;
import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.display.ColorConverter;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;

public class RealARGBColorGammaConverterSetup implements GammaConverterSetup {
	private final int id;

	private final List< ColorConverter > converters;

	private final Listeners.List< SetupChangeListener > listeners;
	
	private float [][] lut;
	
	/**0 = maximum intensity projection; 1 = transparency **/
	private int nRenderType =0; 
	
	private boolean useLUT = false;
	
	private boolean cropActive = false;
	
	private FinalRealInterval cropInt = null;
	
	private AffineTransform3D cropTransform = new AffineTransform3D();

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
				useLUT=false;
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
		useLUT = true;
		
		listeners.list.forEach( l -> l.setupParametersChanged( this ) );
	}

	@Override
	public float[][] getLUT() {
		
		if(!useLUT)
			return null;
		else
			return lut;
	}


	@Override
	public void setRenderType(int nRender) {
		if (nRender >=1)
			nRenderType = 1;
		else
			nRenderType = 0;
		listeners.list.forEach( l -> l.setupParametersChanged( this ) );
	}

	@Override
	public int getRenderType() {
		return nRenderType;
	}

	@Override
	public boolean useLut() {

		return useLUT;
	}

	@Override
	public boolean cropActive() {
		
		return cropActive;
	}

	@Override
	public void setCropInterval(RealInterval cropInt) {
		this.cropInt = new FinalRealInterval(cropInt);
		cropActive = true;
		listeners.list.forEach( l -> l.setupParametersChanged( this ) );
	}

	@Override
	public FinalRealInterval getCropInterval() {
		if(cropActive)
			return cropInt;
		else
			return null;
	}

	@Override
	public AffineTransform3D getCropTransform() {
		
		return cropTransform;
	}

	@Override
	public void setCropTransform(AffineTransform3D t) {
		
		cropTransform = t.copy();
		
	}




}
