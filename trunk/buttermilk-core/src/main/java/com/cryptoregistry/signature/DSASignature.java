package com.cryptoregistry.signature;

import java.math.BigInteger;

import com.cryptoregistry.util.ArmoredString;

public class DSASignature implements SignatureBytes {

	public final ArmoredString r;
	public final ArmoredString s;

	public DSASignature(ArmoredString r, ArmoredString s) {
		super();
		this.r = r;
		this.s = s;
	}
	
	public DSASignature(BigInteger r, BigInteger s) {
		super();
		this.r = new ArmoredString(r.toByteArray());
		this.s = new ArmoredString(s.toByteArray());
	}
	
	public DSASignature(byte [] r, byte [] s) {
		super();
		this.r = new ArmoredString(r);
		this.s = new ArmoredString(s);
	}
	
	@Override
	public byte[] b1() {
		return r.decodeToBytes();
	}
	
	@Override
	public byte[] b2() {
		return s.decodeToBytes();
	}

	@Override
	public boolean hasTwoMembers() {
		return true;
	}

}
