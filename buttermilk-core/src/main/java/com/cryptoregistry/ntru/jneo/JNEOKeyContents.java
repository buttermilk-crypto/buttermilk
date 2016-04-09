/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.ntru.jneo;

import java.io.IOException;
import java.io.StringWriter;

import com.cryptoregistry.Signer;
import com.cryptoregistry.formats.EncodingHint;
import com.cryptoregistry.passwords.Password;
import com.cryptoregistry.pbe.PBEParams;
import com.cryptoregistry.util.Lf2SpacesIndenter;
import com.cryptoregistry.util.TimeUtil;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.securityinnovation.jneo.math.FullPolynomial;

/**
 * The full key contents
 * 
 * @author Dave
 *
 */
public class JNEOKeyContents extends JNEOKeyForPublication implements Signer {
	
	// the confidential key
	public final FullPolynomial f;

	public JNEOKeyContents(JNEOKeyMetadata metadata, 
			JNEONamedParameters namedParameter,
			FullPolynomial h, FullPolynomial f) {
		super(metadata, namedParameter, h);
		this.f = f;
	}
	
	@Override
	public String formatJSON() {
			StringWriter privateDataWriter = new StringWriter();
			JsonFactory _f = new JsonFactory();
			JsonGenerator g = null;
			try {
				g = _f.createGenerator(privateDataWriter);
				g.useDefaultPrettyPrinter();
				DefaultPrettyPrinter pp = (DefaultPrettyPrinter) g.getPrettyPrinter();
				pp.indentArraysWith(new Lf2SpacesIndenter());
				g.writeStartObject();
				g.writeObjectFieldStart(metadata.getHandle()+"-U");
				g.writeStringField("KeyAlgorithm", "JNEO");
				g.writeStringField("CreatedOn", TimeUtil.format(metadata.createdOn));
				g.writeStringField("Encoding", EncodingHint.Base64url.toString());
				g.writeStringField("ParameterSet", this.namedParameterSet.name());
				g.writeStringField("h", new FullPolynomialEncoder(h).encode());
				g.writeStringField("f", new FullPolynomialEncoder(f).encode());
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

	/**
	 * If a password is set in the KeyFormat, clean that out. This call can be made once we're done
	 * with the key materials in this cycle of use. 
	 */
	@Override
	public void scrubPassword() {
		PBEParams params = this.metadata.format.pbeParams;
		if(params != null) {
			Password password = params.getPassword();
			if(password != null && password.isAlive()) password.selfDestruct();
		}
	}

}
