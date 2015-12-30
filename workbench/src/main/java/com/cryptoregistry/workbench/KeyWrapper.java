package com.cryptoregistry.workbench;

import com.cryptoregistry.CryptoKey;
import com.cryptoregistry.KeyGenerationAlgorithm;
import com.cryptoregistry.ec.ECKeyForPublication;
import com.cryptoregistry.rsa.RSAKeyForPublication;

public class KeyWrapper {
	
	public final CryptoKey key;
	
	public KeyWrapper(CryptoKey key){
		this.key = key;
		if(key == null) throw new RuntimeException("Key cannot be set to null");
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(key.getMetadata().getDistinguishedHandle());
		buf.append(" ");
		buf.append(key.getMetadata().getKeyAlgorithm());
		
		// special case, more info
		if(key.getMetadata().getKeyAlgorithm() == KeyGenerationAlgorithm.EC){
			buf.append(" ");
			buf.append(((ECKeyForPublication)key).curveName);
		}else if(key.getMetadata().getKeyAlgorithm() == KeyGenerationAlgorithm.RSA){
			buf.append(" ");
			buf.append(((RSAKeyForPublication)key).metadata.strength);
		}
		return buf.toString();
	}
}
