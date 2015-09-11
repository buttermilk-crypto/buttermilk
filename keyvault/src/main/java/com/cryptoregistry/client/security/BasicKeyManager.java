/*
 *  This file is part of Buttermilk(TM) 
 *  Copyright 2013-2015 David R. Smith for cryptoregistry.com
 *
 */
package com.cryptoregistry.client.security;

import java.io.File;

import com.cryptoregistry.formats.simplereader.JSONSymmetricKeyReader;
import com.cryptoregistry.passwords.Password;
import com.cryptoregistry.passwords.SensitiveBytes;
import com.cryptoregistry.symmetric.SymmetricKeyContents;

/**
 * Creates and loads key needed for securing the key vault. This basic manager just loads a symmetric key.
 * 
 * @author Dave
 *
 */
public class BasicKeyManager implements KeyManager {
	
	final SymmetricKeyContents key;

	public BasicKeyManager(File path, Password password) {
		JSONSymmetricKeyReader reader = new JSONSymmetricKeyReader(path,password);
		key = reader.parse();
		password.selfDestruct();
	}

	@Override
	public SensitiveBytes loadKey() {
		return new SensitiveBytes(key.getBytes());
	}

}
