package btbvv.btuitools;

import java.awt.Color;

import javax.swing.border.TitledBorder;

public class CenteredTitle extends TitledBorder{


	public CenteredTitle(String paramString) {
		super(paramString);
		this.setTitleColor(Color.DARK_GRAY);
		this.setTitleJustification(TitledBorder.CENTER);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


}
