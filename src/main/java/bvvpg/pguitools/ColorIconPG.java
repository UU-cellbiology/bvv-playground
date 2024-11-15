/*-
 * #%L
 * Volume rendering of bdv datasets with gamma and transparency option
 * %%
 * Copyright (C) 2022 - 2024 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package bvvpg.pguitools;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.IndexColorModel;

import javax.swing.Icon;


public class ColorIconPG implements Icon
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

	public ColorIconPG( final Color color, final IndexColorModel icm)
	{
		this( color, icm, 16, 16, true );
	}

	public ColorIconPG( final Color color, final IndexColorModel icm, final int width, final int height, final boolean drawAsCircle )
	{
		this( color, icm, width, height, drawAsCircle, 3, 3, false, null );
	}

	public ColorIconPG( final Color color, final IndexColorModel icm, final int width, final int height, final int arcWidth, final int arcHeight )
	{
		this( color, icm, width, height, false, arcWidth, arcHeight, false, null );
	}

	public ColorIconPG( final Color color, final IndexColorModel icm, final int width, final int height, final int arcWidth, final int arcHeight, final boolean drawOutline )
	{
		this( color, icm, width, height, false, arcWidth, arcHeight, drawOutline, null );
	}

	public ColorIconPG( final Color color, final IndexColorModel icm, final int width, final int height, final int arcWidth, final int arcHeight, final boolean drawOutline, final Color outlineColor )
	{
		this( color, icm, width, height, false, arcWidth, arcHeight, drawOutline, outlineColor );
	}

	private ColorIconPG( final Color color, final IndexColorModel icm, final int width, final int height, final boolean drawAsCircle, final int arcWidth, final int arcHeight, final boolean drawOutline, final Color outlineColor )
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
			drawBlank(g2d, x0, y0); 
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
				if(l>0)
				{
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
				else
				{
					drawBlank (g2d, x0, y0);
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
				if(color != null)
				{
					if ( drawAsCircle )
						g2d.drawOval( x0, y0, size, size );
					else
						g2d.drawRoundRect( x, y, width, height, arcWidth, arcHeight );
				}
				else
				{
					g2d.drawRect( x, y, width, height );
				}
			}
		}
	}
	
	void drawBlank (final Graphics2D g2d, final int x0, final int y0)
	{
		g2d.setColor( new Color( 0xffbcbc ) );
		g2d.fillArc( x0, y0, size, size, 0, 120 );
		g2d.setColor( new Color( 0xbcffbc ) );
		g2d.fillArc( x0, y0, size, size, 120, 120 );
		g2d.setColor( new Color( 0xbcbcff ) );
		g2d.fillArc( x0, y0, size, size, 240, 120 );
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
