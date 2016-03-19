/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.ntru.jneo;

import com.securityinnovation.jneo.math.FullPolynomial;

public class JNEOKeyContents extends JNEOKeyForPublication {
	
	// the confidential key
	protected FullPolynomial f = null;

	public JNEOKeyContents(JNEOKeyMetadata metadata, 
			JNEONamedParameters namedParameter,
			FullPolynomial h, FullPolynomial f) {
		super(metadata, namedParameter, h);
		this.f = f;
	}
	
	@Override
	public String formatJSON() {
		// TODO Auto-generated method stub
		return null;
	}

}
