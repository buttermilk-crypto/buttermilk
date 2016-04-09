/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith. All Rights Reserved.
 *
 */
package com.cryptoregistry.workbench;

import java.util.EventObject;

import com.cryptoregistry.CryptoKey;

public class UnlockKeyEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	
	private CryptoKey key;

	public UnlockKeyEvent(Object source) {
		super(source);
	}

	public CryptoKey getKey() {
		return key;
	}

	public void setKey(CryptoKey key) {
		this.key = key;
	}

}
