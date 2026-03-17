package bvvpg.ui.overlay;

import static bdv.ui.UIUtils.TextPosition.TOP_CENTER;
import static bdv.ui.UIUtils.TextPosition.TOP_RIGHT;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;

import bdv.ui.UIUtils;
import bdv.util.Prefs.OverlayPosition;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.ViewerState;
import mpicbg.spim.data.sequence.TimePoint;

public class SourceInfoColorOverlayRenderer
{
	protected List< TimePoint > timePointsOrdered;

	protected String sourceName;

	protected String groupName;

	protected String timepointString;
	
	protected Color textColor = Color.WHITE;

	protected OverlayPosition sourceNameOverlayPosition = OverlayPosition.TOP_RIGHT;

	public synchronized void paint( final Graphics2D g )
	{
		final Font font = UIUtils.getFont( "monospaced.small.font" );

		g.setColor( textColor );
		g.setFont( font );

		UIUtils.drawString( g, TOP_RIGHT, 0, timepointString );

		switch ( sourceNameOverlayPosition )
		{
		default:
		case TOP_CENTER:
			UIUtils.drawString( g, TOP_CENTER, 0, sourceName );
			UIUtils.drawString( g, TOP_CENTER, 1, groupName );
			break;
		case TOP_RIGHT:
			UIUtils.drawString( g, TOP_RIGHT, 2, sourceName );
			UIUtils.drawString( g, TOP_RIGHT, 3, groupName );
			break;
		}
	}

	public synchronized void setTimePointsOrdered( final List< TimePoint > timePointsOrdered )
	{
		this.timePointsOrdered = timePointsOrdered;
	}
	
	public void setOverlayTextColor (final Color color)
	{
		textColor = new Color(color.getRGB());
	}

	/**
	 * Update data to show in the overlay.
	 */
	@Deprecated
	public synchronized void setViewerState( final bdv.viewer.state.ViewerState state )
	{
		synchronized ( state )
		{
			setViewerState( state.getState() );
		}
	}

	/**
	 * Update data to show in the overlay.
	 */
	public synchronized void setViewerState( final ViewerState state )
	{
		synchronized ( state )
		{
			final SourceAndConverter< ? > currentSource = state.getCurrentSource();
			sourceName = currentSource != null
					? currentSource.getSpimSource().getName() : "";

			final bdv.viewer.SourceGroup currentGroup = state.getCurrentGroup();
			groupName = currentGroup != null && state.getDisplayMode().hasGrouping()
					? state.getGroupName( currentGroup ) : "";

			final int t = state.getCurrentTimepoint();
			if ( timePointsOrdered != null && t >= 0 && t < timePointsOrdered.size() )
				timepointString = String.format( "t = %s", timePointsOrdered.get( t ).getName() );
			else
				timepointString = String.format( "t = %d", t );
		}
	}

	public void setSourceNameOverlayPosition( final OverlayPosition position )
	{
		this.sourceNameOverlayPosition = position;
	}
}
