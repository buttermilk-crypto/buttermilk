/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.ntru.jneo;

import com.securityinnovation.jneo.math.FullPolynomial;
import com.securityinnovation.jneo.ntruencrypt.KeyParams;

public class JNEOKeyContents extends JNEOKeyForPublication {
	
	// the confidential key
	protected FullPolynomial f = null;

	public JNEOKeyContents(JNEOKeyMetadata metadata, KeyParams keyParams,
			FullPolynomial h, FullPolynomial f) {
		super(metadata, keyParams, h);
		this.f = f;
	}
	
	@Override
	public String formatJSON() {
		// TODO Auto-generated method stub
		return null;
	}

}
