package com.cryptoregistry.dsa;

import java.math.BigInteger;

import com.cryptoregistry.CryptoKey;
import com.cryptoregistry.CryptoKeyMetadata;
import com.cryptoregistry.Verifier;

public class DSAKeyForPublication implements CryptoKey,Verifier {
	
	public final DSAKeyMetadata metadata;
	
	// parameters
	public final BigInteger p, q, g;
	
	public final BigInteger y; // public key

	public DSAKeyForPublication(DSAKeyMetadata meta, BigInteger p, BigInteger q, BigInteger g, BigInteger y) {
		this.metadata = meta;
		this.p = p;
		this.q =q;
		this.g = g;
		this.y = y;
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
		// TODO Auto-generated method stub
		return null;
	}

}
