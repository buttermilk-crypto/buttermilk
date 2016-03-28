package com.cryptoregistry.dsa;

import java.math.BigInteger;

import com.cryptoregistry.Signer;
import com.cryptoregistry.passwords.Password;
import com.cryptoregistry.pbe.PBEParams;

public class DSAKeyContents extends DSAKeyForPublication implements Signer{
	
	public final BigInteger x;

	public DSAKeyContents(DSAKeyMetadata meta, BigInteger p, BigInteger q, BigInteger g, BigInteger y, BigInteger x) {
		super(meta,p,q,g,y);
		this.x = x;
	}

	/**
	 * If a password is set in the KeyFormat, clean that out. This call can be made once we're done
	 * with the key materials in this cycle of use. 
	 */
	@Override
	public void scrubPassword() {
		PBEParams params = this.metadata.format.pbeParams;
		if(params != null) {
			Password password = params.getPassword();
			if(password != null && password.isAlive()) password.selfDestruct();
		}	
	}
	

}
