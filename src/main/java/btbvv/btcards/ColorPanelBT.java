package btbvv.btcards;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.scijava.listeners.Listeners;

import bdv.tools.brightness.ColorIcon;
import bdv.ui.UIUtils;

import net.imglib2.type.numeric.ARGBType;
import net.miginfocom.swing.MigLayout;

public class ColorPanelBT extends JPanel 
{
	private final JButton colorButton;

	private final ARGBType color = new ARGBType();

	public interface ChangeListener
	{
		void colorChanged();
	}

	private final Listeners.List< ChangeListener > listeners = new Listeners.SynchronizedList<>();

	/**
	 * Whether the color reflects a set of sources all having the same color
	 */
	private boolean isConsistent = true;

	/**
	 * Panel background if color reflects a set of sources all having the same color
	 */
	private Color consistentBg = Color.WHITE;

	/**
	 * Panel background if color reflects a set of sources with different colors
	 */
	private Color inConsistentBg = Color.WHITE;

	public ColorPanelBT()
	{
		setLayout( new MigLayout( "ins 0, fillx, filly, hidemode 3", "[grow]", "" ) );
		updateColors();

		colorButton = new JButton();
		this.add( colorButton, "center" );

		colorButton.addActionListener( e -> chooseColor() );

		colorButton.setBorderPainted( false );
		colorButton.setFocusPainted( false );
		colorButton.setContentAreaFilled( false );
		//colorButton.setMinimumSize( new Dimension( 46, 42 ) );
		//colorButton.setPreferredSize( new Dimension( 46, 42 ) );
		colorButton.setMinimumSize( new Dimension( 32, 30 ) );
		colorButton.setPreferredSize( new Dimension( 32, 30 ) );

		setColor( null );
	}

	@Override
	public void setEnabled( final boolean enabled )
	{
		super.setEnabled( enabled );
		if ( colorButton != null )
			colorButton.setEnabled( enabled );
	}

	@Override
	public void updateUI()
	{
		super.updateUI();
		updateColors();
		if ( !isConsistent )
			setBackground( inConsistentBg );
	}

	private void updateColors()
	{
		consistentBg = UIManager.getColor( "Panel.background" );
		inConsistentBg = UIUtils.mix( consistentBg, Color.red, 0.9 );
	}

	public void setConsistent( final boolean isConsistent )
	{
		this.isConsistent = isConsistent;
		setBackground( isConsistent ? consistentBg : inConsistentBg );
	}

	private void chooseColor()
	{
		final Color newColor = JColorChooser.showDialog( null, "Set Source Color", new Color( color.get() ) );
		if ( newColor == null )
			return;
		setColor( new ARGBType(  newColor.getRGB() | 0xff000000 ) );
		listeners.list.forEach( ChangeListener::colorChanged );
	};

	public Listeners< ChangeListener > changeListeners()
	{
		return listeners;
	}

	public synchronized void setColor( final ARGBType color )
	{
		if ( color == null )
			this.color.set( 0xffaaaaaa );
		else
			this.color.set( color );
		//colorButton.setIcon( new ColorIcon( new Color( this.color.get() ), 30, 30, 10, 10, true ) );
		colorButton.setIcon( new ColorIcon( new Color( this.color.get() ), 21, 21, 7, 7, true ) );
	}

	public ARGBType getColor()
	{
		return color.copy();
	}
}
