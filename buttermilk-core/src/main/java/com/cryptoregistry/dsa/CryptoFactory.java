/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith. All Rights Reserved.
 *
 */
package com.cryptoregistry.dsa;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.locks.ReentrantLock;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.generators.DSAKeyPairGenerator;
import org.bouncycastle.crypto.generators.DSAParametersGenerator;
import org.bouncycastle.crypto.params.DSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.DSAParameters;
import org.bouncycastle.crypto.params.DSAPrivateKeyParameters;
import org.bouncycastle.crypto.params.DSAPublicKeyParameters;
import org.bouncycastle.crypto.signers.DSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;

import com.cryptoregistry.SignatureAlgorithm;
import com.cryptoregistry.signature.DSACryptoSignature;
import com.cryptoregistry.signature.DSASignature;
import com.cryptoregistry.signature.SignatureMetadata;

/**
 * DSA implementation. The key generation methods can take a while.
 * 
 * @author Dave
 *
 */
public class CryptoFactory {

	private final ReentrantLock lock;
	private final SecureRandom rand;

	private final byte[] VALIDATE_SIGN_BYTES = "abc"
			.getBytes(StandardCharsets.UTF_8);

	public static final CryptoFactory INSTANCE = new CryptoFactory();

	private CryptoFactory() {
		lock = new ReentrantLock();
		try {
			rand = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public DSAKeyContents generateKeys(char[] password) {
		lock.lock();
		try {

			DSAParametersGenerator pGen = new DSAParametersGenerator();

			// about the best my laptop can handle
			pGen.init(2048, 80, rand);

			DSAParameters params = pGen.generateParameters();

			DSAKeyPairGenerator kpGen = new DSAKeyPairGenerator();
			kpGen.init(new DSAKeyGenerationParameters(rand, params));
			AsymmetricCipherKeyPair pair = kpGen.generateKeyPair();
			DSAPrivateKeyParameters priv = (DSAPrivateKeyParameters) pair
					.getPrivate();
			DSAPublicKeyParameters pub = (DSAPublicKeyParameters) pair
					.getPublic();

			BigInteger p = params.getP();
			BigInteger q = params.getQ();
			BigInteger g = params.getG();

			BigInteger y = pub.getY();
			BigInteger x = priv.getX();

			DSAKeyMetadata meta = DSAKeyMetadata.createSecurePBKDF2(password);
			meta.setLengthL(2048);

			return new DSAKeyContents(meta, p, q, g, y, x);

		} finally {
			lock.unlock();
		}

	}

	public DSAKeyContents generateKeys(char[] password, int strength,
			int certainty) {
		lock.lock();
		try {

			DSAParametersGenerator pGen = new DSAParametersGenerator();

			// about the best my laptop can handle
			pGen.init(strength, certainty, rand);

			DSAParameters params = pGen.generateParameters();

			DSAKeyPairGenerator kpGen = new DSAKeyPairGenerator();
			kpGen.init(new DSAKeyGenerationParameters(rand, params));
			AsymmetricCipherKeyPair pair = kpGen.generateKeyPair();
			DSAPrivateKeyParameters priv = (DSAPrivateKeyParameters) pair
					.getPrivate();
			DSAPublicKeyParameters pub = (DSAPublicKeyParameters) pair
					.getPublic();

			BigInteger p = params.getP();
			BigInteger q = params.getQ();
			BigInteger g = params.getG();

			BigInteger y = pub.getY();
			BigInteger x = priv.getX();

			DSAKeyMetadata meta = DSAKeyMetadata.createSecurePBKDF2(password);
			meta.setLengthL(strength);

			return new DSAKeyContents(meta, p, q, g, y, x);

		} finally {
			lock.unlock();
		}
	}

	/**
	 * Validate if key is complete and works as expected
	 * 
	 * @param contents
	 * @return
	 */
	public boolean isValidKeyPair(DSAKeyContents contents) {
		final DSASigner signer = new DSASigner();
		final DSAParameters params = new DSAParameters(contents.p, contents.q, contents.g);

		signer.init(true, new DSAPrivateKeyParameters(contents.x, params));
		final BigInteger[] sig = signer.generateSignature(VALIDATE_SIGN_BYTES);
		signer.init(false, new DSAPublicKeyParameters(contents.y, params));
		return signer.verifySignature(VALIDATE_SIGN_BYTES, sig[0], sig[1]);
	}
	
	/**
	 * Sign using DSA with a deterministic k value
	 * 
	 * @param signedBy - the handle of the registrant
	 * @param contents - the key handle
	 * @param digestNameUsedOnMsgBytes - a value like "SHA-256"
	 * @param msgHashBytes - the bytes of the digest on the message
	 * @return
	 */
	public DSACryptoSignature sign(String signedBy, 
			DSAKeyContents contents, 
			String digestNameUsedOnMsgBytes, 
			byte [] msgHashBytes) {
		
		// use a deterministic k to be more secure - see RFC 6979
		HMacDSAKCalculator calc = new HMacDSAKCalculator(new SHA1Digest());
		final DSASigner signer = new DSASigner(calc);
		final DSAParameters params = new DSAParameters(contents.p, contents.q, contents.g);

		signer.init(true, new DSAPrivateKeyParameters(contents.x, params));
		final BigInteger[] sigIntegers = signer.generateSignature(msgHashBytes);
		
		DSASignature sig = new DSASignature(sigIntegers[0].toByteArray(), sigIntegers[1].toByteArray());
		SignatureMetadata meta =  new SignatureMetadata(
				SignatureAlgorithm.DSA, 
				digestNameUsedOnMsgBytes, 
				contents.getMetadata().getHandle(), 
				signedBy);
		return new DSACryptoSignature(meta,sig);
		
	}
	
	/**
	 * Validate a signature
	 * 
	 * @param sig
	 * @param pKey
	 * @param msgHashBytes
	 * @return
	 */
	public boolean verify(DSACryptoSignature sig, DSAKeyForPublication pKey, byte [] msgHashBytes){
		
		final DSASigner signer = new DSASigner();
		  final DSAParameters params = new DSAParameters(
		    pKey.p,
		    pKey.q,
		    pKey.g);

		    signer.init(false, new DSAPublicKeyParameters(pKey.y, params));
		    BigInteger r = new BigInteger(sig.signature.r.decodeToBytes());
		    BigInteger s = new BigInteger(sig.signature.s.decodeToBytes());
		    
		    return signer.verifySignature(msgHashBytes, r, s);

	}

}
