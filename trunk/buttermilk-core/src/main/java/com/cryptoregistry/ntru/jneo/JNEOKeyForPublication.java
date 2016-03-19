/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.ntru.jneo;

import com.cryptoregistry.CryptoKey;
import com.cryptoregistry.CryptoKeyMetadata;
import com.cryptoregistry.Verifier;
import com.securityinnovation.jneo.math.FullPolynomial;

public class JNEOKeyForPublication implements CryptoKey, Verifier {
	
	protected JNEOKeyMetadata metadata;
	protected JNEONamedParameters namedParameter;
	protected FullPolynomial h = null;

	public JNEOKeyForPublication(JNEOKeyMetadata metadata, 
			JNEONamedParameters namedParameter,
			FullPolynomial h) {
		super();
		this.metadata = metadata;
		this.namedParameter = namedParameter;
		this.h = h;
	}

	@Override
	public CryptoKeyMetadata getMetadata() {
		return metadata;
	}

	@Override
	public String formatJSON() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CryptoKey keyForPublication() {
		return new JNEOKeyForPublication(metadata,namedParameter,h);
	}

}
