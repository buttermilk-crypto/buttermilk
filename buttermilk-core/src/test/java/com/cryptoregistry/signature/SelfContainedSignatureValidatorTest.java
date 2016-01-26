package com.cryptoregistry.signature;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Assert;
import org.junit.Test;

import x.org.bouncycastle.crypto.digests.SHA1Digest;
import x.org.bouncycastle.crypto.digests.SHA256Digest;

import com.cryptoregistry.KeyMaterials;
import com.cryptoregistry.MapData;
import com.cryptoregistry.c2.key.Curve25519KeyContents;
import com.cryptoregistry.ec.ECKeyContents;
import com.cryptoregistry.formats.JSONFormatter;
import com.cryptoregistry.formats.JSONReader;
import com.cryptoregistry.rsa.RSAKeyContents;
import com.cryptoregistry.signature.builder.C2SignatureCollector;
import com.cryptoregistry.signature.builder.ECDSASignatureBuilder;
import com.cryptoregistry.signature.builder.MapDataContentsIterator;
import com.cryptoregistry.signature.builder.RSASignatureBuilder;
import com.cryptoregistry.signature.validator.SelfContainedSignatureValidator;

public class SelfContainedSignatureValidatorTest {

	@Test
	public void testRSA() {
		
		String signedBy = "Chinese Eyes"; // my registration handle
		String message = "My message text...";
		
		RSAKeyContents rKeys = com.cryptoregistry.rsa.CryptoFactory.INSTANCE.generateKeys();
		RSASignatureBuilder builder = new RSASignatureBuilder(signedBy,rKeys);
		builder.setDebugMode(true);
		MapData data = new MapData();
		data.put("Msg", message);
		MapDataContentsIterator iter = new MapDataContentsIterator(data);
		while(iter.hasNext()){
			String label = iter.next();
			builder.update(label, iter.get(label));
		}
		RSACryptoSignature sig = builder.build();
		JSONFormatter format = new JSONFormatter(signedBy);
		format.add(rKeys);
		format.add(data);
		format.add(sig);
		StringWriter writer = new StringWriter();
		format.format(writer);
		String serialized = writer.toString();
	//	System.err.println(serialized);
		
		// now validate the serialized text
		
		SelfContainedJSONResolver resolver = new SelfContainedJSONResolver(serialized);
		resolver.walk();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			resolver.resolve(sig.dataRefs,out);
			byte [] msgBytes = out.toByteArray();
			SHA256Digest digest = new SHA256Digest();
			digest.update(msgBytes, 0, msgBytes.length);
			byte [] m = new byte[digest.getDigestSize()];
			digest.doFinal(m, 0);
			// precheck
			boolean ok = com.cryptoregistry.rsa.CryptoFactory.INSTANCE.verify(sig, rKeys, m);
			Assert.assertTrue(ok);
			
			JSONReader reader = new JSONReader(new StringReader(serialized));
			KeyMaterials km = reader.parse();
			SelfContainedSignatureValidator validator = new SelfContainedSignatureValidator(km, true);
			Assert.assertTrue(validator.validate());
			
		} catch (RefNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testECDSA() {
		
		String signedBy = "Chinese Eyes"; // my registration handle
		String message = "My message text...";
		
		ECKeyContents ecKeys = com.cryptoregistry.ec.CryptoFactory.INSTANCE.generateKeys("P-256");
		ECDSASignatureBuilder builder = new ECDSASignatureBuilder(signedBy,ecKeys);
		builder.setDebugMode(true);
		MapData data = new MapData();
		data.put("Msg", message);
		MapDataContentsIterator iter = new MapDataContentsIterator(data);
		while(iter.hasNext()){
			String label = iter.next();
			builder.update(label, iter.get(label));
		}
		ECDSACryptoSignature sig = builder.build();
		JSONFormatter format = new JSONFormatter(signedBy);
		format.add(ecKeys);
		format.add(data);
		format.add(sig);
		StringWriter writer = new StringWriter();
		format.format(writer);
		String serialized = writer.toString();
	//	System.err.println(serialized);
		
		// now validate the serialized text
		
		SelfContainedJSONResolver resolver = new SelfContainedJSONResolver(serialized);
		resolver.walk();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			resolver.resolve(sig.dataRefs,out);
			byte [] msgBytes = out.toByteArray();
			SHA1Digest digest = new SHA1Digest();
			digest.update(msgBytes, 0, msgBytes.length);
			byte [] m = new byte[digest.getDigestSize()];
			digest.doFinal(m, 0);
			// precheck
			boolean ok = com.cryptoregistry.ec.CryptoFactory.INSTANCE.verify(sig, ecKeys, m);
			Assert.assertTrue(ok);
			
			JSONReader reader = new JSONReader(new StringReader(serialized));
			KeyMaterials km = reader.parse();
			SelfContainedSignatureValidator validator = new SelfContainedSignatureValidator(km, true);
			Assert.assertTrue(validator.validate());
			
		} catch (RefNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testCurve25519() {
		
		String signedBy = "Chinese Eyes"; // my registration handle
		String message = "My message text...";
		
		Curve25519KeyContents cKeys = com.cryptoregistry.c2.CryptoFactory.INSTANCE.generateKeys();
		
	// wrong one, must use collector with C2 because it does it's own digest as part of the implementation
	//	C2SignatureBuilder builder = new C2SignatureBuilder(signedBy,cKeys);
		C2SignatureCollector builder = new C2SignatureCollector(signedBy,cKeys);
		builder.setDebugMode(true);
		MapData data = new MapData();
		data.put("Msg", message);
		MapDataContentsIterator iter = new MapDataContentsIterator(data);
		while(iter.hasNext()){
			String label = iter.next();
			builder.collect(label, iter.get(label));
		}
		C2CryptoSignature sig = builder.build();
		JSONFormatter format = new JSONFormatter(signedBy);
		format.add(cKeys);
		format.add(data);
		format.add(sig);
		StringWriter writer = new StringWriter();
		format.format(writer);
		String serialized = writer.toString();
		System.err.println(serialized);
		
		// now validate the serialized text
		
		SelfContainedJSONResolver resolver = new SelfContainedJSONResolver(serialized);
		resolver.walk();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			resolver.resolve(sig.dataRefs,out);
			byte [] msgBytes = out.toByteArray();
		//	SHA256Digest digest = new SHA256Digest();
		//	digest.update(msgBytes, 0, msgBytes.length);
		//	byte [] m = new byte[digest.getDigestSize()];
		//	digest.doFinal(m, 0);
			
			// precheck
			boolean ok = com.cryptoregistry.c2.CryptoFactory.INSTANCE.verify(cKeys, msgBytes, sig.getSignature(), new SHA256Digest());
			Assert.assertTrue(ok);
			
			JSONReader reader = new JSONReader(new StringReader(serialized));
			KeyMaterials km = reader.parse();
			SelfContainedSignatureValidator validator = new SelfContainedSignatureValidator(km, true);
	    	Assert.assertTrue(validator.validate());
			
		} catch (RefNotFoundException e) {
			e.printStackTrace();
		}
	}

}
