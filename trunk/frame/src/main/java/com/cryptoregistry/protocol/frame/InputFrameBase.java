/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2015 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.protocol.frame;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Base class provides some reusable utility methods to read bytes off streams
 * 
 * @author Dave
 *
 */
public class InputFrameBase {

	// could possibly make use of a byte buffer here, but let's not pre-optimize

	public InputFrameBase() {
	}

	public final void readFully(InputStream in, byte b[], int off, int len)
			throws IOException {
		if (len < 0)
			throw new IndexOutOfBoundsException();
		int n = 0;
		while (n < len) {
			int count = in.read(b, off + n, len - n);
			if (count < 0)
				throw new EOFException();
			n += count;
		}
	}
	
	public final byte readByte(InputStream in)throws IOException {
		int ch1 = in.read();
		return (byte) ch1;
	}
	
	public final int readShort16(InputStream in) throws IOException {
		int ch1 = in.read();
		int ch2 = in.read();
		if ((ch1 | ch2) < 0)
			throw new EOFException();
		return ((ch1 << 8) + (ch2 << 0));
	}

	public final int readInt32(InputStream in) throws IOException {
		int ch1 = in.read();
		int ch2 = in.read();
		int ch3 = in.read();
		int ch4 = in.read();
		if ((ch1 | ch2 | ch3 | ch4) < 0)
			throw new EOFException();
		return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
	}
	
	public final int intFromBytes(byte [] bytes) throws IOException {
		int ch1 = bytes[0];
		int ch2 = bytes[1];
		int ch3 = bytes[2];
		int ch4 = bytes[3];
		if ((ch1 | ch2 | ch3 | ch4) < 0)
			throw new EOFException();
		return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
	}
	
	public final int intFromBytes(byte [] bytes, int off) throws IOException {
		int ch1 = bytes[0+off];
		int ch2 = bytes[1+off];
		int ch3 = bytes[2+off];
		int ch4 = bytes[3+off];
		if ((ch1 | ch2 | ch3 | ch4) < 0)
			throw new EOFException();
		return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
	}

}
