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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.cryptoregistry.KeyGenerationAlgorithm;
import com.cryptoregistry.ntru.bc.NTRUKeyContents;
import com.cryptoregistry.ntru.bc.NTRUKeyForPublication;
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
import com.cryptoregistry.util.*;

class NTRUKeyFormatter {

	protected NTRUKeyForPublication ntruKeys;
	protected final KeyFormat format;
	protected PBEParams pbeParams;

	public NTRUKeyFormatter(NTRUKeyForPublication ntruKeys) {
		super();
		this.ntruKeys = ntruKeys;
		this.format = ntruKeys.metadata.getFormat();
		this.pbeParams = format.pbeParams;
	}

	public void formatKeys(JsonGenerator g, Writer writer) {

		try {
			switch (format.mode) {
			case UNSECURED: {
				formatOpen(g, writer);
				break;
			}
			case REQUEST_SECURE: {
				seal(g, writer);
				break;
			}
			case REQUEST_FOR_PUBLICATION: {
				formatForPublication(g, writer);
				break;
			}
			default:
				throw new RuntimeException("Unknown mode");
			}
		}catch(Exception x){
			throw new RuntimeException(x);
		}
		
	}

	protected void seal(JsonGenerator g, Writer writer)
			throws JsonGenerationException, IOException {

		String plain = formatItem(writer, (NTRUKeyContents)ntruKeys);
		ArmoredPBEResult result;
		try {
			byte[] plainBytes = plain.getBytes("UTF-8");
			PBE pbe0 = new PBE(pbeParams);
			result = pbe0.encrypt(plainBytes);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		g.writeObjectFieldStart(ntruKeys.getDistinguishedHandle());
		g.writeStringField("KeyData.Type", KeyGenerationAlgorithm.NTRU.toString());
		g.writeStringField("KeyData.PBEAlgorithm", pbeParams.getAlg().toString());
		
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

	protected void formatOpen(JsonGenerator g, Writer writer)
			throws JsonGenerationException, IOException {

		g.writeObjectFieldStart(ntruKeys.getDistinguishedHandle());
		g.writeStringField("KeyAlgorithm", KeyGenerationAlgorithm.NTRU.toString());
		g.writeStringField("CreatedOn", TimeUtil.format(ntruKeys.metadata.createdOn));
		//g.writeStringField("Encoding", enc.toString());
		
		g.writeStringField("h", ntruKeys.wrappedH().toString());
		g.writeStringField("fp", ((NTRUKeyContents)ntruKeys).wrappedFp().toString());
		
		Map<String,ArmoredString> obj = ((NTRUKeyContents)ntruKeys).wrappedT();
		Iterator<String> iter = obj.keySet().iterator();
		// should write td, ts, or tp and an encoded string
		while(iter.hasNext()){
			String key = iter.next();
			g.writeStringField(key, obj.get(key).toString());
		}
		
		NTRUParametersFormatter pFormat = null;
		if(ntruKeys.parameterEnum == null) pFormat = new NTRUParametersFormatter(ntruKeys.params);
		else pFormat = new NTRUParametersFormatter(ntruKeys.parameterEnum);
		pFormat.format(g, writer);
		
		g.writeEndObject();

	}

	protected void formatForPublication(JsonGenerator g, Writer writer) throws JsonGenerationException, IOException {
		
		g.writeObjectFieldStart(ntruKeys.getDistinguishedHandle());
		g.writeStringField("KeyAlgorithm", "NTRU");
		g.writeStringField("CreatedOn", TimeUtil.format(ntruKeys.metadata.createdOn));
	//	g.writeStringField("Encoding", enc.toString());
		g.writeStringField("h", ntruKeys.wrappedH().toString());
		
		NTRUParametersFormatter pFormat = null;
		if(ntruKeys.parameterEnum == null) pFormat = new NTRUParametersFormatter(ntruKeys.params);
		else pFormat = new NTRUParametersFormatter(ntruKeys.parameterEnum);
		pFormat.format(g, writer);
		
		g.writeEndObject();

	}

	private String formatItem(Writer writer, NTRUKeyContents item) {
		StringWriter privateDataWriter = new StringWriter();
		JsonFactory f = new JsonFactory();
		JsonGenerator g = null;
		try {
			g = f.createGenerator(privateDataWriter);
			g.useDefaultPrettyPrinter();
			g.writeStartObject();
			g.writeObjectFieldStart(ntruKeys.metadata.handle+"-U");
			g.writeStringField("KeyAlgorithm", KeyGenerationAlgorithm.NTRU.toString());
			g.writeStringField("CreatedOn", TimeUtil.format(ntruKeys.metadata.createdOn));
			//g.writeStringField("Encoding", enc.toString());
			
			g.writeStringField("h", ntruKeys.wrappedH().toString());
			g.writeStringField("fp", ((NTRUKeyContents)ntruKeys).wrappedFp().toString());
			
			Map<String,ArmoredString> obj = ((NTRUKeyContents)ntruKeys).wrappedT();
			Iterator<String> iter = obj.keySet().iterator();
			// should write td, ts, or tp and an encoded string
			while(iter.hasNext()){
				String key = iter.next();
				g.writeStringField(key, obj.get(key).toString());
			}
			
			NTRUParametersFormatter pFormat = null;
			if(ntruKeys.parameterEnum == null) pFormat = new NTRUParametersFormatter(ntruKeys.params);
			else pFormat = new NTRUParametersFormatter(ntruKeys.parameterEnum);
			pFormat.format(g, writer);
			
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
