package com.cryptoregistry.compat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import junit.framework.Assert;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPrivateCrtKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

import com.cryptoregistry.formats.JSONFormatter;
import com.cryptoregistry.formats.KeyFormat;
import com.cryptoregistry.rsa.RSAKeyContents;
import com.cryptoregistry.rsa.RSAKeyForPublication;
import com.cryptoregistry.rsa.RSAKeyMetadata;

public class TestCertMethods {

	@Test
	public void test0() {

		 if (Security.getProvider("BC") == null) {
	            Security.addProvider(new BouncyCastleProvider());
	       }

		try (InputStream inStream = this.getClass().getResourceAsStream(
				"/cryptoregistry.com-selfsigned.cer");
			) {
			X509Certificate cert = X509Certificate.getInstance(inStream);
			inStream.close();
			Assert.assertNotNull(cert);

			RSAPublicKey key = (RSAPublicKey) cert.getPublicKey();
			System.err.println(key.getClass());
			BigInteger modulus = key.getModulus();
			BigInteger publicExponent = key.getPublicExponent();
			int strength = key.getModulus().bitLength();

			byte[] subjectDN = cert.getSubjectDN().getName().toLowerCase()
					.getBytes(StandardCharsets.UTF_8);
			byte[] issuerDN = cert.getIssuerDN().getName().toLowerCase()
					.getBytes(StandardCharsets.UTF_8);
			byte[] serial = cert.getSerialNumber().toByteArray();
			Date notBefore = cert.getNotBefore();

			// make a digest of subjectDN, issuerDN, and serial number in that
			// order. Lower case the strings
			SHA1Digest digest = new SHA1Digest();
			digest.update(subjectDN, 0, subjectDN.length);
			digest.update(issuerDN, 0, issuerDN.length);
			digest.update(subjectDN, 0, subjectDN.length);
			byte[] result = new byte[digest.getDigestSize()];
			digest.doFinal(result, 0);

			// the generated handle should be unique enough and algorithm to create it is
			// intended to produce a stable, repeatable value. Hex encoding here...
			RSAKeyMetadata meta = new RSAKeyMetadata(Hex.toHexString(result),
					notBefore, KeyFormat.forPublication());
			meta.strength = strength;

			RSAKeyForPublication pub = new RSAKeyForPublication(meta, modulus, publicExponent);
		
			String regHandle = null;
			try {
				LdapName ln = new LdapName(cert.getSubjectDN().getName());
				for (Rdn rdn : ln.getRdns()) {
					if (rdn.getType().equalsIgnoreCase("CN")) {
						regHandle = String.valueOf(rdn.getValue());
						break;
					}
				}
			} catch (Exception x) {
				x.printStackTrace();
			}
			
			RSAPrivateKey privateKey = null;
			try (InputStream instream = this.getClass().getResourceAsStream(
					"/cryptoregistry.com-selfsigned.key");
			) {
				String in = readFully(instream, "UTF-8");
				privateKey =  getKeyFromString(in);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			RSAKeyContents contents = null;
			RSAKeyContents confidential = null;
			RSAKeyMetadata metaOpen = new RSAKeyMetadata(meta.handle,meta.createdOn,KeyFormat.unsecured(),meta.strength);
			char [] passwordChars = {'p','a','s','s'};
			RSAKeyMetadata metaConfidential = 
					new RSAKeyMetadata(meta.handle,meta.createdOn,KeyFormat.securedPBKDF2(passwordChars),meta.strength);
			
			// because this is going through the provider. Yuck.
			
			if(privateKey instanceof BCRSAPrivateCrtKey){
				BCRSAPrivateCrtKey crtKey = (BCRSAPrivateCrtKey) privateKey;
				contents = new RSAKeyContents(
						metaOpen, 
						crtKey.getModulus(),
						crtKey.getPublicExponent(), 
						crtKey.getPrivateExponent(),
						crtKey.getPrimeP(),
						crtKey.getPrimeQ(),
						crtKey.getPrimeExponentP(),
						crtKey.getPrimeExponentQ(),
						crtKey.getCrtCoefficient()
				);
				confidential = new RSAKeyContents(
						metaConfidential, 
						crtKey.getModulus(),
						crtKey.getPublicExponent(), 
						crtKey.getPrivateExponent(),
						crtKey.getPrimeP(),
						crtKey.getPrimeQ(),
						crtKey.getPrimeExponentP(),
						crtKey.getPrimeExponentQ(),
						crtKey.getCrtCoefficient()
				);
			}

			JSONFormatter formatter = new JSONFormatter(regHandle);
			formatter.add(pub);
			formatter.add(contents);
			formatter.add(confidential);
			StringWriter writer = new StringWriter();
			formatter.format(writer, true);
			System.err.println(writer.toString());

		} catch (CertificateException | IOException e) {
			e.printStackTrace();
		}

	}

	public String readFully(InputStream inputStream, String encoding)
			throws IOException {
		return new String(readFully(inputStream), encoding);
	}

	private byte[] readFully(InputStream inputStream) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length = 0;
		while ((length = inputStream.read(buffer)) != -1) {
			baos.write(buffer, 0, length);
		}
		return baos.toByteArray();
	}

	public RSAPrivateCrtKey getKeyFromString(String key)
			throws Exception {
		StringReader reader = new StringReader(key);
		PEMParser pp = new PEMParser(reader);
		Object obj =  pp.readObject();
		pp.close();
		if(obj instanceof PrivateKeyInfo){
			PrivateKeyInfo info = (PrivateKeyInfo) obj;
			JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
			return (RSAPrivateCrtKey)converter.getPrivateKey(PrivateKeyInfo.getInstance(info));
		}else{
			return null;
		}
		
	}

}
