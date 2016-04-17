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

import com.cryptoregistry.formats.EncodingHint;
import com.cryptoregistry.pbe.ArmoredPBEResult;
import com.cryptoregistry.pbe.ArmoredPBKDF2Result;
import com.cryptoregistry.pbe.ArmoredScryptResult;
import com.cryptoregistry.pbe.PBE;
import com.cryptoregistry.pbe.PBEParams;
import com.cryptoregistry.rainbow.RainbowKeyContents;
import com.cryptoregistry.rainbow.RainbowKeyForPublication;
import com.cryptoregistry.rainbow.RainbowLayer;
import com.cryptoregistry.util.ArrayUtil;
import com.cryptoregistry.util.StringToList;
import com.cryptoregistry.util.TimeUtil;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;

class RainbowKeyFormatter {

	protected final RainbowKeyForPublication rainbowKeys;
	protected final KeyFormat format;
	protected final PBEParams pbeParams;

	public RainbowKeyFormatter(RainbowKeyForPublication rainbowKeys) {
		super();
		this.rainbowKeys = rainbowKeys;
		this.format = rainbowKeys.getMetadata().getFormat();
		this.pbeParams = rainbowKeys.getMetadata().getFormat().pbeParams;
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

		String plain = formatItem(enc, (RainbowKeyContents)rainbowKeys);
		ArmoredPBEResult result;
		try {
			byte[] plainBytes = plain.getBytes("UTF-8");
			PBE pbe0 = new PBE(pbeParams);
			result = pbe0.encrypt(plainBytes);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		g.writeObjectFieldStart(rainbowKeys.getMetadata().getDistinguishedHandle());
		g.writeStringField("KeyData.Type", "Rainbow");
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

		g.writeObjectFieldStart(rainbowKeys.getMetadata().getDistinguishedHandle());
		g.writeStringField("KeyAlgorithm", "Rainbow");
		g.writeStringField("CreatedOn", TimeUtil.format(rainbowKeys.meta.createdOn));
		g.writeStringField("Encoding", enc.toString());
		g.writeStringField("DocLength", String.valueOf(rainbowKeys.docLength));
		fl(g,"Quadratic", rainbowKeys.coeffQuadratic.encode());
		fl(g,"Singular", rainbowKeys.coeffSingular.encode());
		fl(g,"Scalar", rainbowKeys.coeffScalar.encode());
		
		RainbowKeyContents item = (RainbowKeyContents) rainbowKeys;
		fl(g,"InvA1", item.A1inv.encode());
		fl(g,"B1", item.b1.encode());
		fl(g,"InvA2", item.A2inv.encode());
		fl(g,"B2", item.b2.encode());
		fl(g,"Vi", ArrayUtil.wrap(item.vi).toString());
		List<RainbowLayer> layers = item.layers;
		g.writeArrayFieldStart("Layers");
		for(RainbowLayer rl: layers){
			g.writeStartObject();
			g.writeStringField("Vi", String.valueOf(rl.vi));
			g.writeStringField("ViNext", String.valueOf(rl.viNext));
			fl(g,"Alpha", rl.coeffAlpha.encode());
			fl(g,"Beta", rl.coeffBeta.encode());
			fl(g,"Gamma", rl.coeffGamma.encode());
			fl(g,"Eta", rl.coeffEta.encode());
			g.writeEndObject();
		}
		g.writeEndArray();
		
		g.writeEndObject();

	}

	protected void formatForPublication(JsonGenerator g, EncodingHint enc,
			Writer writer) throws JsonGenerationException, IOException {

		g.writeObjectFieldStart(rainbowKeys.getMetadata().getDistinguishedHandle());
		g.writeStringField("KeyAlgorithm", "Rainbow");
		g.writeStringField("CreatedOn", TimeUtil.format(rainbowKeys.meta.createdOn));
		g.writeStringField("Encoding", enc.toString());
		g.writeStringField("DocLength", String.valueOf(rainbowKeys.docLength));
		fl(g,"Quadratic", rainbowKeys.coeffQuadratic.encode());
		fl(g,"Singular", rainbowKeys.coeffSingular.encode());
		fl(g,"Scalar", rainbowKeys.coeffScalar.encode());
		g.writeEndObject();

	}

	private String formatItem(EncodingHint enc, RainbowKeyContents item) {
		StringWriter privateDataWriter = new StringWriter();
		JsonFactory f = new JsonFactory();
		JsonGenerator g = null;
		try {
			g = f.createGenerator(privateDataWriter);
			g.useDefaultPrettyPrinter();
			g.writeStartObject();
			g.writeObjectFieldStart(rainbowKeys.meta.getHandle()+"-U");
			g.writeStringField("KeyAlgorithm", "Rainbow");
			g.writeStringField("CreatedOn", TimeUtil.format(rainbowKeys.meta.createdOn));
			g.writeStringField("Encoding", enc.toString());
			
			g.writeStringField("DocLength", String.valueOf(rainbowKeys.docLength));
			fl(g,"Quadratic", rainbowKeys.coeffQuadratic.encode());
			fl(g,"Singular", rainbowKeys.coeffSingular.encode());
			fl(g,"Scalar", rainbowKeys.coeffScalar.encode());
			fl(g,"InvA1", item.A1inv.encode());
			fl(g,"B1", item.b1.encode());
			fl(g,"InvA2", item.A2inv.encode());
			fl(g,"B2", item.b2.encode());
			fl(g,"Vi", ArrayUtil.wrap(item.vi).toString());
			List<RainbowLayer> layers = item.layers;
		
			g.writeArrayFieldStart("Layers");
			for(RainbowLayer rl: layers){
				g.writeStartObject();
				g.writeStringField("Vi", String.valueOf(rl.vi));
				g.writeStringField("ViNext", String.valueOf(rl.viNext));
				fl(g,"Alpha", rl.coeffAlpha.encode());
				fl(g,"Beta", rl.coeffBeta.encode());
				fl(g,"Gamma", rl.coeffGamma.encode());
				fl(g,"Eta", rl.coeffEta.encode());
				g.writeEndObject();
				
			}
			g.writeEndArray();
		
			
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
	
	private void fl(JsonGenerator g, String key, String value) throws IOException{
		g.writeArrayFieldStart(key);
		List<String> list = new StringToList(value).toList();
			for(String s: list){
				g.writeString(s);
			}
		g.writeEndArray();
	}

}
