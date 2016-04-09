/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith. All Rights Reserved.
 *
 */
package com.cryptoregistry.dsa;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;

import org.bouncycastle.crypto.params.DSAParameters;

import com.cryptoregistry.CryptoKey;
import com.cryptoregistry.CryptoKeyMetadata;
import com.cryptoregistry.Verifier;
import com.cryptoregistry.formats.EncodingHint;
import com.cryptoregistry.formats.FormatUtil;
import com.cryptoregistry.util.TimeUtil;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public class DSAKeyForPublication implements CryptoKey,Verifier {
	
	public final DSAKeyMetadata metadata;
	
	// parameters
	public final BigInteger p, q, g;
	
	public final BigInteger y; // public key

	public DSAKeyForPublication(DSAKeyMetadata meta, BigInteger p, BigInteger q, BigInteger g, BigInteger y) {
		this.metadata = meta;
		this.p = p;
		this.q =q;
		this.g = g;
		this.y = y;
	}
	
	public DSAParameters getParams() {
		return new DSAParameters(p,q,g);
	}

	@Override
	public CryptoKeyMetadata getMetadata() {
		return metadata;
	}

	@Override
	public String formatJSON() {
		StringWriter writer = new StringWriter();
		JsonFactory f = new JsonFactory();
		JsonGenerator _g = null;
		try {
			_g = f.createGenerator(writer);
			_g.useDefaultPrettyPrinter();
			_g.writeStartObject();
			_g.writeObjectFieldStart(getMetadata().getHandle()+"-P");
			_g.writeStringField("KeyAlgorithm", "DSA");
			_g.writeStringField("CreatedOn", TimeUtil.format(metadata.createdOn));
			EncodingHint enc = metadata.getFormat().encodingHint;
			_g.writeStringField("Encoding", enc.toString());
			_g.writeStringField("Strength", String.valueOf(metadata.lengthL));
			_g.writeStringField("P", FormatUtil.wrap(enc, p));
			_g.writeStringField("Q", FormatUtil.wrap(enc, q));
			_g.writeStringField("G", FormatUtil.wrap(enc, g));
			_g.writeStringField("Y", FormatUtil.wrap(enc, y));
		//	_g.writeStringField("X", FormatUtil.wrap(enc, x));
			_g.writeEndObject();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				_g.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		return writer.toString();
	}

	@Override
	public CryptoKey keyForPublication() {
		return cloneForPublication();
	}
	
	public DSAKeyForPublication cloneForPublication(){
		DSAKeyMetadata meta = this.metadata.cloneForPublication();
		return new DSAKeyForPublication(meta,this.p,this.q,this.g,this.y);
	}

}
