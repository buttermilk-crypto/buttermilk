package com.cryptoregistry.workbench;

import java.util.EventObject;

import com.cryptoregistry.CryptoKey;

public class CryptoKeySelectionEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	
	private CryptoKey key;

	public CryptoKeySelectionEvent(Object source) {
		super(source);
	}

	public CryptoKey getKey() {
		return key;
	}

	public void setKey(CryptoKey key) {
		this.key = key;
	}

}
