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

import com.cryptoregistry.dsa.DSAKeyForPublication;
import com.cryptoregistry.formats.EncodingHint;
import com.cryptoregistry.formats.FormatUtil;
import com.cryptoregistry.pbe.ArmoredPBEResult;
import com.cryptoregistry.pbe.ArmoredPBKDF2Result;
import com.cryptoregistry.pbe.ArmoredScryptResult;
import com.cryptoregistry.pbe.PBE;
import com.cryptoregistry.pbe.PBEParams;
import com.cryptoregistry.dsa.DSAKeyContents;
import com.cryptoregistry.util.StringToList;
import com.cryptoregistry.util.TimeUtil;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;

class DSAKeyFormatter {

	protected final DSAKeyForPublication dsaKeys;
	protected final KeyFormat format;
	protected final PBEParams pbeParams;

	public DSAKeyFormatter(DSAKeyForPublication dsaKeys) {
		super();
		this.dsaKeys = dsaKeys;
		this.format = dsaKeys.getMetadata().getFormat();
		this.pbeParams = dsaKeys.getMetadata().getFormat().pbeParams;
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

		String plain = formatItem(enc, (DSAKeyContents) dsaKeys);
		ArmoredPBEResult result;
		try {
			byte[] plainBytes = plain.getBytes("UTF-8");
			PBE pbe0 = new PBE(pbeParams);
			result = pbe0.encrypt(plainBytes);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		g.writeObjectFieldStart(dsaKeys.getMetadata().getDistinguishedHandle());
		g.writeStringField("KeyData.Type", "DSA");
		g.writeStringField("KeyData.Strength", String.valueOf(dsaKeys.metadata.lengthL));
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

		g.writeObjectFieldStart(dsaKeys.getMetadata().getHandle()+"-U");
		g.writeStringField("KeyAlgorithm", "DSA");
		g.writeStringField("CreatedOn", TimeUtil.format(dsaKeys.metadata.createdOn));
		g.writeStringField("Encoding", enc.toString());
		g.writeStringField("Strength", String.valueOf(dsaKeys.metadata.lengthL));
		g.writeStringField("P", FormatUtil.wrap(enc, dsaKeys.p));
		g.writeStringField("Q", FormatUtil.wrap(enc, dsaKeys.q));
		g.writeStringField("G", FormatUtil.wrap(enc, dsaKeys.g));
		g.writeStringField("Y", FormatUtil.wrap(enc, dsaKeys.y));
		DSAKeyContents contents = (DSAKeyContents) dsaKeys;
		g.writeStringField("X", FormatUtil.wrap(enc, contents.x));
		g.writeEndObject();

	}

	protected void formatForPublication(JsonGenerator g, EncodingHint enc,
			Writer writer) throws JsonGenerationException, IOException {

		g.writeObjectFieldStart(dsaKeys.getMetadata().getHandle()+"-P");
		g.writeStringField("KeyAlgorithm", "DSA");
		g.writeStringField("CreatedOn", TimeUtil.format(dsaKeys.metadata.createdOn));
		g.writeStringField("Encoding", enc.toString());
		g.writeStringField("Strength", String.valueOf(dsaKeys.metadata.lengthL));
		g.writeStringField("P", FormatUtil.wrap(enc, dsaKeys.p));
		g.writeStringField("Q", FormatUtil.wrap(enc, dsaKeys.q));
		g.writeStringField("G", FormatUtil.wrap(enc, dsaKeys.g));
		// do not print the private key part, X
		g.writeStringField("Y", FormatUtil.wrap(enc, dsaKeys.y));
		g.writeEndObject();

	}

	private String formatItem(EncodingHint enc, DSAKeyContents item) {
		StringWriter privateDataWriter = new StringWriter();
		JsonFactory f = new JsonFactory();
		JsonGenerator g = null;
		try {
			g = f.createGenerator(privateDataWriter);
			g.useDefaultPrettyPrinter();
			g.writeStartObject();
			g.writeObjectFieldStart(dsaKeys.metadata.getHandle()+"-U");
			g.writeStringField("KeyAlgorithm", "DSA");
			g.writeStringField("CreatedOn", TimeUtil.format(dsaKeys.metadata.createdOn));
			g.writeStringField("Encoding", enc.toString());
			g.writeStringField("CreatedOn", TimeUtil.format(dsaKeys.metadata.createdOn));
			g.writeStringField("Encoding", enc.toString());
			g.writeStringField("Strength", String.valueOf(dsaKeys.metadata.lengthL));
			g.writeStringField("P", FormatUtil.wrap(enc, dsaKeys.p));
			g.writeStringField("Q", FormatUtil.wrap(enc, dsaKeys.q));
			g.writeStringField("G", FormatUtil.wrap(enc, dsaKeys.g));
			g.writeStringField("Y", FormatUtil.wrap(enc, dsaKeys.y));
			DSAKeyContents contents = (DSAKeyContents) dsaKeys;
			g.writeStringField("X", FormatUtil.wrap(enc, contents.x));
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
