/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2014 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.formats;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;

import com.cryptoregistry.ec.ECKeyContents;
import com.cryptoregistry.ec.ECKeyForPublication;
import com.cryptoregistry.formats.EncodingHint;
import com.cryptoregistry.formats.FormatUtil;
import com.cryptoregistry.pbe.ArmoredPBEResult;
import com.cryptoregistry.pbe.ArmoredPBKDF2Result;
import com.cryptoregistry.pbe.ArmoredScryptResult;
import com.cryptoregistry.pbe.PBE;
import com.cryptoregistry.pbe.PBEParams;
import com.cryptoregistry.util.MapIterator;
import com.cryptoregistry.util.StringToList;
import com.cryptoregistry.util.TimeUtil;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;

public class ECKeyFormatter {

	protected ECKeyForPublication ecKeys;
	protected final KeyFormat format;
	protected PBEParams pbeParams;

	public ECKeyFormatter(ECKeyForPublication ecKeys) {
		super();
		this.ecKeys = ecKeys;
		this.format = ecKeys.getFormat();
		this.pbeParams = ecKeys.getFormat().pbeParams;
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

		g.writeObjectFieldStart(ecKeys.getDistinguishedHandle());
		g.writeStringField("KeyData.Type", "EC");
		g.writeStringField("KeyData.PBEAlgorithm", pbeParams.getAlg()
				.toString());
	//	g.writeStringField("KeyData.EncryptedData", result.base64Enc);
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

		g.writeObjectFieldStart(ecKeys.getDistinguishedHandle());
		g.writeStringField("KeyAlgorithm", "EC");
		g.writeStringField("CreatedOn", TimeUtil.format(ecKeys.metadata.createdOn));
		g.writeStringField("Encoding", enc.toString());
		g.writeStringField("Q", FormatUtil.serializeECPoint(ecKeys.Q, enc));
		g.writeStringField("D", FormatUtil.wrap(enc, ((ECKeyContents)ecKeys).d));
		if(ecKeys.usesNamedCurve()) {
			g.writeStringField("CurveName", ecKeys.curveName);
		}else{
			g.writeObjectFieldStart("Curve");
			MapIterator iter = (MapIterator) ecKeys.getCustomCurveDefinition();
			while(iter.hasNext()) {
				String key = iter.next();
				String value = iter.get(key);
				g.writeStringField(key, value);
			}
			g.writeEndObject();
		}
		g.writeEndObject();

	}

	protected void formatForPublication(JsonGenerator g, EncodingHint enc,
			Writer writer) throws JsonGenerationException, IOException {

		g.writeObjectFieldStart(ecKeys.getDistinguishedHandle());
		g.writeStringField("KeyAlgorithm", "EC");
		g.writeStringField("CreatedOn", TimeUtil.format(ecKeys.metadata.createdOn));
		g.writeStringField("Encoding", enc.toString());
		g.writeStringField("Q", FormatUtil.serializeECPoint(ecKeys.Q, enc));
		if(ecKeys.usesNamedCurve()) {
			g.writeStringField("CurveName", ecKeys.curveName);
		}else{
			g.writeObjectFieldStart("Curve");
			MapIterator iter = (MapIterator) ecKeys.getCustomCurveDefinition();
			while(iter.hasNext()) {
				String key = iter.next();
				String value = iter.get(key);
				g.writeStringField(key, value);
			}
			g.writeEndObject();
		}
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
			g.writeObjectFieldStart(ecKeys.getHandle()+"-U");
			g.writeStringField("KeyAlgorithm", "EC");
			g.writeStringField("CreatedOn", TimeUtil.format(ecKeys.metadata.createdOn));
			g.writeStringField("Encoding", enc.toString());
			g.writeStringField("Q", FormatUtil.serializeECPoint(ecKeys.Q, enc));
			g.writeStringField("D", FormatUtil.wrap(enc, ((ECKeyContents)ecKeys).d));
			if(ecKeys.usesNamedCurve()) {
				g.writeStringField("CurveName", ecKeys.curveName);
			}else{
				g.writeObjectFieldStart("Curve");
				MapIterator iter = (MapIterator) ecKeys.getCustomCurveDefinition();
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

}
