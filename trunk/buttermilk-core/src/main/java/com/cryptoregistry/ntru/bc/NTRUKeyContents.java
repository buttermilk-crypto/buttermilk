/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2014 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.ntru.bc;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.cryptoregistry.KeyGenerationAlgorithm;
import com.cryptoregistry.Signer;
import com.cryptoregistry.formats.NTRUParametersFormatter;
import com.cryptoregistry.passwords.Password;
import com.cryptoregistry.pbe.PBEParams;
import com.cryptoregistry.util.ArmoredCompressedString;
import com.cryptoregistry.util.ArmoredString;
import com.cryptoregistry.util.ArrayUtil;
import com.cryptoregistry.util.TimeUtil;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import org.bouncycastle.pqc.crypto.ntru.NTRUEncryptionParameters;
import org.bouncycastle.pqc.crypto.ntru.NTRUEncryptionPrivateKeyParameters;
import org.bouncycastle.pqc.math.ntru.polynomial.DenseTernaryPolynomial;
import org.bouncycastle.pqc.math.ntru.polynomial.IntegerPolynomial;
import org.bouncycastle.pqc.math.ntru.polynomial.Polynomial;
import org.bouncycastle.pqc.math.ntru.polynomial.ProductFormPolynomial;
import org.bouncycastle.pqc.math.ntru.polynomial.SparseTernaryPolynomial;


public class NTRUKeyContents extends NTRUKeyForPublication implements Signer{
	
	public final Polynomial t;
	public final IntegerPolynomial fp;
	
	public NTRUKeyContents(NTRUNamedParameters e, 
			IntegerPolynomial h, Polynomial t, IntegerPolynomial fp) {
		super(e, h);
		this.t = t;
		this.fp = fp;
	}
	
	public NTRUKeyContents(NTRUKeyMetadata metadata, 
			NTRUNamedParameters e, IntegerPolynomial h,
			Polynomial t, IntegerPolynomial fp) {
		super(metadata, e, h);
		this.t = t;
		this.fp = fp;
	}

	public NTRUEncryptionPrivateKeyParameters getPrivateKey() {
		return new NTRUEncryptionPrivateKeyParameters(h,t,fp,parameterSetName.getParameters());
	}
	
	
	
	public NTRUKeyForPublication forPublication(){
		NTRUKeyMetadata meta = this.metadata.cloneForPublication();
		return new NTRUKeyForPublication(meta,parameterSetName,h);
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
	
	@Override
	public String formatJSON() {
		StringWriter writer = new StringWriter();
		JsonFactory f = new JsonFactory();
		JsonGenerator g = null;
		try {
			g = f.createGenerator(writer);
			g.useDefaultPrettyPrinter();
			g.writeStartObject();
			g.writeObjectFieldStart(metadata.handle+"-U");
			g.writeStringField("KeyAlgorithm", KeyGenerationAlgorithm.NTRU.toString());
			g.writeStringField("CreatedOn", TimeUtil.format(metadata.createdOn));
			//g.writeStringField("Encoding", enc.toString());
			
			
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

}
