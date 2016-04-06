package com.cryptoregistry.compat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;

import javax.crypto.Cipher;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ParseBCDataPEMFiles {
	
	@BeforeClass
	public static void prep() {
		// dependency
		if(Security.getProvider("BC") == null) 
			Security.addProvider(new BouncyCastleProvider());
		
		// dependency
		try {
			if(Integer.MAX_VALUE  == Cipher.getMaxAllowedKeyLength("AES")) {
				System.err.println("Unlimited Policy jar is installed");
			}else{
				System.err.println("Unlimited Policy jar is not installed, exiting");
			}
			
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
	}
	
	@Test
	public void testDER() {
		// TODO
	}
	
	@Test
	public void testPEMConverter() {
		
		String resourcePath = "/fm4dd/570-ec-sect571r1-keypair.pem";
		try (InputStream in = this.getClass().getResourceAsStream(resourcePath); ) {

			Assert.assertTrue(in != null);
			
			PEMConverter converter = new PEMConverter(in);
			converter.convert();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@Test
	public void testPEM() {
		
		// NOTE - this code requires the "unlimited strength policy.jar" rubbish associated with the JCA
		
		// get encrypted private key
		char [] passwd = "changeit".toCharArray();
		Object obj = null;
		String type = null;
		
		try {
			obj = parse("/data/pkcs8/openssl_pkcs8_rsa_enc.pem");
			type = parsePemObjectType("/data/pkcs8/openssl_pkcs8_rsa_enc.pem");
			System.err.println(type+", "+obj.getClass().getName());
			
			if(PKCS8EncryptedPrivateKeyInfo.class == obj.getClass()){
				PKCS8EncryptedPrivateKeyInfo priv = (PKCS8EncryptedPrivateKeyInfo) obj;
				InputDecryptorProvider pkcs8Prov = 
					new JceOpenSSLPKCS8DecryptorProviderBuilder().setProvider("BC").build(passwd);
				PrivateKeyInfo info = priv.decryptPrivateKeyInfo(pkcs8Prov);
			//	System.err.println("successfully decrypted, got "+info.getClass().getName());
				JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
				converter.setProvider("BC");
				PrivateKey key = converter.getPrivateKey(info);
			//	System.err.println(key.getAlgorithm()+", "+key);
			}
			
		} catch (PKCSException e) {
			e.printStackTrace();
		} catch (OperatorCreationException e) {
			e.printStackTrace();
		} catch (PEMException e) {
			e.printStackTrace();
		}
		
		
		obj = parse("/data/pkcs8/openssl_pkcs8_rsa.pem");
		type = parsePemObjectType("/data/pkcs8/openssl_pkcs8_rsa.pem");
		System.err.println(type+", "+obj.getClass().getName());
		
		
		//dsa
		obj = parse("/data/dsa/openssl_dsa_aes128_cbc.pem");
		type = parsePemObjectType("/data/dsa/openssl_dsa_aes128_cbc.pem");
		System.err.println(type+", "+obj.getClass().getName());
		
		obj = parse("/data/dsa/openssl_dsa_unencrypted.pem");
		type = parsePemObjectType("/data/dsa/openssl_dsa_unencrypted.pem");
		System.err.println(type+", "+obj.getClass().getName());
		
		
		obj = parse("/data/rsa/openssl_rsa_aes128_cbc.pem");
		type = parsePemObjectType("/data/rsa/openssl_rsa_aes128_cbc.pem");
		System.err.println(type+", "+obj.getClass().getName());
		
		
		obj = parse("/data/rsa/openssl_rsa_unencrypted.pem");
		type = parsePemObjectType("/data/rsa/openssl_rsa_unencrypted.pem");
		System.err.println(type+", "+obj.getClass().getName());
		
		obj = parse("/eckey.pem");
		type = parsePemObjectType("/eckey.pem");
		System.err.println(type+", "+obj.getClass().getName());
		System.err.println(type+" "+obj);
		
		obj = parse("/ecexpparam.pem");
		type = parsePemObjectType("/ecexpparam.pem");
		System.err.println(type+", "+obj.getClass().getName());
		
		obj = parse("/enckey.pem");
		type = parsePemObjectType("/enckey.pem");
		System.err.println(type+", "+obj.getClass().getName());
		
		obj = parse("/pkcs7.pem");
		type = parsePemObjectType("/pkcs7.pem");
		System.err.println(type+", "+obj.getClass().getName());
		
		
		obj = parse("/pkcs8test.pem");
		type = parsePemObjectType("/pkcs8test.pem");
		System.err.println(type+", "+obj.getClass().getName());
		
		obj = parse("/smimenopw.pem");
		type = parsePemObjectType("/smimenopw.pem");
		System.err.println(type+", "+obj.getClass().getName());
		
		obj = parse("/test.pem");
		type = parsePemObjectType("/test.pem");
		System.err.println(type+", "+obj.getClass().getName());
		
		obj = parse("/trusted_cert.pem");
		type = parsePemObjectType("/trusted_cert.pem");
		System.err.println(type+", "+obj.getClass().getName());
		
		obj = parse("/fm4dd/570-ec-sect571r1-keypair.pem");
		type = parsePemObjectType("/fm4dd/570-ec-sect571r1-keypair.pem");
		System.err.println(type+", "+obj.getClass().getName());
		
		
	}
	
    private String parsePemObjectType(String resourcePath){
		
		String obj = null;
		
		try (InputStream in = this.getClass().getResourceAsStream(
				resourcePath);
				InputStreamReader inreader = new InputStreamReader(in);
		) {

			PEMParser parser = new PEMParser(inreader);
			obj = parser.readPemObject().getType();
			parser.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return obj;
	}
	
	private Object parse(String resourcePath){
		
		Object obj = null;
		
		try (InputStream in = this.getClass().getResourceAsStream(
				resourcePath);
				InputStreamReader inreader = new InputStreamReader(in);
		) {

			PEMParser parser = new PEMParser(inreader);
			obj = parser.readObject();
			parser.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return obj;
	}

}
