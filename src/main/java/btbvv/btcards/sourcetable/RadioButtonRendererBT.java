package btbvv.btcards.sourcetable;

import java.awt.Component;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

/**
 * @author Tobias Pietzsch
 */
class RadioButtonRendererBT extends JRadioButton implements TableCellRenderer
{
	private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

	public RadioButtonRendererBT() {
		setHorizontalAlignment( SwingConstants.CENTER);
		setBorderPainted( true );
		setOpaque( true );
	}

	@Override
	public Component getTableCellRendererComponent( final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column )
	{
		if ( isSelected )
		{
			setForeground( table.getSelectionForeground() );
			setBackground( table.getSelectionBackground() );
		}
		else
		{
			setForeground( table.getForeground() );
			setBackground( table.getBackground() );
		}
		setSelected( ( value != null && ( Boolean ) value ) );

		if ( hasFocus )
		{
			setBorder( UIManager.getBorder( "Table.focusCellHighlightBorder" ) );
		}
		else
		{
			setBorder( noFocusBorder );
		}

		return this;
	}
}
