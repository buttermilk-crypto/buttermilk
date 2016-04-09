/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.ntru.jneo;

import java.io.IOException;
import java.io.StringWriter;

import com.cryptoregistry.CryptoKey;
import com.cryptoregistry.CryptoKeyMetadata;
import com.cryptoregistry.Verifier;
import com.cryptoregistry.formats.EncodingHint;
import com.cryptoregistry.util.Lf2SpacesIndenter;
import com.cryptoregistry.util.TimeUtil;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.securityinnovation.jneo.math.FullPolynomial;

/**
 * The for-publication key
 * 
 * @author Dave
 *
 */
public class JNEOKeyForPublication implements CryptoKey, Verifier {
	
	public final JNEOKeyMetadata metadata;
	public final JNEONamedParameters namedParameterSet;
	public final FullPolynomial h;

	public JNEOKeyForPublication(JNEOKeyMetadata metadata, 
			JNEONamedParameters namedParameterSet,
			FullPolynomial h) {
		super();
		this.metadata = metadata;
		this.namedParameterSet = namedParameterSet;
		this.h = h;
	}

	@Override
	public CryptoKeyMetadata getMetadata() {
		return metadata;
	}

	public JNEONamedParameters getNamedParameterSet() {
		return namedParameterSet;
	}

	@Override
	public String formatJSON() {
			StringWriter privateDataWriter = new StringWriter();
			JsonFactory f = new JsonFactory();
			JsonGenerator g = null;
			try {
				g = f.createGenerator(privateDataWriter);
				g.useDefaultPrettyPrinter();
				DefaultPrettyPrinter pp = (DefaultPrettyPrinter) g.getPrettyPrinter();
				pp.indentArraysWith(new Lf2SpacesIndenter());
				g.writeStartObject();
				g.writeObjectFieldStart(metadata.getHandle()+"-P");
				g.writeStringField("KeyAlgorithm", "JNEO");
				g.writeStringField("CreatedOn", TimeUtil.format(metadata.createdOn));
				g.writeStringField("Encoding", EncodingHint.Base64url.toString());
				g.writeStringField("ParameterSet", this.namedParameterSet.name());
				g.writeStringField("h", new FullPolynomialEncoder(h).encode());
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
			return privateDataWriter.toString();
		}

	@Override
	public CryptoKey keyForPublication() {
		return new JNEOKeyForPublication(metadata,namedParameterSet,h);
	}

}
