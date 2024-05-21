package btbvv.btuitools;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.IndexColorModel;

import javax.swing.Icon;


public class ColorIconBT implements Icon
{
	private final int width;

	private final int height;

	private final boolean drawAsCircle;

	private final int arcWidth;

	private final int arcHeight;

	private final boolean drawOutline;

	private Color outlineColor;

	private Color color;

	private final int size; // == min(width, height)

	private final int ox;

	private final int oy;
	
	private IndexColorModel icm;

	public ColorIconBT( final Color color, final IndexColorModel icm)
	{
		this( color, icm, 16, 16, true );
	}

	public ColorIconBT( final Color color, final IndexColorModel icm, final int width, final int height, final boolean drawAsCircle )
	{
		this( color, icm, width, height, drawAsCircle, 3, 3, false, null );
	}

	public ColorIconBT( final Color color, final IndexColorModel icm, final int width, final int height, final int arcWidth, final int arcHeight )
	{
		this( color, icm, width, height, false, arcWidth, arcHeight, false, null );
	}

	public ColorIconBT( final Color color, final IndexColorModel icm, final int width, final int height, final int arcWidth, final int arcHeight, final boolean drawOutline )
	{
		this( color, icm, width, height, false, arcWidth, arcHeight, drawOutline, null );
	}

	public ColorIconBT( final Color color, final IndexColorModel icm, final int width, final int height, final int arcWidth, final int arcHeight, final boolean drawOutline, final Color outlineColor )
	{
		this( color, icm, width, height, false, arcWidth, arcHeight, drawOutline, outlineColor );
	}

	private ColorIconBT( final Color color, final IndexColorModel icm, final int width, final int height, final boolean drawAsCircle, final int arcWidth, final int arcHeight, final boolean drawOutline, final Color outlineColor )
	{
		this.color = color;
		this.icm = icm;
		this.width = width;
		this.height = height;
		this.drawAsCircle = drawAsCircle;
		this.arcWidth = arcWidth;
		this.arcHeight = arcHeight;
		this.drawOutline = drawOutline;
		this.outlineColor = outlineColor;

		size = Math.min( width, height );
		ox = ( width - size ) / 2;
		oy = ( height - size ) / 2;
	}

	@Override
	public void paintIcon( final Component c, final Graphics g, final int x, final int y )
	{
		final Graphics2D g2d = ( Graphics2D ) g;
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		final int x0 = x + ox;
		final int y0 = y + oy;
		if ( color == null && icm == null)
		{
			g2d.setColor( new Color( 0xffbcbc ) );
			g2d.fillArc( x0, y0, size, size, 0, 120 );
			g2d.setColor( new Color( 0xbcffbc ) );
			g2d.fillArc( x0, y0, size, size, 120, 120 );
			g2d.setColor( new Color( 0xbcbcff ) );
			g2d.fillArc( x0, y0, size, size, 240, 120 );
		}
		else
		{
			if(color != null)
			{
				g2d.setColor( color );
				if ( drawAsCircle )
					g2d.fillOval( x0, y0, size, size );
				else
					g2d.fillRoundRect( x, y, width, height, arcWidth, arcHeight );
	

			}
			else
			{

				int l = icm.getMapSize()-1;
				if ( drawAsCircle )
				{
					for(int r = size;r>0; r--)
					{
						int ind = Math.round( r*l /size);
						final Color cLUT = new Color(icm.getRed( ind ) ,icm.getGreen( ind ) ,icm.getBlue( ind ) );
						g2d.setColor( cLUT );
						g2d.fillRect( x, y, r, size );
					}
				}
				else
				{
					for(int r = width;r>0; r--)
					{
						int ind = Math.round( r*l /width);
						final Color cLUT = new Color(icm.getRed( ind ) ,icm.getGreen( ind ) ,icm.getBlue( ind ) );
						g2d.setColor( cLUT );
						g2d.fillRect( x, y, r, height);
					}	
				}

			}
			if ( drawOutline )
			{
				final Color oc;
				if ( outlineColor == null )
					oc = c.isFocusOwner() ? new Color( 0x8FC4F9 ) : Color.gray;
				else
					oc = outlineColor;
				g2d.setColor( oc );
				if ( drawAsCircle )
					g2d.drawOval( x0, y0, size, size );
				else
					g2d.drawRoundRect( x, y, width, height, arcWidth, arcHeight );
			}
		}
	}

	@Override
	public int getIconWidth()
	{
		return width;
	}

	@Override
	public int getIconHeight()
	{
		return height;
	}

	public void setColor( final Color color )
	{
		this.color = color;
		this.icm = null;
	}
	public void setICM (final IndexColorModel icm)
	{
		this.icm = icm;
		this.color = null;
	}

	public void setOutlineColor( final Color color )
	{
		this.outlineColor = color;
	}
}
