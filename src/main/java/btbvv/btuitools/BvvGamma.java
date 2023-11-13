package btbvv.btuitools;


import java.util.ArrayList;
import java.util.List;

import bdv.SpimSource;
import bdv.ViewerImgLoader;
import bdv.ViewerSetupImgLoader;
import bdv.VolatileSpimSource;
import bdv.tools.InitializeViewerState;
import bdv.tools.brightness.ConverterSetup;
import bdv.util.Bounds;
import bdv.viewer.ConverterSetups;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.ViewerState;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.sequence.Angle;
import mpicbg.spim.data.sequence.Channel;
import net.imglib2.Volatile;
import net.imglib2.converter.Converter;
import net.imglib2.display.ColorConverter;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.volatiles.VolatileARGBType;

public class BvvGamma {
	/**
	 * Create standard converter from the given {@code type} to ARGB:
	 * <ul>
	 * <li>For {@code RealType}s a {@link RealARGBColorGammaConverter} is
	 * returned.</li>
	 * <li>For {@code ARGBType}s a {@link ScaledARGBGammaConverter.ARGB} is
	 * returned.</li>
	 * <li>For {@code VolatileARGBType}s a
	 * {@link ScaledARGBGammaConverter.VolatileARGB} is returned.</li>
	 * </ul>
	 */
	@SuppressWarnings( "unchecked" )
	public static < T extends NumericType< T > > Converter< T, ARGBType > createConverterToARGB( final T type )
	{
		if ( type instanceof RealType )
		{
			final RealType< ? > t = ( RealType< ? > ) type;
			final double typeMin = Math.max( 0, Math.min( t.getMinValue(), 65535 ) );
			final double typeMax = Math.max( 0, Math.min( t.getMaxValue(), 65535 ) );
			return ( Converter< T, ARGBType > ) RealARGBColorGammaConverter.create( t, typeMin, typeMax, 1.0 );
		}
		else if ( type instanceof ARGBType )
			return ( Converter< T, ARGBType > ) new ScaledARGBGammaConverter.ARGB( 0, 255, 1.0);
		else if ( type instanceof VolatileARGBType )
			return ( Converter< T, ARGBType > ) new ScaledARGBGammaConverter.VolatileARGB( 0, 255, 1.0 );
		else
			throw new IllegalArgumentException( "ImgLoader of type " + type.getClass() + " not supported." );
	}
	/**
	 * Create a {@code ConverterSetup} for the given {@code SourceAndConverter}.
	 * {@link SourceAndConverter#asVolatile() Nested volatile}
	 * {@code SourceAndConverter} are added to the {@code ConverterSetup} if
	 * present. If {@code SourceAndConverter} does not comprise a
	 * {@code ColorConverter}, returns {@code null}.
	 *
	 * @param soc
	 *            {@code SourceAndConverter} for which to create a
	 *            {@code ConverterSetup}
	 * @param setupId
	 *            setupId of the created {@code ConverterSetup}
	 * @return a new {@code ConverterSetup} or {@code null}
	 */
	public static ConverterSetup createConverterSetupBT( final SourceAndConverter< ? > soc, final int setupId )
	{
		final List< ColorConverter > converters = new ArrayList<>();

		final Converter< ?, ARGBType > c = soc.getConverter();
		if ( c instanceof ColorConverter )
			converters.add( ( ColorConverter ) c );

		final SourceAndConverter< ? extends Volatile< ? > > vsoc = soc.asVolatile();
		if ( vsoc != null )
		{
			final Converter< ?, ARGBType > vc = vsoc.getConverter();
			if ( vc instanceof ColorConverter )
				converters.add( ( ColorConverter ) vc );
		}

		if ( converters.isEmpty() )
			return null;
		else
			return new RealARGBColorGammaConverterSetup( setupId, converters );
	}
	
	public static void initSetups(
			final AbstractSpimData< ? > spimData,
			final List< ConverterSetup > converterSetups,
			final List< SourceAndConverter< ? > > sources )
	{
		for ( final BasicViewSetup setup : spimData.getSequenceDescription().getViewSetupsOrdered() )
			initSetupNumericType( spimData, setup, converterSetups, sources );
	}
	
	private static < T extends NumericType< T >, V extends Volatile< T > & NumericType< V > > void initSetupNumericType(
			final AbstractSpimData< ? > spimData,
			final BasicViewSetup setup,
			final List< ConverterSetup > converterSetups,
			final List< SourceAndConverter< ? > > sources )
	{
		final int setupId = setup.getId();
		final ViewerImgLoader imgLoader = ( ViewerImgLoader ) spimData.getSequenceDescription().getImgLoader();
		@SuppressWarnings( "unchecked" )
		final ViewerSetupImgLoader< T, V > setupImgLoader = ( ViewerSetupImgLoader< T, V > ) imgLoader.getSetupImgLoader( setupId );
		final T type = setupImgLoader.getImageType();
		final V volatileType = setupImgLoader.getVolatileImageType();

		if ( ! ( type instanceof NumericType ) )
			throw new IllegalArgumentException( "ImgLoader of type " + type.getClass() + " not supported." );

		final String setupName = createSetupName( setup );

		SourceAndConverter< V > vsoc = null;
		if ( volatileType != null )
		{
			final VolatileSpimSource< V > vs = new VolatileSpimSource<>( spimData, setupId, setupName );
			vsoc = new SourceAndConverter<>( vs, BvvGamma.createConverterToARGB( volatileType ) );
		}

		final SpimSource< T > s = new SpimSource<>( spimData, setupId, setupName );
		final SourceAndConverter< T > soc = new SourceAndConverter<>( s, BvvGamma.createConverterToARGB( type ), vsoc );
		final SourceAndConverter< T > tsoc = bdv.BigDataViewer.wrapWithTransformedSource( soc );
		sources.add( tsoc );

		final ConverterSetup converterSetup = BvvGamma.createConverterSetupBT( tsoc, setupId );
		if ( converterSetup != null )
			converterSetups.add( converterSetup );
	}
	
	private static String createSetupName( final BasicViewSetup setup )
	{
		if ( setup.hasName() )
			return setup.getName();

		String name = "";

		final Angle angle = setup.getAttribute( Angle.class );
		if ( angle != null )
			name += ( name.isEmpty() ? "" : " " ) + "a " + angle.getName();

		final Channel channel = setup.getAttribute( Channel.class );
		if ( channel != null )
			name += ( name.isEmpty() ? "" : " " ) + "c " + channel.getName();

		return name;
	}
	
	public static void initBrightness( final double cumulativeMinCutoff, final double cumulativeMaxCutoff, final ViewerState state, final ConverterSetups converterSetups )
	{
		final SourceAndConverter< ? > current = state.getCurrentSource();
		if ( current == null )
			return;
		final Source< ? > source = current.getSpimSource();
		final int timepoint = state.getCurrentTimepoint();
		final Bounds bounds = InitializeViewerState.estimateSourceRange( source, timepoint, cumulativeMinCutoff, cumulativeMaxCutoff );
		for ( final SourceAndConverter< ? > s : state.getSources() )
		{
			final ConverterSetup setup = converterSetups.getConverterSetup( s );
			setup.setDisplayRange( bounds.getMinBound(), bounds.getMaxBound() );
			if(setup instanceof GammaConverterSetup)
			{
				((GammaConverterSetup)setup).setAlphaRange( bounds.getMinBound(), bounds.getMaxBound() );
			}
		}
	}
	


}
