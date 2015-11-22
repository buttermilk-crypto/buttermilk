package com.cryptoregistry.mac;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.junit.Test;

import net.iharder.Base64;
import x.org.bouncycastle.crypto.Digest;
import x.org.bouncycastle.crypto.digests.SHA256Digest;
import x.org.bouncycastle.crypto.macs.HMac;
import x.org.bouncycastle.crypto.params.KeyParameter;


public class HMacTest {
	
	@Test
	public void test0() throws NoSuchAlgorithmException {
		
		SecureRandom rand = SecureRandom.getInstanceStrong();
		byte [] key = new byte [128];
		rand.nextBytes(key);
		
		byte [] mac = hmac("My Test String", key, new SHA256Digest());
		try {
			String macString = Base64.encodeBytes(mac, Base64.URL_SAFE);
			System.err.println(macString);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private byte[] hmac(String toEncode, byte[] key, Digest digest) {
		HMac hmac = new HMac(digest);
		byte[] resBuf = new byte[hmac.getMacSize()];
		byte[] plainBytes = toEncode.getBytes(StandardCharsets.UTF_8);
		byte[] keyBytes = key;
		hmac.init(new KeyParameter(keyBytes));
		hmac.update(plainBytes, 0, plainBytes.length);
		hmac.doFinal(resBuf, 0);
		return resBuf;
	}

}
