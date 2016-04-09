/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith. All Rights Reserved.
 *
 */
package com.cryptoregistry;

/**
 * Interface for any key - either contents or for-publication keys
 * 
 * @author Dave
 *
 */
public interface CryptoKey {

	public CryptoKeyMetadata getMetadata();
	public String formatJSON();
	public CryptoKey keyForPublication();
	
	// helpful for use with Sets, etc.
	public int hashCode();
	public boolean equals(Object obj);
	

}
