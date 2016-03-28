/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2014 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.formats;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import com.cryptoregistry.pbe.ArmoredPBEResult;
import com.cryptoregistry.pbe.PBE;
import com.cryptoregistry.pbe.PBEParams;
import com.cryptoregistry.util.MapIterator;
import com.cryptoregistry.util.TimeUtil;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

/**
 * Produce an Armored confidential key. This allows a confidential key object to be "locked" or "unlocked" with a password
 * in a manner similar to a private key in a keystore. The inner encoding is JSON.
 * 
 * This code knows about all the keys we can create in Buttermilk.
 * 
 * @author Dave
 *
 */
public class KeyEncryptor {

	KeyHolder holder;

	public KeyEncryptor(KeyHolder holder) {
		super();
		this.holder = holder;
	}
	
	/**
	 * TODO - JNEO
	 * 
	 * @param params
	 * @return
	 */
	public ArmoredPBEResult wrap(PBEParams params) {

		String plain = null;
		
		if(holder.c2Keys != null){
			plain = formatC2Item();
		}else if(holder.dsaKeys != null){
			plain = formatDSAItem();
		}else if(holder.ecKeys != null){
			plain = formatECItem();
		}else if(holder.rsaKeys != null){
			plain = formatRSAItem();
		}else if(holder.sKeys != null){
			plain = formatSymItem();
		}
		ArmoredPBEResult result;
		try {
			byte[] plainBytes = plain.getBytes("UTF-8");
			PBE pbe0 = new PBE(params);
			result = pbe0.encrypt(plainBytes);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		return result;
	}

	private String formatC2Item() {
		StringWriter privateDataWriter = new StringWriter();
		JsonFactory f = new JsonFactory();
		JsonGenerator g = null;
		try {
			g = f.createGenerator(privateDataWriter);
			g.useDefaultPrettyPrinter();
			g.writeStartObject();
			g.writeObjectFieldStart(holder.c2Keys.metadata.getHandle()+"-U");
			g.writeStringField("KeyAlgorithm", "Curve25519");
			g.writeStringField("CreatedOn", TimeUtil.format(holder.c2Keys.metadata.createdOn));
			g.writeStringField("Encoding", EncodingHint.Base64url.toString());
			g.writeStringField("P", holder.c2Keys.publicKey.getBase64UrlEncoding());
			g.writeStringField("s", holder.c2Keys.signingPrivateKey.getBase64UrlEncoding());
			g.writeStringField("k", holder.c2Keys.agreementPrivateKey.getBase64UrlEncoding());
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
	
	private String formatDSAItem() {
		StringWriter privateDataWriter = new StringWriter();
		JsonFactory f = new JsonFactory();
		JsonGenerator g = null;
		try {
			g = f.createGenerator(privateDataWriter);
			g.useDefaultPrettyPrinter();
			g.writeStartObject();
			g.writeObjectFieldStart(holder.rsaKeys.metadata.getHandle()+"-U");
			g.writeStringField("KeyAlgorithm", "DSA");
			g.writeStringField("CreatedOn", TimeUtil.format(holder.dsaKeys.metadata.createdOn));
			g.writeStringField("Encoding", holder.dsaKeys.metadata.format.encodingHint.toString());
			g.writeStringField("Strength", String.valueOf(holder.dsaKeys.metadata.lengthL));
			g.writeStringField("P", FormatUtil.wrap(holder.dsaKeys.metadata.format.encodingHint, holder.dsaKeys.p));
			g.writeStringField("Q", FormatUtil.wrap(holder.dsaKeys.metadata.format.encodingHint, holder.dsaKeys.q));
			g.writeStringField("G", FormatUtil.wrap(holder.dsaKeys.metadata.format.encodingHint, holder.dsaKeys.g));
			g.writeStringField("X", FormatUtil.wrap(holder.dsaKeys.metadata.format.encodingHint, holder.dsaKeys.x));
			g.writeStringField("Y", FormatUtil.wrap(holder.dsaKeys.metadata.format.encodingHint, holder.dsaKeys.y));
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
	
	private String formatECItem() {
		StringWriter privateDataWriter = new StringWriter();
		JsonFactory f = new JsonFactory();
		JsonGenerator g = null;
		try {
			g = f.createGenerator(privateDataWriter);
			g.useDefaultPrettyPrinter();
			g.writeStartObject();
			g.writeObjectFieldStart(holder.ecKeys.getHandle()+"-U");
			g.writeStringField("KeyAlgorithm", "EC");
			g.writeStringField("CreatedOn", TimeUtil.format(holder.ecKeys.metadata.createdOn));
			g.writeStringField("Encoding", holder.ecKeys.metadata.format.encodingHint.toString());
			g.writeStringField("Q", FormatUtil.serializeECPoint(holder.ecKeys.Q, holder.ecKeys.metadata.format.encodingHint));
			g.writeStringField("D", FormatUtil.wrap(holder.ecKeys.metadata.format.encodingHint, holder.ecKeys.d));
			if(holder.ecKeys.usesNamedCurve()) {
				g.writeStringField("CurveName", holder.ecKeys.curveName);
			}else{
				g.writeObjectFieldStart("Curve");
				MapIterator iter = (MapIterator) holder.ecKeys.getCustomCurveDefinition();
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
	
	private String formatRSAItem() {
		StringWriter privateDataWriter = new StringWriter();
		JsonFactory f = new JsonFactory();
		JsonGenerator g = null;
		try {
			g = f.createGenerator(privateDataWriter);
			g.useDefaultPrettyPrinter();
			g.writeStartObject();
			g.writeObjectFieldStart(holder.rsaKeys.metadata.getHandle()+"-U");
			g.writeStringField("KeyAlgorithm", "RSA");
			g.writeStringField("CreatedOn", TimeUtil.format(holder.rsaKeys.metadata.createdOn));
			g.writeStringField("Encoding", holder.rsaKeys.metadata.format.encodingHint.toString());
			g.writeStringField("Strength", String.valueOf(holder.rsaKeys.metadata.strength));
			g.writeStringField("Modulus", FormatUtil.wrap(holder.rsaKeys.metadata.format.encodingHint, holder.rsaKeys.modulus));
			g.writeStringField("PublicExponent", FormatUtil.wrap(holder.rsaKeys.metadata.format.encodingHint, holder.rsaKeys.publicExponent));
			g.writeStringField("PrivateExponent", FormatUtil.wrap(holder.rsaKeys.metadata.format.encodingHint, holder.rsaKeys.privateExponent));
			g.writeStringField("P", FormatUtil.wrap(holder.rsaKeys.metadata.format.encodingHint, holder.rsaKeys.p));
			g.writeStringField("Q", FormatUtil.wrap(holder.rsaKeys.metadata.format.encodingHint,holder.rsaKeys.q));
			g.writeStringField("dP", FormatUtil.wrap(holder.rsaKeys.metadata.format.encodingHint, holder.rsaKeys.dP));
			g.writeStringField("dQ", FormatUtil.wrap(holder.rsaKeys.metadata.format.encodingHint, holder.rsaKeys.dQ));
			g.writeStringField("qInv", FormatUtil.wrap(holder.rsaKeys.metadata.format.encodingHint, holder.rsaKeys.qInv));
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

	private String formatSymItem() {
		StringWriter privateDataWriter = new StringWriter();
		JsonFactory f = new JsonFactory();
		JsonGenerator g = null;
		try {
			g = f.createGenerator(privateDataWriter);
			g.useDefaultPrettyPrinter();
			g.writeStartObject();
			g.writeObjectFieldStart(holder.sKeys.metadata.getHandle()+"-U");
			g.writeStringField("KeyAlgorithm", "Symmetric");
			g.writeStringField("CreatedOn", TimeUtil.format(holder.sKeys.metadata.createdOn));
			g.writeStringField("Encoding", EncodingHint.Base64url.toString());
			g.writeStringField("s", holder.sKeys.getBase64UrlEncoding());
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
	
}
