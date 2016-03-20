package com.cryptoregistry.ntru.jneo;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import net.iharder.Base64;

import com.securityinnovation.jneo.math.FullPolynomial;

public class FullPolynomialDecoder {

	// final prevents reuse - encourages good thread-safety
	final String base64url;
	
	public FullPolynomialDecoder(String base64url) {
		this.base64url = base64url;
	}
	
	public FullPolynomial decode(){
		try {
			byte [] bytes = Base64.decode(base64url, Base64.URL_SAFE);
			//now uncompress
			   Inflater decompresser = new Inflater();
			     decompresser.setInput(bytes, 0, bytes.length);
			     byte[] result = new byte[bytes.length*50];// should be a big enough buffer
			     int resultLength = decompresser.inflate(result);
			     decompresser.end();
			  //   byte [] exactbytes = new byte[resultLength];
			  //   System.arraycopy(result, 0, exactbytes, 0, resultLength);
			    short [] p = this.byteToShortArray(result, 0, resultLength, java.nio.ByteOrder.BIG_ENDIAN);
			    
			    return new FullPolynomial(p);
			     
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (DataFormatException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	/**
	 * Taken from: https://raw.githubusercontent.com/Red5/red5-hls-plugin/master/plugin/src/main/java/org/red5/stream/util/BufferUtils.java
	 * 
	 * Converts a byte array into a short array. Since a byte is 8-bits,
	 * and a short is 16-bits, the returned short array will be half in
	 * length than the byte array. If the length of the byte array is odd,
	 * the length of the short array will be
	 * <code>(byteArray.length - 1)/2</code>, i.e., the last byte is discarded.
	 *
	 * @param byteArray a byte array
	 * @param offset which byte to start from
	 * @param length how many bytes to convert
	 * @param endian order
	 * order or not.
	 *
	 * @return a short array, or <code>null</code> if byteArray is of zero length
	 *
	 * @throws java.lang.ArrayIndexOutOfBoundsException
	 */
	private short[] byteToShortArray(byte[] byteArray, int offset, int length, ByteOrder order)
			throws ArrayIndexOutOfBoundsException {
		if (0 < length && (offset + length) <= byteArray.length) {
			int shortLength = length / 2;
			short[] shortArray = new short[shortLength];
			int temp;
			for (int i = offset, j = 0; j < shortLength; j++, temp = 0x00000000) {
				if (order == ByteOrder.LITTLE_ENDIAN) {
					temp = byteArray[i++] & 0x000000FF;
					temp |= 0x0000FF00 & (byteArray[i++] << 8);
				} else {
					temp = byteArray[i++] << 8;
					temp |= 0x000000FF & byteArray[i++];
				}
				shortArray[j] = (short) temp;
			}
			return shortArray;
		} else {
			throw new ArrayIndexOutOfBoundsException("offset: " + offset + ", length: " + length + ", array length: " + byteArray.length);
		}
	}


}
