/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith. All Rights Reserved.
 *
 */
package com.cryptoregistry;

import com.cryptoregistry.passwords.Password;
import com.cryptoregistry.pbe.PBEParams;


public interface CryptoKeyWrapper {

	String distingushedHandle();
	
	// this is only set if isSecure == false
	CryptoKeyMetadata getMetadata();
	
	Class<?> getWrappedType();
	
	// this is only usable if isSecure == false
	boolean isForPublication();
	
	// capable of being unlocked, if we are for publication this also returns false
	boolean isSecure();
	
	// opens key contents, net result is contents becomes unsecure
	boolean unlock(Password password); // returns false if fails to unlock
	
	// net result is key contents becomes secure
	void lock(PBEParams params); // throws RuntimeException if contents is ForPublication or there was error
	
	CryptoKey getKeyContents();
	void setKeyContents(Object obj);
	
}
