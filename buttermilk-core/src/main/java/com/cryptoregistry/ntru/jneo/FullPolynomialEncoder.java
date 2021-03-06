/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith. All Rights Reserved.
 *
 */
package com.cryptoregistry.ntru.jneo;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;

import net.iharder.Base64;

import com.securityinnovation.jneo.math.FullPolynomial;

/**
 * The premise here is just to use a byte array and zip compression to save space - really simple.
 * 
 * @author Dave
 *
 */
public class FullPolynomialEncoder {

	// final prevents reuse - encourages good thread-safety
	final FullPolynomial poly;

	public FullPolynomialEncoder(FullPolynomial poly) {
		this.poly = poly;
	}
	
	/**
	 * Return a base64url encoded String representation of the polynomial coefficients. The string contents
	 * is converted from short[] to bytes and then compressed using zip compression. 
	 * 
	 * @return
	 */

	public String encode() {
		short[] data = poly.p;
		// convert short[] to byte[]
		byte[] bytes;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			for (int i = 0; i < data.length; ++i) {
				dos.writeShort(data[i]);
			}
			
			bytes = baos.toByteArray();

			// zip format, includes header
			Deflater compresser = new Deflater();
			compresser.setInput(bytes);
			compresser.finish();
			byte[] output = new byte[bytes.length];
			int compressedDataLength = compresser.deflate(output);
			compresser.end();
			byte[] compressed = new byte[compressedDataLength];
			System.arraycopy(output, 0, compressed, 0, compressedDataLength);

			return Base64.encodeBytes(compressed, Base64.URL_SAFE);

		} catch (IOException x) {
			throw new RuntimeException(x);
		}
	}

}
