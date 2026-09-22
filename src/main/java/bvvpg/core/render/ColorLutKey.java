/*-
 * #%L
 * Volume rendering of bdv datasets with gamma and transparency option
 * %%
 * Copyright (C) 2022 - 2026 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
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
