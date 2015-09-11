/*
 *  This file is part of Buttermilk(TM) 
 *  Copyright 2015 David R. Smith for cryptoregistry.com
 *
 */
package com.cryptoregistry.client.storage;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.locks.ReentrantLock;

import com.cryptoregistry.passwords.SensitiveBytes;
import com.cryptoregistry.symmetric.AESCBCPKCS7;

/**
 * Helper class for dealing with encryption
 * 
 * @author Dave
 *
 */
public class StorageUtil {

	private static ReentrantLock lock0 = new ReentrantLock();

	public static Object getSecure(SensitiveBytes cachedKey, SecureData data) {

		lock0.lock();
		try {

			String serializedName = data.getDatatype();
			AESCBCPKCS7 aes = new AESCBCPKCS7(cachedKey.getData(), data.getIv());
			byte[] bytes = aes.decrypt(data.getData());
			switch (serializedName) {
			
			case "String": {
				try {
					return new String(bytes, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			default:
				throw new RuntimeException("Unknown datatype: " + serializedName);
			}
		} finally {
			lock0.unlock();
		}
	}
}
