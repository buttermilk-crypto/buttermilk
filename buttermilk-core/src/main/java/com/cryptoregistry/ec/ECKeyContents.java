/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2014 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.ec;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.Date;

import com.cryptoregistry.CryptoKey;
import com.cryptoregistry.ECCustomCurve;
import com.cryptoregistry.Signer;
import com.cryptoregistry.formats.FormatUtil;
import com.cryptoregistry.formats.KeyFormat;
import com.cryptoregistry.passwords.Password;
import com.cryptoregistry.pbe.PBEParams;
import com.cryptoregistry.util.MapIterator;
import com.cryptoregistry.util.TimeUtil;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.math.ec.ECPoint;

public class ECKeyContents extends ECKeyForPublication implements Signer {
	
	public final BigInteger d;
	
	/**
	 * Formatting is Mode.Open
	 * @param q
	 * @param curveName
	 * @param d
	 */
	public ECKeyContents(ECPoint q, String curveName, BigInteger d) {
		super(ECKeyMetadata.createDefault(), q, curveName);
		this.d = d;
	}
	
	public ECKeyContents(char [] password, ECPoint q, String curveName, BigInteger d) {
		super(ECKeyMetadata.createSecurePBKDF2(password), q, curveName);
		this.d = d;
	}
	
	public ECKeyContents(ECKeyMetadata metadata, ECPoint q, String curveName, BigInteger d) {
		super(metadata, q, curveName);
		this.d = d;
	}
	
	// NOTICE
	// the constructors below are used in the case of a user-defined curve. <b>Only very advanced
	// cryptographers</b> would even attempt to define their own curves
	
	
	public ECKeyContents(ECPoint q, ECCustomCurve customCurveDefinition, BigInteger d) {
		super(ECKeyMetadata.createDefault(), q, customCurveDefinition);
		this.d = d;
	}
	
	public ECKeyContents(char [] password, ECPoint q, ECCustomCurve customCurveDefinition, BigInteger d) {
		super(ECKeyMetadata.createSecurePBKDF2(password), q, customCurveDefinition);
		this.d = d;
	}
	
	public ECKeyContents(ECKeyMetadata metadata, ECPoint q, ECCustomCurve customCurveDefinition, BigInteger d) {
		super(metadata, q, customCurveDefinition);
		this.d = d;
	}
	
	public ECPrivateKeyParameters getPrivateKey() {
		if(usesNamedCurve()){
			ECDomainParameters domain = CurveFactory.getCurveForName(curveName);
			ECPrivateKeyParameters params = new ECPrivateKeyParameters(d, domain);
			return params;
		}else{
			ECDomainParameters domain = this.customCurveDefinition.getParameters();
			ECPrivateKeyParameters params = new ECPrivateKeyParameters(d, domain);
			return params;
		}
	}
	
	public ECKeyForPublication copyForPublication(){
		ECKeyMetadata meta = metadata.cloneForPublication();
		if(usesNamedCurve()) {
			return new ECKeyForPublication(meta,Q,curveName);
		}else{
			return new ECKeyForPublication(meta,Q,this.customCurveDefinition);
		}
	}
	
	@Override
	public CryptoKey keyForPublication() {
		return copyForPublication();
	}
	
	public ECKeyContents clone(){
		ECKeyMetadata meta = metadata.clone();
		if(usesNamedCurve()) {
			return new ECKeyContents(meta,Q,curveName,d);
		}else{
			return new ECKeyContents(meta,Q,this.customCurveDefinition,d);
		}
	}
	
	public ECKeyForPublication cloneForPublication(){
		ECKeyMetadata meta = metadata.cloneForPublication();
		if(usesNamedCurve()) return new ECKeyForPublication(meta,Q,curveName);
		else return new ECKeyForPublication(meta,Q,customCurveDefinition);
	}
	
	public ECKeyContents clone(KeyFormat format){
		ECKeyMetadata meta = new ECKeyMetadata(this.getHandle(),new Date(this.getCreatedOn().getTime()),format);
		if(usesNamedCurve()) {
			return new ECKeyContents(meta,Q,curveName,d);
		}else{
			return new ECKeyContents(meta,Q,this.customCurveDefinition,d);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((d == null) ? 0 : d.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof ECKeyContents)) return false;
		ECKeyContents item = (ECKeyContents) obj;
		if(item.usesNamedCurve() != this.usesNamedCurve()) return false;
		if(!item.d.equals(this.d)) return false;
		if(!item.Q.equals(this.Q)) return false;
		if(!item.metadata.equals(this.metadata)) return false;
		
		return true;
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
		StringWriter privateDataWriter = new StringWriter();
		JsonFactory f = new JsonFactory();
		JsonGenerator g = null;
		try {
			g = f.createGenerator(privateDataWriter);
			g.useDefaultPrettyPrinter();
			g.writeStartObject();
			g.writeObjectFieldStart(getHandle()+"-U");
			g.writeStringField("KeyAlgorithm", "EC");
			g.writeStringField("CreatedOn", TimeUtil.format(metadata.createdOn));
			g.writeStringField("Encoding", metadata.format.encodingHint.toString());
			g.writeStringField("Q", FormatUtil.serializeECPoint(Q, metadata.format.encodingHint));
			g.writeStringField("D", FormatUtil.wrap(this.metadata.format.encodingHint, this.d));
			if(usesNamedCurve()) {
				g.writeStringField("CurveName", curveName);
			}else{
				g.writeObjectFieldStart("Curve");
				MapIterator iter = (MapIterator) getCustomCurveDefinition();
				while(iter.hasNext()) {
					String key = iter.next();
					String value = iter.get(key);
					g.writeStringField(key, value);
				}
				g.writeEndObject();
			}
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
	 * Only return the public portion here - so don't use this for attempting to glean the key
	 */
	final public String toString() {
		return super.toString();
	}
	
	
}
