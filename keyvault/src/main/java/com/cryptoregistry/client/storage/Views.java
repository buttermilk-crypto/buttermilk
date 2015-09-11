/*
 *  This file is part of Buttermilk(TM) 
 *  Copyright 2013 David R. Smith for cryptoregistry.com
 *
 */
package com.cryptoregistry.client.storage;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Map;

import com.cryptoregistry.passwords.SensitiveBytes;
import com.cryptoregistry.symmetric.AESCBCPKCS7;
import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.ClassCatalog;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.collections.StoredSortedMap;
import com.sleepycat.je.EnvironmentStats;

public class Views {

	private final KeyVaultDatabase db;
	private final StoredSortedMap<Handle, SecureData> secureMap; // our secured data
	private final StoredSortedMap<Handle, Metadata> metadataMap; // attributes describing the secure data
	private final StoredSortedMap<Handle, Metadata> regHandleMap; // secondary index by registration handle
	
	private final SecureRandom rand = new SecureRandom();
	
	private final SensitiveBytes cachedKey; // the encryption key

	/**
	 * Create the data bindings and collection views.
	 */

	public Views(KeyVaultDatabase db, SensitiveBytes cachedKey) {

		this.cachedKey = cachedKey;
		this.db = db;
		ClassCatalog catalog = db.getClassCatalog();

		EntryBinding<Handle> secureKeyBinding = new SerialBinding<Handle>(
				catalog, Handle.class);
		EntryBinding<SecureData> secureDataBinding = new SerialBinding<SecureData>(
				catalog, SecureData.class);

		EntryBinding<Handle> metadataKeyBinding = new SerialBinding<Handle>(
				catalog, Handle.class);
		EntryBinding<Metadata> metadataDataBinding = new SerialBinding<Metadata>(
				catalog, Metadata.class);
		
		// this is the reg Handle
		EntryBinding<Handle> regHandleKeyBinding = new SerialBinding<Handle>(
				catalog, Handle.class);
		EntryBinding<Metadata> regHandleDataBinding = new SerialBinding<Metadata>(
				catalog, Metadata.class);

		secureMap = new StoredSortedMap<Handle, SecureData>(
				db.getSecureDatabase(), secureKeyBinding, secureDataBinding,
				true);

		metadataMap = new StoredSortedMap<Handle, Metadata>(
				db.getMetadataDatabase(), metadataKeyBinding,
				metadataDataBinding, true);
		
		regHandleMap = new StoredSortedMap<Handle, Metadata>(
				db.getRegHandleDatabase(), regHandleKeyBinding,
				regHandleDataBinding, true);

	}

	
	public Map<Handle, SecureData> getSecureMap() {
		return secureMap;
	}

	
	public Map<Handle, Metadata> getMetadataMap() {
		return metadataMap;
	}
	
	public Map<Handle, Metadata> getRegHandleMap() {
		return regHandleMap;
	}
	
	/**
	 * Access to the index
	 */
	public Collection<Metadata> getAllForRegHandle(String regHandle){
		return regHandleMap.duplicates(new Handle(regHandle));
	}
	
	public boolean hasRegHandle(String regHandle){
		Collection<Metadata> col = regHandleMap.duplicates(new Handle(regHandle));
		if(col == null || col.size() == 0) return false;
		return true;
	}

	void clearCachedKey() {
		cachedKey.selfDestruct();
	}

	

	protected void putSecure(String handle, Metadata meta, String json) {
		byte[] input = json.getBytes(StandardCharsets.UTF_8);
		Handle key = new Handle(handle);
		
		byte[] iv = new byte[16]; // construct a fresh IV unique to this entry
		rand.nextBytes(iv);
		
		AESCBCPKCS7 aes = new AESCBCPKCS7(cachedKey.getData(), iv);
		byte[] encrypted = aes.encrypt(input);
		
		SecureData value = new SecureData(encrypted, iv);
		this.getSecureMap().put(key, value);
		this.getMetadataMap().put(key, meta);
	}
	

	public String getDbStatus() {
		EnvironmentStats status = this.db.getEnvironment().getStats(null);
		return status.toStringVerbose();
	}
	
}
