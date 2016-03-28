package com.cryptoregistry.dsa;

import java.math.BigInteger;

public class DSAKeyContents extends DSAKeyForPublication {
	
	final BigInteger x;

	public DSAKeyContents(DSAKeyMetadata meta, BigInteger p, BigInteger q, BigInteger g, BigInteger y, BigInteger x) {
		super(meta,p,q,g,y);
		this.x = x;
	}

}
