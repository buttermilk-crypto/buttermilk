package com.cryptoregistry.protocol;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import x.org.bouncycastle.crypto.digests.SHA256Digest;
import x.org.bouncycastle.crypto.io.DigestInputStream;
import x.org.bouncycastle.crypto.io.DigestOutputStream;

import com.cryptoregistry.c2.CryptoFactory;
import com.cryptoregistry.c2.key.Curve25519KeyContents;
import com.cryptoregistry.c2.key.Curve25519KeyForPublication;
import com.cryptoregistry.c2.key.SecretKey;

public class Handshake implements Runnable {

	List<Module> modules;
	
	public final DigestInputStream in;
	public final DigestOutputStream out;
	public final boolean isClient;
	public final String regHandle;
	
	public Curve25519KeyContents myC2Key;
	public Curve25519KeyForPublication remoteC2Key;
	
	private SecretKey secretKey;
	
	public State currentState;
	
	public Handshake(
		boolean isClient, 
		InputStream in, 
		OutputStream out, 
		Curve25519KeyContents myKey,
		String regHandle) {
			
			this.modules = new ArrayList<Module>();
			this.in = new DigestInputStream(in, new SHA256Digest());
			this.out = new DigestOutputStream(out, new SHA256Digest());
			this.isClient = isClient;
			this.myC2Key = myKey;
			this.regHandle = regHandle;
	}
	
	public void add(Module mod){
		modules.add(mod);
	}
	
	public void run() {
		
		for(Module mod: modules){
			if(this.currentState==State.Error){
				break;
			}
			mod.run();
		}
		
		if(this.currentState == State.Success) calculateSecret();
		
		if (secretKey!=null){
			currentState=State.Success;
		}
		
	}
	
	private void calculateSecret() {
		secretKey = CryptoFactory.INSTANCE.keyAgreement(this.remoteC2Key.publicKey, this.myC2Key.agreementPrivateKey);
	}

	/**
	 * May be null if something failed
	 * 
	 * @return
	 */
	public SecretKey getSecretKey() {
		return secretKey;
	}

}
