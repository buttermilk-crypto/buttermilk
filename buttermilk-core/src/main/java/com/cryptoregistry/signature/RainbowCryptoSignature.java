/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2014 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.signature;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import com.cryptoregistry.SignatureAlgorithm;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;

public class RainbowCryptoSignature extends CryptoSignature {

	private static final long serialVersionUID = 1L;
	
	public final RainbowSignature signature;

	public RainbowCryptoSignature(SignatureMetadata metadata, RainbowSignature sig) {
		super(metadata);
		this.signature=sig;
	}
	
	public RainbowCryptoSignature(SignatureMetadata metadata, List<String> dataRefs, RainbowSignature sig) {
		super(metadata,dataRefs);
		this.signature=sig;
	}
	
	/**
	 * Assume SHA-224
	 * 
	 * @param signedWith
	 * @param signedBy
	 * @param sig
	 */
	public RainbowCryptoSignature(String signedWith, String signedBy, RainbowSignature sig) {
		super(new SignatureMetadata(
				SignatureAlgorithm.Rainbow,
				"SHA-224",
				signedWith,
				signedBy));
		this.signature=sig;
	}

	public RainbowSignature getSignature() {
		return signature;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((signature == null) ? 0 : signature.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RainbowCryptoSignature other = (RainbowCryptoSignature) obj;
		if (signature == null) {
			if (other.signature != null)
				return false;
		} else if (!signature.equals(other.signature))
			return false;
		return true;
	}


	@Override
	public void formatSignaturePrimitivesJSON(JsonGenerator g, Writer writer)
			throws JsonGenerationException, IOException {
		g.writeStringField("s", signature.s.toString());
	}
	
	@Override
	public SignatureBytes signatureBytes() {
		return signature;
	}
}
