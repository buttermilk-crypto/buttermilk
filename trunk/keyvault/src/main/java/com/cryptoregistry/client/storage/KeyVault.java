/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2015 David R. Smith. All Rights Reserved.
 *
 */
package com.cryptoregistry.client.storage;

import java.io.File;

import com.cryptoregistry.client.security.KeyManager;
import com.cryptoregistry.passwords.SensitiveBytes;
import com.sleepycat.je.DatabaseException;

/**
 * Create a BDB-style data store to use as a Key Vault. The store contents will be encrypted using a key loaded by 
 * the keyManager. This type of store is best suited to a server or other single-access, high performance use-case
 * 
 * @author Dave
 * 
 */
public class KeyVault {

	protected KeyVaultDatabase db;
	protected Views views;
	protected KeyManager keyManager;
	
	

	public KeyVault(String datastoreRootPath, KeyManager keyManager) {
		this.keyManager = keyManager;
		SensitiveBytes cachedKey = keyManager.loadKey();
		initDb(datastoreRootPath, cachedKey);
	}


	protected void initDb(String dataHomeDir, SensitiveBytes cachedKey) throws DatabaseException {

		File dbPathFile = new File(dataHomeDir);
		if (!dbPathFile.exists()) {
			dbPathFile.mkdirs();
		}

		db = new KeyVaultDatabase(dataHomeDir);
		views = new Views(db, cachedKey);
	}

	
	public void close() throws DatabaseException {
		db.close();
	}

	public Views getViews() {
		return views;
	}

	public KeyManager getKeyManager() {
		return keyManager;
	}
	
	/**
	 * used only in testing
	 */
	public void cleanOut() {
		this.getViews().getMetadataMap().clear();
		this.getViews().getSecureMap().clear();
	}

	
}
