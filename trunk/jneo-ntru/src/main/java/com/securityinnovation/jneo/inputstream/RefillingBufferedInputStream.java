/******************************************************************************
 * NTRU Cryptography Reference Source Code
 * Copyright (c) 2009-2013, by Security Innovation, Inc. All rights reserved.
 *
 * Copyright (C) 2009-2013  Security Innovation
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *********************************************************************************/

package com.securityinnovation.jneo.inputstream;

import java.io.InputStream;
import java.io.IOException;

/**
 * <p>
 * This class buffers the output of an InputStream. It is parameterized by an
 * InputStream that it wraps and a buffer length.
 *
 * <p>
 * This differs from the standard java.io.BufferedInputStream in that the
 * standard BIS reads n bytes of input from its wrapped input, and when that is
 * consumed it consistently returns 0. This class, on the other hand, reads n
 * bytes of input from the wrapped input, doles it out, and when it is all
 * consumed it will request another n bytes of input from the wrapped
 * InputStream.
 *
 * <p>
 * This class was written to buffer the output of the X9.82 DRBG. The DRBG is
 * defined to hash its internal state to produce the output stream and discard
 * any hash output in excess of the requested length. This results in
 * unnecessary hash calculations, especially when many small requests are made.
 * Wrapping the X9.82 in a RefillingBufferedInputStream and setting the buffer
 * size to the size of the underlying hash algorithm will eliminate this waste.
 */
public class RefillingBufferedInputStream extends InputStream {
	InputStream is;
	int next;
	byte buf[];

	/**
	 * Constructor.
	 *
	 * @param in
	 *            the underlying input stream.
	 * @param size
	 *            the buffer size.
	 */
	RefillingBufferedInputStream(InputStream in, int size) {
		is = in;
		buf = new byte[size];
		next = buf.length;
	}

	/**
	 * Returns the next byte of input.
	 */
	public int read() throws IOException {
		if (next >= buf.length)
			refill();
		return (0xff & buf[next++]);
	}

	/**
	 * Reads bytes from this byte-input stream into the specified byte array,
	 * starting at the given offset.
	 *
	 * @param b
	 *            destination buffer.
	 * @param off
	 *            offset at which to start storing bytes.
	 * @param len
	 *            maximum number of bytes to read.
	 *
	 *            See the parallel <code>read</code> method in
	 *            <code>InputStream</code>.
	 */
	public int read(byte[] b, int off, int len) throws IOException {
		int origLen = len;
		while (len > 0) {
			if (next >= buf.length)
				refill();
			int count = Math.min(len, buf.length - next);
			System.arraycopy(buf, next, b, off, count);
			off += count;
			len -= count;
			next += count;
		}

		return origLen;
	}

	/**
	 * Actually read the next block of data from the underlying stream.
	 */
	void refill() throws IOException {
		is.read(buf);
		next = 0;
	}
}
