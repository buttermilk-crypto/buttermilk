package com.cryptoregistry.ec;

import java.math.BigInteger;

import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

/**
 * Extend domain parameters so the curve name can be passed through
 * 
 * @author Dave
 *
 */
public class NamedECDomainParameters extends ECDomainParameters {
	
	private String name;

	public NamedECDomainParameters(ECCurve arg0, ECPoint arg1, BigInteger arg2) {
		super(arg0, arg1, arg2);
		name = null;
	}

	public NamedECDomainParameters(ECCurve arg0, ECPoint arg1, BigInteger arg2,
			BigInteger arg3) {
		super(arg0, arg1, arg2, arg3);
		name = null;
	}

	public NamedECDomainParameters(ECCurve arg0, ECPoint arg1, BigInteger arg2,
			BigInteger arg3, byte[] arg4) {
		super(arg0, arg1, arg2, arg3, arg4);
		name = null;
	}
	
	public NamedECDomainParameters(ECCurve arg0, ECPoint arg1, BigInteger arg2, String name) {
		super(arg0, arg1, arg2);
		this.name = name;
	}
	
	public NamedECDomainParameters(ECCurve arg0, ECPoint arg1, BigInteger arg2,
			BigInteger arg3, byte[] arg4, String name) {
		super(arg0, arg1, arg2, arg3, arg4);
		this.name = name;
	}
	
	public ECDomainParameters alias(String alias){
		return new NamedECDomainParameters(
				this.getCurve(),
				this.getG(),
				this.getN(),
				this.getH(),
				this.getSeed(),
				alias);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
