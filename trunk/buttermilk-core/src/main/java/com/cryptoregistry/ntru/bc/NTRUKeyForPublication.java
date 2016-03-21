/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2014 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.ntru.bc;

import java.io.IOException;
import java.io.StringWriter;

import com.cryptoregistry.CryptoKey;
import com.cryptoregistry.CryptoKeyMetadata;
import com.cryptoregistry.KeyGenerationAlgorithm;
import com.cryptoregistry.Verifier;
import com.cryptoregistry.formats.NTRUParametersFormatter;
import com.cryptoregistry.util.ArmoredCompressedString;
import com.cryptoregistry.util.ArrayUtil;
import com.cryptoregistry.util.TimeUtil;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import org.bouncycastle.pqc.crypto.ntru.NTRUEncryptionParameters;
import org.bouncycastle.pqc.crypto.ntru.NTRUEncryptionPublicKeyParameters;
import org.bouncycastle.pqc.math.ntru.polynomial.IntegerPolynomial;

/**
 * When parameterName is defined, we can format this using our internal definitions (NTRUNamedParams).
 * this is similar to the concept of a well-known curve name as found in EC.
 * 
 * @author Dave
 *
 */
public class NTRUKeyForPublication implements CryptoKey, Verifier {

	public final NTRUKeyMetadata metadata;
	public final NTRUNamedParameters parameterSetName;
	public final IntegerPolynomial h;
	
	public NTRUKeyForPublication(NTRUNamedParameters e, IntegerPolynomial h) {
		metadata = NTRUKeyMetadata.createDefault();
		this.parameterSetName = e;
		this.h = h;
	}
	
	public NTRUKeyForPublication(NTRUKeyMetadata metadata, NTRUNamedParameters e, IntegerPolynomial h) {
		this.metadata = metadata;
		this.parameterSetName = e;
		this.h = h;
	}
	
	public NTRUEncryptionPublicKeyParameters getPublicKey() {
		return new NTRUEncryptionPublicKeyParameters(h,parameterSetName.getParameters());
	}
	
	public String getDistinguishedHandle() {
		return metadata.handle+"-"+metadata.format.mode.code;
	}

	@Override
	public CryptoKeyMetadata getMetadata() {
		return metadata;
	}
	
	@Override
	public String formatJSON() {
		StringWriter writer = new StringWriter();
		JsonFactory f = new JsonFactory();
		JsonGenerator g = null;
		try {
			g = f.createGenerator(writer);
			g.useDefaultPrettyPrinter();
			g.writeStartObject();
			
			
			g.writeEndObject();
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				g.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		return writer.toString();
	}

	@Override
	public CryptoKey keyForPublication() {
		return new NTRUKeyForPublication(metadata,parameterSetName,h);
	}

}
