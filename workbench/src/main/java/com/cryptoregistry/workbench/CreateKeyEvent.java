/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith. All Rights Reserved.
 *
 */
package com.cryptoregistry.workbench;

import java.util.EventObject;

import com.cryptoregistry.CryptoKey;

public class CreateKeyEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	
	private CryptoKey key;
	private CryptoKey keyForPublication;
	private String textualRepresentation;

	public CreateKeyEvent(Object source) {
		super(source);
	}

	public CryptoKey getKey() {
		return key;
	}

	public void setKey(CryptoKey key) {
		this.key = key;
	}

	public CryptoKey getKeyForPublication() {
		return keyForPublication;
	}

	public void setKeyForPublication(CryptoKey keyForPublication) {
		this.keyForPublication = keyForPublication;
	}

	public String getTextualRepresentation() {
		return textualRepresentation;
	}

	public void setTextualRepresentation(String textualRepresentation) {
		this.textualRepresentation = textualRepresentation;
	}
	
}
