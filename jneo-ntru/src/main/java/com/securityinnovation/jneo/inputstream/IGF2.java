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

import com.securityinnovation.jneo.digest.DigestAlgorithm;

/**
 * This class implements the IGF2 Index Generation Function defined in the X9.92
 * specification for NtruEncrypt..
 *
 * <p>
 * An Index Generation Function produces a sequence of numbers in the range
 * [0..N-1] for some N.
 *
 * <p>
 * The IGF2 algorithm is defined in terms of the following inputs:
 * <ul>
 * <li>An underlying bitstream generator
 * <li>The max returnable value N
 * <li>An integer 'bitsPerIndex'
 * <li>A cutoff value, that is the largest multiple of N that is smaller than
 * 2^bitsPerIndex.
 * </ul>
 *
 * <p>
 * The algorithm generates an index by reading 'bitsPerIndex' bits off of the
 * input stream. If the result is larger than or equal to the cutoff the value
 * is discarded and a new value is read. Once a value less than the cutoff is
 * found the igf returns (value % N).
 *
 * <p>
 * In addition to the IGF2 parameters, the default constructor also takes in the
 * parameters needed to initialize an MGF1 byte stream. This is used as the
 * underlying bitstream generator. A secondary constructor is also provided to
 * make testing easier.
 */
public class IGF2 {
	private short maxValue;
	private short bitsPerIndex;
	private int leftoverBits;
	private int numLeftoverBits;
	private int cutoff;
	private InputStream source;

	/**
	 * Create an IGF driven by an MGF1 InputStream.
	 */
	public IGF2(int _maxValue, int _bitsPerIndex, DigestAlgorithm hashAlg,
			int _minNumRuns, byte seed[], int seedoff, int seedLen) {
		MGF1 mgf = new MGF1(hashAlg, _minNumRuns, true, seed, seedoff, seedLen);
		init(_maxValue, _bitsPerIndex, mgf);
	}

	/**
	 * Create an IGF driven by an externally-supplied InputStream.
	 */
	public IGF2(int _maxValue, int _bitsPerIndex, InputStream _source) {
		init(_maxValue, _bitsPerIndex, _source);
	}

	/**
	 * Constructor utility - initialize the IGF2.
	 */
	private void init(int _maxValue, int _bitsPerIndex, InputStream _source) {
		maxValue = (short) _maxValue;
		bitsPerIndex = (short) _bitsPerIndex;
		leftoverBits = 0;
		numLeftoverBits = 0;
		source = _source;
		int modulus = (1 << bitsPerIndex);
		cutoff = modulus - (modulus % maxValue);
	}

	/**
	 * Close the IGF. This clos()es the underlying InputStream.
	 */
	public void close() {
		try {
			source.close();
		} catch (IOException e) {
			throw new InternalError("IGF bit source was unable to close");
		}
	}

	/**
	 * Derive the next index.
	 */
	public int nextIndex() {
		try {
			int ret = 0;
			while (true) {
				// Make sure leftoverBits has at least bitsPerIndex in it.
				while (numLeftoverBits < bitsPerIndex) {
					leftoverBits <<= 8;
					leftoverBits |= (0xff & source.read());
					numLeftoverBits += 8;
				}

				// Pull off bitsPerIndex from leftoverBits. Store in ret.
				int shift = numLeftoverBits - bitsPerIndex;
				ret = 0xffff & (leftoverBits >> shift);
				numLeftoverBits = shift;
				leftoverBits &= ((1 << numLeftoverBits) - 1);

				// If the value is below the cutoff, use it
				if (ret < cutoff)
					return (int) (ret % maxValue);
			}
		} catch (IOException e) {
			throw new InternalError(
					"IGF bit source was unable to generate input");
		}
	}
}
