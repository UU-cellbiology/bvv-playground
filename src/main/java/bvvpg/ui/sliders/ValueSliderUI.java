package bvvpg.ui.sliders;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicSliderUI;



/** BigVolumeViewer style single thumb slider **/
public class ValueSliderUI extends BasicSliderUI
{	
	private boolean bHover;
	private boolean bDrag;
	
	public ValueSliderUI( final ValueSlider b )
	{
		super( b );
	}
	
	/**
	 * Returns the size of a thumb.
	 */
	@Override
	protected Dimension getThumbSize()
	{
		return new Dimension( 12, 12 );
	}
	
	/**
	 * Paints the slider. 
	 */
	@Override
	public void paint( final Graphics g, final JComponent c )
	{
		super.paint( g, c );
	}
	
	/**
	 * Creates a listener to handle track events in the specified slider.
	 */
	@Override
	protected TrackListener createTrackListener( final JSlider slider_ )
	{
		return new ValueTrackListener();
	}
	
	/**
	 * Returns a Shape representing a thumb.
	 */
	private Shape createThumbShape( final int width, final int height )
	{
		// Use circular shape.
		final Ellipse2D shape = new Ellipse2D.Double( 0, 0, width, height );
		return shape;
	}
	
	private Color getSliderFillColor()
	{
		return slider.isEnabled() ? Color.lightGray : Color.white;
	}

	private Color getSliderColor()
	{
		return slider.isEnabled() ? Color.darkGray : Color.lightGray;
	}
	
	private Color getSliderHoverColor()
	{
		return Color.lightGray.darker();
	}
	
	/**
	 * Paints the thumb using the specified graphics object.
	 */
	@Override
	public void paintThumb( final Graphics g )
	{
		final Rectangle knobBounds = thumbRect;
		final int w = knobBounds.width;
		final int h = knobBounds.height;

		// Create graphics copy.
		final Graphics2D g2d = ( Graphics2D ) g.create();

		// Create default thumb shape.
		final Shape thumbShape = createThumbShape( w - 1, h - 1 );

		// Draw thumb.
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON );
		g2d.translate( knobBounds.x, knobBounds.y );

		final Color sliderFillColor;
		if(!bHover && ! bDrag)
			sliderFillColor = getSliderFillColor();
		else
			sliderFillColor = getSliderHoverColor();
		g2d.setColor( sliderFillColor );
		g2d.fill( thumbShape );

		final Color sliderColor = getSliderColor();
		g2d.setColor( sliderColor );
		g2d.draw( thumbShape );

		// Dispose graphics.
		g2d.dispose();
	}
	
	/**
	 * Paints the track.
	 */
	@Override
	public void paintTrack( final Graphics g )
	{
		// Draw track.
		super.paintTrack( g );

		final Rectangle trackBounds = trackRect;

		if ( slider.getOrientation() == SwingConstants.HORIZONTAL )
		{
			// Determine position of selected range by moving from the middle
			// of one thumb to the other.
			final int lowerX = trackRect.x;
			final int upperX = thumbRect.x + ( thumbRect.width / 2 );
			//final int upperX = upperThumbRect.x + ( upperThumbRect.width / 2 );

			// Determine track position.
			final int cy = ( trackBounds.height / 2 ) - 2;

			// Save color and shift position.
			final Color oldColor = g.getColor();
			g.translate( trackBounds.x, trackBounds.y + cy );

			// Draw selected range.
			g.setColor( getSliderColor() );
			for ( int y = 0; y <= 2; y++ )
			{
				g.drawLine( lowerX - trackBounds.x, y, upperX - trackBounds.x, y );
			}

			// Restore position and color.
			g.translate( -trackBounds.x, -( trackBounds.y + cy ) );
			g.setColor( oldColor );

		}
		else
		{
			// Determine position of selected range by moving from the middle
			// of one thumb to the other.
			final int lowerY = trackRect.y;
			final int upperY = thumbRect.y + ( thumbRect.height / 2 );

			// Determine track position.
			final int cx = ( trackBounds.width / 2 ) - 2;

			// Save color and shift position.
			final Color oldColor = g.getColor();
			g.translate( trackBounds.x + cx, trackBounds.y );

			// Draw selected range.
			g.setColor( getSliderColor() );
			for ( int x = 0; x <= 2; x++ )
			{
				g.drawLine( x, lowerY - trackBounds.y, x, upperY - trackBounds.y );
			}

			// Restore position and color.
			g.translate( -( trackBounds.x + cx ), -trackBounds.y );
			g.setColor( oldColor );
		}
	}
	
	public class ValueTrackListener extends TrackListener
	{
		@Override
		public void mouseMoved(MouseEvent e) 
		{
			if (!slider.isEnabled()) {
				return;
			}
			currentMouseX = e.getX();
			currentMouseY = e.getY();

			if(thumbRect.contains( currentMouseX, currentMouseY ))
			{
				bHover = true;
				slider.repaint();
			}
			else
			{
				bHover = false;
				slider.repaint();
			}

		}
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
			if ( thumbRect.contains( currentMouseX, currentMouseY ) )
			{
				bDrag = true;
			}
			super.mousePressed(e);
		}
		@Override
		public void mouseReleased( final MouseEvent e )
		{
			bDrag = false;
			slider.setValueIsAdjusting( false );
			super.mouseReleased( e );
		}
		@Override
		public void mouseExited(MouseEvent e) 
		{
			bHover = false;
			slider.repaint();

		}
	}

}
