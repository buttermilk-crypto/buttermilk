package com.cryptoregistry.protocol;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import x.org.bouncycastle.crypto.digests.SHA256Digest;
import x.org.bouncycastle.crypto.io.DigestInputStream;
import x.org.bouncycastle.crypto.io.DigestOutputStream;

import com.cryptoregistry.c2.key.Curve25519KeyContents;
import com.cryptoregistry.c2.key.Curve25519KeyForPublication;

public class Handshake {

	List<Module> modules;
	
	public final DigestInputStream in;
	public final DigestOutputStream out;
	public final boolean isClient;
	public final String regHandle;
	
	public Curve25519KeyContents myC2Key;
	public Curve25519KeyForPublication remoteC2Key;
	
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
	
	public void start() {
		
		for(Module mod: modules){
			if(this.currentState==State.Error){
				break;
			}
			mod.run();
		}
	}

}
