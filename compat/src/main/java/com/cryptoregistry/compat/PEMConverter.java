package com.cryptoregistry.compat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.X509TrustedCertificateBlock;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;

import com.cryptoregistry.formats.JSONFormatter;
import com.cryptoregistry.passwords.ExistingPassword;
import com.cryptoregistry.passwords.NewPassword;

public class PEMConverter {

	final InputStream in;
	ExistingPassword password;
	public final JSONFormatter formatter;

	private String currentOID;
	private X9ECParameters currentECParams;

	public PEMConverter(InputStream in) {
		this.in = in;
		formatter = new JSONFormatter();
	}

	/**
	 * Optional, used with encrypted private key if we encounter that type
	 * 
	 * @param pass
	 */
	public void setPassword(ExistingPassword pass) {
		this.password = pass;
	}

	public void convert() {
		parsePemObjects();
	}

	private void parsePemObjects() {

		InputStreamReader inreader = new InputStreamReader(in);
		PEMParser parser = new PEMParser(inreader);

		try {

			Object obj = null;

			while ((obj = parser.readObject()) != null) {

				final String type = obj.getClass().getSimpleName();

				switch (type) {
				case "PKCS8EncryptedPrivateKeyInfo": {
					PKCS8EncryptedPrivateKeyInfo encinfo = (PKCS8EncryptedPrivateKeyInfo) obj;
					InputDecryptorProvider pkcs8Prov = new JceOpenSSLPKCS8DecryptorProviderBuilder()
							.setProvider("BC").build(password.getPassword());
					PrivateKeyInfo info = encinfo
							.decryptPrivateKeyInfo(pkcs8Prov);
					JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
					converter.setProvider("BC");
					PrivateKey key = converter.getPrivateKey(info);
					convertPrivateKeySecure(key, password.createNewPassword());
					break;
				}
				case "PrivateKeyInfo": {
					PrivateKeyInfo info = (PrivateKeyInfo) obj;
					JcaPEMKeyConverter converter = new JcaPEMKeyConverter()
							.setProvider("BC");
					PrivateKey privKey = converter.getPrivateKey(info);
					convertPrivateKey(privKey);
					break;
				}
				case "PEMEncryptedKeyPair": {
					// could be DSA, RSA, etc.
					PEMEncryptedKeyPair encpair = (PEMEncryptedKeyPair) obj;
					PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder()
							.build(password.getPassword());
					JcaPEMKeyConverter converter = new JcaPEMKeyConverter()
							.setProvider("BC");
					PEMKeyPair pair = encpair.decryptKeyPair(decProv);
					PrivateKeyInfo info = pair.getPrivateKeyInfo();
					SubjectPublicKeyInfo sinfo = pair.getPublicKeyInfo();
					PrivateKey key = converter.getPrivateKey(info);
					PublicKey pubKey = converter.getPublicKey(sinfo);
					this.convertKeyPairSecure(key, pubKey,
							password.createNewPassword());
					break;
				}
				case "PEMKeyPair": {
					PEMKeyPair pair = (PEMKeyPair) obj;
					JcaPEMKeyConverter converter = new JcaPEMKeyConverter()
							.setProvider("BC");
					PrivateKeyInfo info = pair.getPrivateKeyInfo();
					SubjectPublicKeyInfo sinfo = pair.getPublicKeyInfo();
					PrivateKey key = converter.getPrivateKey(info);
					PublicKey pubKey = converter.getPublicKey(sinfo);
					this.convertKeyPair(key, pubKey);
					break;
				}
				case "ASN1ObjectIdentifier": {
					ASN1ObjectIdentifier asnId = (ASN1ObjectIdentifier) obj;
					currentOID = asnId.getId();
					break;
				}
				case "X9ECParameters": {
					currentECParams = (X9ECParameters) obj;
					break;
				}
				case "ContentInfo": {
					ContentInfo info = (ContentInfo) obj;

					break;
				}
				case "X509TrustedCertificateBlock": {
					X509TrustedCertificateBlock block = (X509TrustedCertificateBlock) obj;
					X509CertificateHolder holder = block.getCertificateHolder();

					break;
				}
				default: {
					throw new RuntimeException("Unknown type: " + type);
				}
				} // end switch
			} // end while

			parser.close();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private void convertPrivateKey(PrivateKey key) {

	}

	private void convertPrivateKeySecure(PrivateKey key, NewPassword pass) {

	}

	private void convertKeyPair(PrivateKey key, PublicKey pubKey) {
		System.err.println("convertKeyPair: " + key.getAlgorithm());
	}

	private void convertKeyPairSecure(PrivateKey key, PublicKey pubKey, NewPassword pass) {

	}

}
