package bvvpg.core.render;


import java.awt.image.IndexColorModel;
import java.util.Arrays;

/** class generating unique hash per IndexColorModel **/
public class ColorLutKey 
{
	private final int hash;
	private final byte[] argbData;

	public ColorLutKey(IndexColorModel icm) 
	{
		if (icm == null) {
			this.hash = 0;
			this.argbData = new byte[0];
			return;
		}

		int size = icm.getMapSize();
		byte[] a = new byte[size];
		byte[] r = new byte[size];
		byte[] g = new byte[size];
		byte[] b = new byte[size];

		icm.getAlphas(a);
		icm.getReds(r);
		icm.getGreens(g);
		icm.getBlues(b);

		// Interleave or pack all 4 channels (ARGB)
		this.argbData = new byte[size * 4];
		System.arraycopy(a, 0, argbData, 0, size);
		System.arraycopy(r, 0, argbData, size, size);
		System.arraycopy(g, 0, argbData, size * 2, size);
		System.arraycopy(b, 0, argbData, size * 3, size);

		this.hash = Arrays.hashCode(this.argbData);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ColorLutKey)) return false;
		ColorLutKey other = (ColorLutKey) o;
		return Arrays.equals(this.argbData, other.argbData);
	}

	@Override
	public int hashCode() {
		return hash;
	}

}
