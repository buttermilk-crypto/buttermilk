package com.cryptoregistry.ntru.jneo;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.security.SecureRandom;

import junit.framework.Assert;

import org.bouncycastle.util.Arrays;
import org.junit.BeforeClass;
import org.junit.Test;

import com.securityinnovation.jneo.PlaintextBadLengthException;
import com.securityinnovation.jneo.math.FullPolynomial;

public class CryptoFactoryTest {
	
	static SecureRandom rand;

	@Test
	public void test0() {
		JNEOKeyContents contents = CryptoFactory.INSTANCE.generateKeys();
		Assert.assertNotNull(contents);
		JNEOKeyForPublication pub = (JNEOKeyForPublication) contents.keyForPublication();
		
		byte [] mes = new byte[177]; // max 178
		rand.nextBytes(mes);
		byte [] encrypted = null;
		try {
			encrypted = CryptoFactory.INSTANCE.encrypt(pub, mes);
		} catch (PlaintextBadLengthException e) {
			Assert.fail(e.getMessage());
		}
		
		Assert.assertNotNull(encrypted);
		
		byte [] resultBytes = CryptoFactory.INSTANCE.decrypt(contents, encrypted);
		
		Assert.assertTrue(Arrays.areEqual(mes, resultBytes));
		
	}
	
	@Test
	public void test1() {
		JNEOKeyContents contents = CryptoFactory.INSTANCE.generateKeys();
		JNEONamedParameters np = contents.namedParameterSet;
		JNEOKeyMetadata meta = (JNEOKeyMetadata) contents.getMetadata();
		Assert.assertNotNull(contents);
		FullPolynomialEncoder encoder0 = new FullPolynomialEncoder(contents.h);
		FullPolynomialEncoder encoder1 = new FullPolynomialEncoder(contents.f);
		String s0 = encoder0.encode();
		String s1 = encoder1.encode();
		FullPolynomialDecoder decoder0 = new FullPolynomialDecoder(s0);
		FullPolynomialDecoder decoder1 = new FullPolynomialDecoder(s1);
		FullPolynomial resultH = decoder0.decode();
		FullPolynomial resultF = decoder1.decode();
		
		System.err.println(java.util.Arrays.toString(contents.h.p));
		System.err.println(java.util.Arrays.toString(resultH.p));
		
		boolean b = contents.h.equals(resultH);
		
		Assert.assertTrue(contents.h.equals(resultH));
		Assert.assertTrue(contents.f.equals(resultF));
		
	}
	
	@BeforeClass
	public static void init() {
		rand = new SecureRandom();
	}
	
	@Test
	public void testShortConverter() {
		
		try {
		short [] data = {0,1,2,3,4};
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		for (int i = 0; i < data.length; ++i) {
			dos.writeShort(data[i]);
		}
		
		byte [] bytes = baos.toByteArray();
		
		short [] result = byteToShortArray(bytes,0,bytes.length,java.nio.ByteOrder.BIG_ENDIAN);
		
		Assert.assertTrue(java.util.Arrays.equals(data, result));
		
		}catch(Exception x){
			x.printStackTrace();
			Assert.fail();
		}
	}
	
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
