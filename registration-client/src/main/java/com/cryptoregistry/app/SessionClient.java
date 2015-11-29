/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2015 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.apache.http.HttpVersion;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;

import com.cryptoregistry.CryptoKey;
import com.cryptoregistry.CryptoKeyWrapper;
import com.cryptoregistry.KeyMaterials;
import com.cryptoregistry.c2.key.Curve25519KeyContents;
import com.cryptoregistry.ec.ECKeyContents;
import com.cryptoregistry.formats.JSONFormatter;
import com.cryptoregistry.formats.JSONReader;
import com.cryptoregistry.passwords.Password;
import com.cryptoregistry.rsa.CryptoFactory;
import com.cryptoregistry.rsa.RSAKeyContents;
import com.cryptoregistry.rsa.RSAKeyForPublication;
import com.cryptoregistry.signature.C2CryptoSignature;
import com.cryptoregistry.signature.ECDSACryptoSignature;
import com.cryptoregistry.signature.RSACryptoSignature;
import com.cryptoregistry.signature.builder.C2SignatureCollector;
import com.cryptoregistry.signature.builder.ECDSASignatureBuilder;
import com.cryptoregistry.signature.builder.RSAKeyContentsIterator;
import com.cryptoregistry.signature.builder.RSASignatureBuilder;

import asia.redact.bracket.properties.Properties;

/**
 * Call the session interface. First we generate an RSA ephemeral key. We sign
 * it using our registration key, so CR knows it is us. We send it and get back
 * as a response a local data with a token encrypted by our RSA key, plus a 
 * signature made by a CR-provable key.
 * 
 * The token is then decrypted, stored in memory, and used as an HMAC key on our further requests.
 * 
 * @author Dave
 *
 */

public class SessionClient {

	Properties props;
	String keyPath;
	String regHandle;
	CryptoKey registrationKey;
	RSAKeyContents rsaKey;
	SecureRandom rand;
	String sessionReq;
	CloseableHttpClient httpclient;

	/**
	 * keyPath will be a directory where our confidential key (and possibly
	 * password file) are located).
	 * 
	 * @param props
	 * @param keyPath
	 */
	public SessionClient(Properties props, String keyPath) {
		this.props = props;
		this.keyPath = keyPath;
		try {
			rand = SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		loadKey(); // loads registration key from file using keyPath to locate
		createEphemeralKey();
		sessionReq = createSignature();
		System.err.println("SessionReq: \n"+sessionReq);
	}

	private void createEphemeralKey() {
		rsaKey = CryptoFactory.INSTANCE.generateKeys(2048);
	}

	private String createSignature() {

		JSONFormatter requestFormatter = new JSONFormatter(regHandle);
		RSAKeyForPublication ephemeralPub = (RSAKeyForPublication) rsaKey.keyForPublication();
		requestFormatter.add(ephemeralPub);
		requestFormatter.add(registrationKey.keyForPublication());
		
		switch (registrationKey.getMetadata().getKeyAlgorithm()) {
		case Curve25519: {
			//Curve25519KeyForPublication pub = (Curve25519KeyForPublication) registrationKey.keyForPublication();
			C2SignatureCollector sigBuilder = new C2SignatureCollector(
					regHandle, (Curve25519KeyContents) registrationKey);
			RSAKeyContentsIterator iter = new RSAKeyContentsIterator(ephemeralPub);
			// key contents
			while (iter.hasNext()) {
				String label = iter.next();
				sigBuilder.collect(label, iter.get(label));
			}
		//	requestFormatter.add(pub);

			C2CryptoSignature sig = sigBuilder.build();
			requestFormatter.add(sig);

			break;
		}
		case EC: {
			//ECKeyForPublication pub = (ECKeyForPublication) registrationKey.keyForPublication();
			ECDSASignatureBuilder sigBuilder = new ECDSASignatureBuilder(regHandle, (ECKeyContents) registrationKey);
			RSAKeyContentsIterator iter = new RSAKeyContentsIterator(ephemeralPub);
			// key contents
			while (iter.hasNext()) {
				String label = iter.next();
				sigBuilder.update(label, iter.get(label));
			}
		//	requestFormatter.add(pub);

			ECDSACryptoSignature sig = sigBuilder.build();
			requestFormatter.add(sig);

			break;
		}
		case RSA: {
			//RSAKeyForPublication pub = (RSAKeyForPublication) registrationKey.keyForPublication();
			RSASignatureBuilder sigBuilder = new RSASignatureBuilder(regHandle, (RSAKeyContents) registrationKey);
			RSAKeyContentsIterator iter = new RSAKeyContentsIterator(ephemeralPub);
			// key contents
			while (iter.hasNext()) {
				String label = iter.next();
				String value = iter.get(label);
				sigBuilder.update(label, value);
			}
		//	requestFormatter.add(pub);
			RSACryptoSignature sig = sigBuilder.build();
			requestFormatter.add(sig);
			break;
		}
		default: {
			throw new RuntimeException("Not a signature algorithm");
		}
		}
		
		StringWriter writer = new StringWriter();
		requestFormatter.format(writer);
		return writer.toString();

	}

	private void loadKey() {
		File passwordProperties = new File(keyPath, "password.properties");
		File secureKey = new File(keyPath, "secureKey.json.txt");
		if (!passwordProperties.exists()) {
			// must get password from user
			System.err
					.println("TODO - Sorry, not able to ask for password - and no password file found...");
			return;
		}
		if (!secureKey.exists()) {
			System.err.println("No key found here...");
			return;
		}

		CryptoKeyWrapper wrapper = null;

		try {
			Properties pass = Properties.Factory
					.getInstance(new FileInputStream(passwordProperties));
			char[] password = pass.deobfuscateToChar("password");
			JSONReader reader = new JSONReader(secureKey);
			KeyMaterials km = reader.parse();
			regHandle = km.regHandle();
			wrapper = (CryptoKeyWrapper) km.keys().get(0);
			boolean ok = wrapper.unlock(new Password(password));
			if (!ok) {
				System.err.println("Looks like password failed...bailing out.");
				return;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		registrationKey = wrapper.getKeyContents();

	}

	public String request() {
		try {
			  URIBuilder builder = new URIBuilder();
			   builder.setScheme(props.get("registration.session.scheme"))
		        .setHost(props.get("registration.session.hostname"))
		        .setPath(props.get("registration.session.path"))
		        .setPort(props.intValue("registration.session.port"));
			   String url = builder.build().toString();
				byte [] res = Request.Post(url)
				        .useExpectContinue()
				        .version(HttpVersion.HTTP_1_1)
				        .bodyString(sessionReq, ContentType.APPLICATION_JSON)
				        .execute().returnContent().asBytes();
				return new String(res, "UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
			}
		return null;
	}

}
