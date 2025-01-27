package bvvpg.pguitools;


import com.formdev.flatlaf.ui.FlatSliderUI;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class RangeSliderUIFL extends FlatSliderUI
{

	/**
	 * Location and size of thumb for upper value.
	 */
	private Rectangle upperThumbRect;

	/**
	 * Indicator that determines whether upper thumb is selected.
	 */
	private boolean upperThumbSelected;

	/**
	 * Indicator that determines whether lower thumb is being dragged.
	 */
	private transient boolean lowerDragging;

	/**
	 * Indicator that determines whether upper thumb is being dragged.
	 */
	private transient boolean upperDragging;
	
	/**
	 * Indicator that determines whether upper thumb is being dragged.
	 */
	private transient boolean trackDragging;
	
	

	/**
	 * Constructs a RangeSliderUI for the specified slider component.
	 *
	 * @param b
	 *     RangeSlider
	 */
	public RangeSliderUIFL( final RangeSliderPG b )
	{
		super( );
	}

	/**
	 * Installs this UI delegate on the specified component.
	 */
	@Override
	public void installUI( final JComponent c )
	{
		upperThumbRect = new Rectangle();
		super.installUI( c );
		
		
	}

	/**
	 * Creates a listener to handle track events in the specified slider.
	 */
	@Override
	protected TrackListener createTrackListener( final JSlider slider_ )
	{
		return new RangeTrackListener();
	}

	/**
	 * Creates a listener to handle change events in the specified slider.
	 */
	@Override
	protected ChangeListener createChangeListener( final JSlider slider_ )
	{
		return new ChangeHandler();
	}

	/**
	 * Updates the dimensions for both thumbs.
	 */
	@Override
	protected void calculateThumbSize()
	{
		// Call superclass method for lower thumb size.
		super.calculateThumbSize();

		// Set upper thumb size.
		upperThumbRect.setSize( thumbRect.width, thumbRect.height );
	}

	/**
	 * Updates the locations for both thumbs.
	 */
	@Override
	protected void calculateThumbLocation()
	{
		// Call superclass method for lower thumb location.
		super.calculateThumbLocation();

		// Adjust upper value to snap to ticks if necessary.
		if ( slider.getSnapToTicks() )
		{
			final int upperValue = slider.getValue() + slider.getExtent();
			int snappedValue = upperValue;
			final int majorTickSpacing = slider.getMajorTickSpacing();
			final int minorTickSpacing = slider.getMinorTickSpacing();
			int tickSpacing = 0;

			if ( minorTickSpacing > 0 )
			{
				tickSpacing = minorTickSpacing;
			}
			else if ( majorTickSpacing > 0 )
			{
				tickSpacing = majorTickSpacing;
			}

			if ( tickSpacing != 0 )
			{
				// If it's not on a tick, change the value
				if ( ( upperValue - slider.getMinimum() ) % tickSpacing != 0 )
				{
					final float temp = ( float ) ( upperValue - slider.getMinimum() ) / ( float ) tickSpacing;
					final int whichTick = Math.round( temp );
					snappedValue = slider.getMinimum() + ( whichTick * tickSpacing );
				}

				if ( snappedValue != upperValue )
				{
					slider.setExtent( snappedValue - slider.getValue() );
				}
			}
		}

		// Calculate upper thumb location. The thumb is centered over its
		// value on the track.
		if ( slider.getOrientation() == SwingConstants.HORIZONTAL )
		{
			final int upperPosition = xPositionForValue( slider.getValue() + slider.getExtent() );
			upperThumbRect.x = upperPosition - ( upperThumbRect.width / 2 );
			upperThumbRect.y = trackRect.y;

		}
		else
		{
			final int upperPosition = yPositionForValue( slider.getValue() + slider.getExtent() );
			upperThumbRect.x = trackRect.x;
			upperThumbRect.y = upperPosition - ( upperThumbRect.height / 2 );
		}
	}

	/**
	 * Paints the slider. The selected thumb is always painted on top of the
	 * other thumb.
	 */
	@Override
	public void paint( final Graphics g, final JComponent c )
	{
		super.paint( g, c );

		final Rectangle clipRect = g.getClipBounds();
		if ( upperThumbSelected )
		{
			// Paint lower thumb first, then upper thumb.
			if ( clipRect.intersects( thumbRect ) )
			{
				paintLowerThumb( g );
			}
			if ( clipRect.intersects( upperThumbRect ) )
			{
				paintUpperThumb( g );
			}

		}
		else
		{
			// Paint upper thumb first, then lower thumb.
			if ( clipRect.intersects( upperThumbRect ) )
			{
				paintUpperThumb( g );
			}
			if ( clipRect.intersects( thumbRect ) )
			{
				paintLowerThumb( g );
			}
		}
	}

	/**
	 * Paints the track.
	 */
	@Override
	public void paintTrack( final Graphics g )
	{
		boolean enabled = slider.isEnabled();
		float tw = UIScale.scale( (float) trackWidth );
		float arc = tw;

		RoundRectangle2D coloredTrack = null;
		RoundRectangle2D track;

		if( slider.getOrientation() == SwingConstants.HORIZONTAL ) {
			final int lowerX = thumbRect.x + ( thumbRect.width / 2 );
			final int upperX = upperThumbRect.x + ( upperThumbRect.width / 2 );
			float y = trackRect.y + (trackRect.height - tw) / 2f;
			if( enabled && isRoundThumb() ) {
				if( slider.getComponentOrientation().isLeftToRight() ) {
					//int cw = thumbRect.x + (thumbRect.width / 2) - trackRect.x;
					int cw = upperX - lowerX;
					coloredTrack = new RoundRectangle2D.Float( lowerX, y, cw, tw, arc, arc );
					track = new RoundRectangle2D.Float( trackRect.x, y, trackRect.width, tw, arc, arc );
				} else {
					int cw = lowerX - upperX;
					//int cw = trackRect.x + trackRect.width - thumbRect.x - (thumbRect.width / 2);
					coloredTrack = new RoundRectangle2D.Float( upperX, y, cw, tw, arc, arc );
					track = new RoundRectangle2D.Float( trackRect.x, y, trackRect.width, tw, arc, arc );
				}
			} else
				track = new RoundRectangle2D.Float( trackRect.x, y, trackRect.width, tw, arc, arc );
		//not tested, but should work
		} else {
			final int lowerY = thumbRect.y + ( thumbRect.height / 2 );
			final int upperY = upperThumbRect.y + ( upperThumbRect.height / 2 );
			float x = trackRect.x + (trackRect.width - tw) / 2f;
			if( enabled && isRoundThumb() ) {
				//int ch = thumbRect.y + (thumbRect.height / 2) - trackRect.y;
				int ch = upperY - lowerY;
				track = new RoundRectangle2D.Float( x, trackRect.y, tw, trackRect.height, arc, arc );
				coloredTrack = new RoundRectangle2D.Float( x, lowerY, tw, trackRect.height - ch, arc, arc );
			} else
				track = new RoundRectangle2D.Float( x, trackRect.y, tw, trackRect.height, arc, arc );
		}
		
		
		g.setColor( enabled ? getTrackColor() : disabledTrackColor );
		((Graphics2D)g).fill( track );
		
		if( coloredTrack != null ) 
		{
			g.setColor( getTrackValueColor() );
			((Graphics2D)g).fill( coloredTrack );
		}
	}

	/**
	 * Overrides superclass method to do nothing. Thumb painting is handled
	 * within the <code>paint()</code> method.
	 */
	@Override
	public void paintThumb( final Graphics g )
	{
		// Do nothing.
	}

	private void paintThumbLU(final Graphics g, final Rectangle rec)
	{
		Object[] oldRenderingHints = FlatUIUtils.setRenderingHints( g );
		Color thumbColor_ = getThumbColor();
		Color defaultForeground = UIManager.getColor( "Slider.foreground" );
		Color color = stateColor( slider, thumbHover, thumbPressed,
			thumbColor_, disabledThumbColor, null, hoverThumbColor, pressedThumbColor );
		color = FlatUIUtils.deriveColor( color, thumbColor_ );

		Color foreground = slider.getForeground();
		Color borderColor = (thumbBorderColor != null && foreground == defaultForeground)
			? stateColor( slider, false, false, thumbBorderColor, disabledThumbBorderColor, focusedThumbBorderColor, null, null )
			: null;

		Color focusedColor_ = FlatUIUtils.deriveColor( this.focusedColor,
			(foreground != defaultForeground) ? foreground : focusBaseColor );

		paintThumb( g, slider, rec, isRoundThumb(), color, borderColor, focusedColor_, thumbBorderWidth, focusWidth );
		FlatUIUtils.resetRenderingHints( g, oldRenderingHints );
		oldRenderingHints = null;
	}
	/**
	 * Paints the thumb for the lower value using the specified graphics object.
	 */
	private void paintLowerThumb( final Graphics g )
	{
		
		paintThumbLU(g, thumbRect);
		
	}

	/**
	 * Paints the thumb for the upper value using the specified graphics object.
	 */
	private void paintUpperThumb( final Graphics g )
	{
		paintThumbLU(g, upperThumbRect);
	}
	
	/**
	 * Sets the location of the upper thumb, and repaints the slider. This is
	 * called when the upper thumb is dragged to repaint the slider. The
	 * <code>setThumbLocation()</code> method performs the same task for the
	 * lower thumb.
	 */
	private void setUpperThumbLocation( final int x, final int y )
	{
		final Rectangle upperUnionRect = new Rectangle();
		upperUnionRect.setBounds( upperThumbRect );

		upperThumbRect.setLocation( x, y );

		SwingUtilities.computeUnion( upperThumbRect.x, upperThumbRect.y, upperThumbRect.width, upperThumbRect.height, upperUnionRect );
		slider.repaint( upperUnionRect.x, upperUnionRect.y, upperUnionRect.width, upperUnionRect.height );
	}

	/**
	 * Moves the selected thumb in the specified direction by a block increment.
	 * This method is called when the user presses the Page Up or Down keys.
	 */
	@Override
	public void scrollByBlock( final int direction )
	{
		synchronized ( slider )
		{
			int blockIncrement = ( slider.getMaximum() - slider.getMinimum() ) / 10;
			if ( blockIncrement <= 0 && slider.getMaximum() > slider.getMinimum() )
			{
				blockIncrement = 1;
			}
			final int delta = blockIncrement * ( ( direction > 0 ) ? POSITIVE_SCROLL : NEGATIVE_SCROLL );

			if ( upperThumbSelected )
			{
				final int oldValue = ( ( RangeSliderPG ) slider ).getUpperValue();
				( ( RangeSliderPG ) slider ).setUpperValue( oldValue + delta );
			}
			else
			{
				final int oldValue = slider.getValue();
				slider.setValue( oldValue + delta );
			}
		}
	}

	/**
	 * Moves the selected thumb in the specified direction by a unit increment.
	 * This method is called when the user presses one of the arrow keys.
	 */
	@Override
	public void scrollByUnit( final int direction )
	{
		synchronized ( slider )
		{
			final int delta = 1 * ( ( direction > 0 ) ? POSITIVE_SCROLL : NEGATIVE_SCROLL );

			if ( upperThumbSelected )
			{
				final int oldValue = ( ( RangeSliderPG ) slider ).getUpperValue();
				( ( RangeSliderPG ) slider ).setUpperValue( oldValue + delta );
			}
			else
			{
				final int oldValue = slider.getValue();
				slider.setValue( oldValue + delta );
			}
		}
	}

	/**
	 * Listener to handle model change events. This calculates the thumb
	 * locations and repaints the slider if the value change is not caused by
	 * dragging a thumb.
	 */
	public class ChangeHandler implements ChangeListener
	{
		@Override
		public void stateChanged( final ChangeEvent arg0 )
		{
			if ( !lowerDragging && !upperDragging )
			{
				calculateThumbLocation();
				slider.repaint();
			}
		}
	}

	/**
	 * Listener to handle mouse movements in the slider track.
	 */
	public class RangeTrackListener extends TrackListener
	{
		//int trackStartDrag;
		int trackDragMin;
		int trackDragMax;
		int lowerPos, upperPos;

		@Override
		public void mousePressed( final MouseEvent e )
		{
			if ( !slider.isEnabled() )
			{ return; }

			currentMouseX = e.getX();
			currentMouseY = e.getY();

			if ( slider.isRequestFocusEnabled() )
			{
				slider.requestFocus();
			}

			// Determine which thumb is pressed. If the upper thumb is
			// selected (last one dragged), then check its position first;
			// otherwise check the position of the lower thumb first.
			boolean lowerPressed = false;
			boolean upperPressed = false;
			if ( upperThumbSelected || slider.getMinimum() == slider.getValue() )
			{
				if ( upperThumbRect.contains( currentMouseX, currentMouseY ) )
				{
					upperPressed = true;
				}
				else if ( thumbRect.contains( currentMouseX, currentMouseY ) )
				{
					lowerPressed = true;
				}
			}
			else
			{
				if ( thumbRect.contains( currentMouseX, currentMouseY ) )
				{
					lowerPressed = true;
				}
				else if ( upperThumbRect.contains( currentMouseX, currentMouseY ) )
				{
					upperPressed = true;
				}
			}
			
			if(!lowerPressed && !upperPressed)
			{
				if( slider.getOrientation() == SwingConstants.HORIZONTAL )
				{	
					offset = currentMouseX;

					trackDragMin = trackRect.x - thumbRect.x - thumbRect.width / 2;
					trackDragMax = trackRect.x + trackRect.width - upperThumbRect.x-upperThumbRect.width/2;
					lowerPos = thumbRect.x;
					upperPos = upperThumbRect.x;
				}
				else
				{
					offset = currentMouseY;
				}
				
				System.out.println( "range pressed" );
				upperThumbSelected = false;
				lowerDragging = false;
				upperDragging = false;
				trackDragging = true;
				return;
			}
			trackDragging = false;

			// Handle lower thumb pressed.
			if ( lowerPressed )
			{
				switch ( slider.getOrientation() )
				{
				case SwingConstants.VERTICAL:
					offset = currentMouseY - thumbRect.y;
					break;
				case SwingConstants.HORIZONTAL:
					offset = currentMouseX - thumbRect.x;
					break;
				}
				upperThumbSelected = false;
				lowerDragging = true;
				trackDragging = false;
				return;
			}
			lowerDragging = false;

			// Handle upper thumb pressed.
			if ( upperPressed )
			{
				switch ( slider.getOrientation() )
				{
				case SwingConstants.VERTICAL:
					offset = currentMouseY - upperThumbRect.y;
					break;
				case SwingConstants.HORIZONTAL:
					offset = currentMouseX - upperThumbRect.x;
					break;
				}
				upperThumbSelected = true;
				upperDragging = true;
				trackDragging = false;
				return;
			}
			upperDragging = false;
		}

		@Override
		public void mouseReleased( final MouseEvent e )
		{
			lowerDragging = false;
			upperDragging = false;
			trackDragging = false;
			slider.setValueIsAdjusting( false );
			super.mouseReleased( e );
		}

		@Override
		public void mouseDragged( final MouseEvent e )
		{
			if ( !slider.isEnabled() )
			{ return; }

			currentMouseX = e.getX();
			currentMouseY = e.getY();

			if ( lowerDragging )
			{
				slider.setValueIsAdjusting( true );
				moveLowerThumb();

			}
			else if ( upperDragging )
			{
				slider.setValueIsAdjusting( true );
				moveUpperThumb();
			}
			else if (trackDragging)
			{
				slider.setValueIsAdjusting( true );
				moveTrack();
			}
		}

		@Override
		public boolean shouldScroll( final int direction )
		{
			//int thumbTop = currentMouseY - offset;
			return false;
		}
		
		private void moveTrack()
		{
			
			//implement vertical dragging + inverted left right?!!
			int trackDisp = currentMouseX - offset;
			
			trackDisp = Math.max( trackDisp, trackDragMin );
			trackDisp = Math.min( trackDisp, trackDragMax );
			int thumbLeft = lowerPos + trackDisp;
			int thumbRight = upperPos + trackDisp;
			setThumbLocation(thumbLeft, trackRect.y );
			setUpperThumbLocation(thumbRight, trackRect.y );
			
			// Update slider range.
			final RangeSliderPG slider_ = ( RangeSliderPG ) RangeSliderUIFL.this.slider;
			slider_.setRange( valueForXPosition(thumbLeft+thumbRect.width / 2), valueForXPosition(thumbRight+upperThumbRect.width / 2) );
			
		}

		/**
		 * Moves the location of the lower thumb, and sets its corresponding
		 * value in the slider.
		 */
		private void moveLowerThumb()
		{
			int thumbMiddle = 0;

			final RangeSliderPG slider_ = ( RangeSliderPG ) RangeSliderUIFL.this.slider;
			if ( slider_.getOrientation() == SwingConstants.VERTICAL )
			{
				final int halfThumbHeight = thumbRect.height / 2;
				int thumbTop = currentMouseY - offset;
				final int trackTop = trackRect.y;
				final int trackBottom = trackRect.y + ( trackRect.height - 1 );

				thumbTop = Math.max( thumbTop, trackTop - halfThumbHeight );
				thumbTop = Math.min( thumbTop, trackBottom - halfThumbHeight );

				setThumbLocation( thumbRect.x, thumbTop );

				// Update slider value.
				thumbMiddle = thumbTop + halfThumbHeight;

				final int lower = valueForYPosition( thumbMiddle );
				final int upper = Math.max( lower, slider_.getUpperValue() );

				final int upperThumbTop = yPositionForValue( upper ) - halfThumbHeight;
				setUpperThumbLocation( thumbRect.x, upperThumbTop );

				slider_.setRange( lower, upper );
			}
			else if ( slider_.getOrientation() == SwingConstants.HORIZONTAL )
			{
				final int halfThumbWidth = thumbRect.width / 2;
				int thumbLeft = currentMouseX - offset;
				final int trackLeft = trackRect.x;
				final int trackRight = trackRect.x + ( trackRect.width - 1 );

				thumbLeft = Math.max( thumbLeft, trackLeft - halfThumbWidth );
				thumbLeft = Math.min( thumbLeft, trackRight - halfThumbWidth );

				setThumbLocation( thumbLeft, thumbRect.y );

				// Update slider range.
				thumbMiddle = thumbLeft + halfThumbWidth;
				final int lower = valueForXPosition( thumbMiddle );
				final int upper = Math.max( lower, slider_.getUpperValue() );

				final int upperThumbLeft = xPositionForValue( upper ) - halfThumbWidth;
				setUpperThumbLocation( upperThumbLeft, thumbRect.y );

				slider_.setRange( lower, upper );
			}
		}

		/**
		 * Moves the location of the upper thumb, and sets its corresponding
		 * value in the slider.
		 */
		private void moveUpperThumb()
		{
			int thumbMiddle = 0;

			final RangeSliderPG slider_ = ( RangeSliderPG ) RangeSliderUIFL.this.slider;
			if ( slider_.getOrientation() == SwingConstants.VERTICAL )
			{
				final int halfThumbHeight = thumbRect.height / 2;
				int thumbTop = currentMouseY - offset;
				final int trackTop = trackRect.y;
				final int trackBottom = trackRect.y + ( trackRect.height - 1 );

				thumbTop = Math.max( thumbTop, trackTop - halfThumbHeight );
				thumbTop = Math.min( thumbTop, trackBottom - halfThumbHeight );

				setUpperThumbLocation( thumbRect.x, thumbTop );

				// Update slider extent.
				thumbMiddle = thumbTop + halfThumbHeight;
				final int upper = valueForYPosition( thumbMiddle );
				final int lower = Math.min( upper, slider_.getValue() );

				final int lowerThumbTop = yPositionForValue( lower ) - halfThumbHeight;
				setThumbLocation( thumbRect.x, lowerThumbTop );

				slider_.setRange( lower, upper );
			}
			else if ( slider_.getOrientation() == SwingConstants.HORIZONTAL )
			{
				final int halfThumbWidth = thumbRect.width / 2;
				int thumbLeft = currentMouseX - offset;
				final int trackLeft = trackRect.x;
				final int trackRight = trackRect.x + ( trackRect.width - 1 );

				thumbLeft = Math.max( thumbLeft, trackLeft - halfThumbWidth );
				thumbLeft = Math.min( thumbLeft, trackRight - halfThumbWidth );

				setUpperThumbLocation( thumbLeft, thumbRect.y );

				// Update slider range.
				thumbMiddle = thumbLeft + halfThumbWidth;
				final int upper = valueForXPosition( thumbMiddle );
				final int lower = Math.min( upper, slider_.getValue() );

				final int lowerThumbLeft = xPositionForValue( lower ) - halfThumbWidth;
				setThumbLocation( lowerThumbLeft, thumbRect.y );

				slider_.setRange( lower, upper );
			}
		}
	}
}
