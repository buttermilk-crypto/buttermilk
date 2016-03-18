/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2014 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.symmetric;

import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

/**
 * You should ALWAYS use ephemeral keys and iv with this class. Do not use the direct result
 * of an ECDH key agreement algorithm with GCM as it is a counter mode.
 * 
 * @author Dave
 *
 */
public class AESGCM {

	private final byte [] key;
	private final byte [] iv;
	
	public AESGCM(byte [] key, byte [] iv) {
		this.key = key;
		this.iv = iv;
	}
	
	public byte [] encrypt(byte [] input) {
		ParametersWithIV holder = this.buildKey();
		GCMBlockCipher aesCipher = new GCMBlockCipher(new AESFastEngine());
		aesCipher.init(true, holder);
		try {
			return genCipherData(aesCipher, input);
		}catch(Exception x){
			throw new RuntimeException(x);
		}
	}
	
	public byte [] decrypt(byte [] encrypted) {
		ParametersWithIV holder = this.buildKey();
		GCMBlockCipher aesCipher = new GCMBlockCipher(new AESFastEngine());
		aesCipher.init(false, holder);
		try {
			return genCipherData(aesCipher, encrypted);
		}catch(Exception x){
			throw new RuntimeException(x);
		}
	}
	
	private byte[] genCipherData(GCMBlockCipher cipher, byte[] data) throws Exception {
	    int minSize = cipher.getOutputSize(data.length);
	    byte[] outBuf = new byte[minSize];
	    int length1 = cipher.processBytes(data, 0, data.length, outBuf, 0);
	    int length2 = cipher.doFinal(outBuf, length1);
	    int actualLength = length1 + length2;
	    byte[] result = new byte[actualLength];
	    System.arraycopy(outBuf, 0, result, 0, result.length);
	    return result;
	}
	
	private ParametersWithIV buildKey() {
		ParametersWithIV holder = new ParametersWithIV(
				new KeyParameter(key, 0, key.length), 
				iv, 
				0, 
				iv.length);
		return holder;
	}

}
