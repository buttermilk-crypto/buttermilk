package com.cryptoregistry.signature;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import com.cryptoregistry.SignatureAlgorithm;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;

public class DSACryptoSignature extends CryptoSignature {

	private static final long serialVersionUID = 1L;
	
	public final DSASignature signature;

	public DSACryptoSignature(SignatureMetadata metadata, DSASignature sig) {
		super(metadata);
		this.signature=sig;
	}
	
	public DSACryptoSignature(SignatureMetadata metadata, List<String> dataRefs, DSASignature sig) {
		super(metadata,dataRefs);
		this.signature=sig;
	}
	
	/**
	 * Assume SHA-256
	 * 
	 * @param signedWith
	 * @param signedBy
	 * @param sig
	 */
	public DSACryptoSignature(String signedWith, String signedBy, DSASignature sig) {
		super(new SignatureMetadata(
				SignatureAlgorithm.DSA,
				"SHA-256",
				signedWith,
				signedBy));
		this.signature=sig;
	}

	public DSASignature getSignature() {
		return signature;
	}

	@Override
	public void formatSignaturePrimitivesJSON(JsonGenerator g, Writer writer)
			throws JsonGenerationException, IOException {
		
		g.writeStringField("r", signature.r.toString());
		g.writeStringField("s", signature.s.toString());
		
	}

	@Override
	public SignatureBytes signatureBytes() {
		return signature;
	}
}
