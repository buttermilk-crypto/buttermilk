/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.formats;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;

import com.cryptoregistry.formats.EncodingHint;
import com.cryptoregistry.ntru.jneo.FullPolynomialEncoder;
import com.cryptoregistry.ntru.jneo.JNEOKeyContents;
import com.cryptoregistry.ntru.jneo.JNEOKeyForPublication;
import com.cryptoregistry.pbe.ArmoredPBEResult;
import com.cryptoregistry.pbe.ArmoredPBKDF2Result;
import com.cryptoregistry.pbe.ArmoredScryptResult;
import com.cryptoregistry.pbe.PBE;
import com.cryptoregistry.pbe.PBEParams;
import com.cryptoregistry.util.StringToList;
import com.cryptoregistry.util.TimeUtil;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;

class JNEOKeyFormatter {

	protected final JNEOKeyForPublication jneoKeys;
	protected final KeyFormat format;
	protected final PBEParams pbeParams;

	public JNEOKeyFormatter(JNEOKeyForPublication jneoKeys) {
		super();
		this.jneoKeys = jneoKeys;
		this.format = jneoKeys.getMetadata().getFormat();
		this.pbeParams = jneoKeys.getMetadata().getFormat().pbeParams;
	}

	public void formatKeys(JsonGenerator g, Writer writer) {

		try {
			switch (format.mode) {
			case UNSECURED: {
				formatOpen(g, format.encodingHint, writer);
				break;
			}
			case REQUEST_SECURE: {
				seal(g, format.encodingHint, writer);
				break;
			}
			case REQUEST_FOR_PUBLICATION: {
				formatForPublication(g, format.encodingHint, writer);
				break;
			}
			default:
				throw new RuntimeException("Unknown mode");
			}
		}catch(Exception x){
			throw new RuntimeException(x);
		}
		
	}

	protected void seal(JsonGenerator g, EncodingHint enc, Writer writer)
			throws JsonGenerationException, IOException {

		String plain = formatItem(enc);
		ArmoredPBEResult result;
		try {
			byte[] plainBytes = plain.getBytes("UTF-8");
			PBE pbe0 = new PBE(pbeParams);
			result = pbe0.encrypt(plainBytes);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		g.writeObjectFieldStart(jneoKeys.getMetadata().getHandle().toString()+"-S");
		g.writeStringField("KeyData.Type", "JNEO_NTRU");
		g.writeStringField("KeyData.PBEAlgorithm", pbeParams.getAlg().toString());
		
//		g.writeStringField("KeyData.EncryptedData", result.base64Enc);
		g.writeArrayFieldStart("KeyData.EncryptedData");
		List<String> list = new StringToList(result.base64Enc).toList();
			for(String s: list){
				g.writeString(s);
			}
		g.writeEndArray();
		
		g.writeStringField("KeyData.PBESalt", result.base64Salt);

		if (result instanceof ArmoredPBKDF2Result) {
			// specific to PBKDF2
			g.writeStringField("KeyData.Iterations",
					String.valueOf(((ArmoredPBKDF2Result) result).iterations));

		} else if (result instanceof ArmoredScryptResult) {
			// specific to Scrypt
			g.writeStringField("KeyData.IV",
					((ArmoredScryptResult) result).base64IV);
			g.writeStringField("KeyData.BlockSize",
					String.valueOf(((ArmoredScryptResult) result).blockSize));
			g.writeStringField("KeyData.CpuMemoryCost", String
					.valueOf(((ArmoredScryptResult) result).cpuMemoryCost));
			g.writeStringField("KeyData.Parallelization", String
					.valueOf(((ArmoredScryptResult) result).parallelization));
			
		}
		g.writeEndObject();

	}

	protected void formatOpen(JsonGenerator g, EncodingHint enc, Writer writer)
			throws JsonGenerationException, IOException {

		g.writeObjectFieldStart(jneoKeys.getMetadata().getHandle().toString()+"-U");
		g.writeStringField("KeyAlgorithm", "JNEO_NTRU");
		g.writeStringField("CreatedOn", TimeUtil.format(jneoKeys.getMetadata().getCreatedOn()));
		g.writeStringField("Encoding", enc.toString());
		g.writeStringField("ParameterSet", jneoKeys.getNamedParameterSet().name());
		
		g.writeArrayFieldStart("h");
		List<String> list = new StringToList(new FullPolynomialEncoder(jneoKeys.h).encode()).toList();
			for(String s: list){
				g.writeString(s);
			}
		g.writeEndArray();
	//	g.writeStringField("h", new FullPolynomialEncoder(jneoKeys.h).encode());
		
		g.writeArrayFieldStart("f");
		list = new StringToList(new FullPolynomialEncoder(((JNEOKeyContents)jneoKeys).f).encode()).toList();
			for(String s: list){
				g.writeString(s);
			}
		g.writeEndArray();
	//	g.writeStringField("f", new FullPolynomialEncoder(((JNEOKeyContents)jneoKeys).f).encode());
		
		g.writeEndObject();

	}

	protected void formatForPublication(JsonGenerator g, EncodingHint enc,
			Writer writer) throws JsonGenerationException, IOException {

		g.writeObjectFieldStart(jneoKeys.getMetadata().getHandle().toString()+"-P");
		g.writeStringField("KeyAlgorithm", "JNEO_NTRU");
		g.writeStringField("CreatedOn", TimeUtil.format(jneoKeys.getMetadata().getCreatedOn()));
		g.writeStringField("Encoding", enc.toString());
		g.writeStringField("ParameterSet", jneoKeys.getNamedParameterSet().name());
		
		g.writeArrayFieldStart("h");
		List<String> list = new StringToList(new FullPolynomialEncoder(jneoKeys.h).encode()).toList();
			for(String s: list){
				g.writeString(s);
			}
		g.writeEndArray();
	//	g.writeStringField("h", new FullPolynomialEncoder(jneoKeys.h).encode());
		
		g.writeEndObject();

	}

	private String formatItem(EncodingHint enc) {
		StringWriter privateDataWriter = new StringWriter();
		JsonFactory f = new JsonFactory();
		JsonGenerator g = null;
		try {
			g = f.createGenerator(privateDataWriter);
			g.useDefaultPrettyPrinter();
			g.writeStartObject();
			g.writeObjectFieldStart(jneoKeys.getMetadata().getHandle().toString()+"-U");
			g.writeStringField("KeyAlgorithm", "JNEO_NTRU");
			g.writeStringField("CreatedOn", TimeUtil.format(jneoKeys.getMetadata().getCreatedOn()));
			g.writeStringField("Encoding", enc.toString());
			g.writeStringField("ParameterSet", jneoKeys.getNamedParameterSet().name());
			
			g.writeArrayFieldStart("h");
			List<String> list = new StringToList(new FullPolynomialEncoder(jneoKeys.h).encode()).toList();
				for(String s: list){
					g.writeString(s);
				}
			g.writeEndArray();
		//	g.writeStringField("h", new FullPolynomialEncoder(jneoKeys.h).encode());
			
			g.writeArrayFieldStart("f");
			list = new StringToList(new FullPolynomialEncoder(((JNEOKeyContents)jneoKeys).f).encode()).toList();
				for(String s: list){
					g.writeString(s);
				}
			g.writeEndArray();
		//	g.writeStringField("f", new FullPolynomialEncoder(((JNEOKeyContents)jneoKeys).f).encode());
			
			g.writeEndObject();
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
