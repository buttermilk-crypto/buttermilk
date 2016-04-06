package com.cryptoregistry.compat;

import java.security.NoSuchAlgorithmException;
import java.security.Security;

import javax.crypto.Cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class Util {

	/**
	 * Will throw RuntimeException if there is an exception or failure of conditions
	 */
	public static final void checkDependencies() {
		// dependency
		if(Security.getProvider("BC") == null) 
			Security.addProvider(new BouncyCastleProvider());
		
		// dependency
		try {
			if(Integer.MAX_VALUE  == Cipher.getMaxAllowedKeyLength("AES")) {
				
			}else{
				throw new RuntimeException("Unlimited Policy jar is not installed, exiting");
			}
			
		} catch (NoSuchAlgorithmException e1) {
			throw new RuntimeException(e1);
		}
	}
}
