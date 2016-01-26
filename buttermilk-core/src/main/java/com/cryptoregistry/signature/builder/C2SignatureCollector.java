/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2014 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.signature.builder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.iharder.Base64;
import x.org.bouncycastle.crypto.digests.SHA1Digest;
import x.org.bouncycastle.crypto.digests.SHA256Digest;

import com.cryptoregistry.SignatureAlgorithm;
import com.cryptoregistry.c2.key.Curve25519KeyContents;
import com.cryptoregistry.c2.CryptoFactory;
import com.cryptoregistry.signature.C2CryptoSignature;
import com.cryptoregistry.signature.SignatureMetadata;


/**<pre>
 * 
 * Collector is like C2SignatureBuilder but instead of updating to a digest it collects 
 * all the text as bytes into a buffer. This is used in the case where the bytes are
 * required.
 * 
 * 
 * 
 * </pre>
 * @author Dave
 *
 */
public class C2SignatureCollector extends SignatureBuilder {

	final Curve25519KeyContents sKey;
	final List<String> references;
	final String signedBy;
	final SignatureMetadata meta;
	final ByteArrayOutputStream collector;
	
	// Idempotent constructor, used for testing
	public C2SignatureCollector(String handle, Date createdOn, String signedBy, Curve25519KeyContents sKey) {
		super(true);
		this.sKey = sKey;
		this.references=new ArrayList<String>();
		this.signedBy = signedBy;
		if(signedBy == null) throw new RuntimeException("Registration Handle cannot be null");
		meta = new SignatureMetadata(handle, 
				createdOn,
				SignatureAlgorithm.ECKCDSA, 
				"SHA-256", 
				sKey.getHandle(),
				signedBy);
		
		collector = new ByteArrayOutputStream();
		
		collect(meta.getHandle()+":SignedBy",signedBy);
		collect(".SignedWith",sKey.getHandle());
	}
	
	
	
	/**
	 * By default this constructor updates SignedBy and SignedWith, so even with no other
	 * calls to update you get a meaningful signature out of build()
	 *  
	 * @param signedBy
	 * @param sKey
	 */
	public C2SignatureCollector(String signedBy, Curve25519KeyContents sKey) {
		super(true);
		this.sKey = sKey;
		this.references=new ArrayList<String>();
		this.signedBy = signedBy;
		if(signedBy == null) throw new RuntimeException("Registration Handle cannot be null");
		meta = new SignatureMetadata(
				SignatureAlgorithm.ECKCDSA,
				"SHA-256",
				sKey.getHandle(),
				signedBy);
		
		collector = new ByteArrayOutputStream();
		
		collect(meta.getHandle()+":SignedBy",signedBy);
		collect(".SignedWith",sKey.getHandle());
	}
	
	public C2SignatureCollector collect(String label, String input){
		if(input == null) throw new RuntimeException("Input is null: "+label);
		references.add(label);
		byte [] bytes = input.getBytes(Charset.forName("UTF-8"));
		try {
			collector.write(bytes);
			log(label,bytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log(label,bytes);
		return this;
	}
	
	public C2SignatureCollector collect(String label,byte[] bytes){
		if(bytes == null) throw new RuntimeException("Input is null: "+label);
		references.add(label);
		try {
			collector.write(bytes);
			log(label,bytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log(label,bytes);
		return this;
	}
	
	
	public C2CryptoSignature build(){
		byte [] collected = collector.toByteArray();
		if(this.apropos!=null) meta.setApropos(this.apropos);
		this.log(meta, collected.length);
		SHA1Digest td = new SHA1Digest();
		td.update(collected, 0, collected.length);
		byte [] result = new byte[td.getDigestSize()];
		td.doFinal(result, 0);
		if(this.debugMode){
			try {
				System.err.println("Bytes: "+Base64.encodeBytes(collected, Base64.URL_SAFE));
				System.err.println("SHA1 Digest: "+Base64.encodeBytes(result, Base64.URL_SAFE));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		C2CryptoSignature sig = CryptoFactory.INSTANCE.sign(meta, sKey, collected, new SHA256Digest());
		for(String ref: references) {
			sig.addDataReference(ref);
		}
		references.clear();
		return sig;
	}



	public ByteArrayOutputStream getCollector() {
		return collector;
	}
	
}
