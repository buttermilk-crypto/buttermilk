package com.cryptoregistry.formats;

import com.cryptoregistry.CryptoKey;
import com.cryptoregistry.c2.key.Curve25519KeyContents;
import com.cryptoregistry.dsa.DSAKeyContents;
import com.cryptoregistry.ec.ECKeyContents;
import com.cryptoregistry.ntru.jneo.JNEOKeyContents;
import com.cryptoregistry.rsa.RSAKeyContents;
import com.cryptoregistry.symmetric.SymmetricKeyContents;

/**
 * Kind of an immutable struct which can hold any required key.
 * 
 * @author Dave
 *
 */
public class KeyHolder {

	final Curve25519KeyContents c2Keys;
	final DSAKeyContents dsaKeys;
	final ECKeyContents ecKeys;
	final JNEOKeyContents jKeys;
	final RSAKeyContents rsaKeys;
	final SymmetricKeyContents sKeys;
	
	public KeyHolder(CryptoKey key) {
		switch(key.getMetadata().getKeyAlgorithm()) {
			case Curve25519: {
				c2Keys = (Curve25519KeyContents)key; 
				sKeys = null;
				ecKeys = null;
				jKeys = null;
				rsaKeys = null;
				dsaKeys = null;
				break;
			}
			case DSA: {
				dsaKeys = (DSAKeyContents) key;
				sKeys = null;
				c2Keys = null;
				ecKeys = null;
				jKeys = null;
				rsaKeys = null;
				break;
			}
			case EC: {
				sKeys = null;
				dsaKeys = null;
				c2Keys = null;
				ecKeys = (ECKeyContents) key; 
				jKeys = null;
				rsaKeys = null;
				
				break;
			}
			case JNEO_NTRU: {
				sKeys = null;
				dsaKeys = null;
				c2Keys = null;
				ecKeys = null;
				jKeys = (JNEOKeyContents) key;
				rsaKeys = null;
				
				break;
			}
			case RSA: {
				sKeys = null;
				dsaKeys = null;
				c2Keys = null;
				jKeys = null;
				ecKeys = null;
				rsaKeys = (RSAKeyContents)key; 
				
				break;
			}
			case Symmetric: {
				sKeys = (SymmetricKeyContents)key; 
				dsaKeys = null;
				c2Keys = null;
				ecKeys = null;
				jKeys = null;
				rsaKeys = null;
				break;
			}
			
			default: {
				dsaKeys = null;
				sKeys = null;
				c2Keys = null;
				ecKeys = null;
				jKeys = null;
				rsaKeys = null;
				break;
			}
		}
	}

}
