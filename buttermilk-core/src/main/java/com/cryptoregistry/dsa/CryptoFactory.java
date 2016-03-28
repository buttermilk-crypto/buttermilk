package com.cryptoregistry.dsa;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.locks.ReentrantLock;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.DSAKeyPairGenerator;
import org.bouncycastle.crypto.generators.DSAParametersGenerator;
import org.bouncycastle.crypto.params.DSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.DSAParameters;
import org.bouncycastle.crypto.params.DSAPrivateKeyParameters;
import org.bouncycastle.crypto.params.DSAPublicKeyParameters;

public class CryptoFactory {
	
	private final ReentrantLock lock;
	private final SecureRandom rand;

	public static final CryptoFactory INSTANCE = new CryptoFactory();

	private CryptoFactory() {
		lock = new ReentrantLock();
		try {
			rand = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	public DSAKeyContents generateKeys(char [] password) {
		lock.lock();
		try {
			
			DSAParametersGenerator  pGen = new DSAParametersGenerator();

			// about the best my laptop can handle
		    pGen.init(2048, 80, rand);

		    DSAParameters           params = pGen.generateParameters();
			
			DSAKeyPairGenerator kpGen = new DSAKeyPairGenerator();
			kpGen.init(new DSAKeyGenerationParameters(rand,params));
			AsymmetricCipherKeyPair pair = kpGen.generateKeyPair();
			DSAPrivateKeyParameters priv = (DSAPrivateKeyParameters) pair.getPrivate();
			DSAPublicKeyParameters pub = (DSAPublicKeyParameters) pair.getPublic();
			
			BigInteger p = params.getP();
			BigInteger q = params.getQ();
			BigInteger g = params.getG();
			
			BigInteger y = pub.getY();
			BigInteger x = priv.getX();
			
			DSAKeyMetadata meta = DSAKeyMetadata.createSecurePBKDF2(password);
			meta.setLengthL(2048);
			
			return new DSAKeyContents(meta,p,q,g,y,x);
			
		} finally {
			lock.unlock();
		}
		
	}
	
	public DSAKeyContents generateKeys(char [] password, int strength, int certainty) {
		lock.lock();
		try {
			
			DSAParametersGenerator  pGen = new DSAParametersGenerator();

			// about the best my laptop can handle
		    pGen.init(strength, certainty, rand);

		    DSAParameters           params = pGen.generateParameters();
			
			DSAKeyPairGenerator kpGen = new DSAKeyPairGenerator();
			kpGen.init(new DSAKeyGenerationParameters(rand,params));
			AsymmetricCipherKeyPair pair = kpGen.generateKeyPair();
			DSAPrivateKeyParameters priv = (DSAPrivateKeyParameters) pair.getPrivate();
			DSAPublicKeyParameters pub = (DSAPublicKeyParameters) pair.getPublic();
			
			BigInteger p = params.getP();
			BigInteger q = params.getQ();
			BigInteger g = params.getG();
			
			BigInteger y = pub.getY();
			BigInteger x = priv.getX();
			
			DSAKeyMetadata meta = DSAKeyMetadata.createSecurePBKDF2(password);
			meta.setLengthL(strength);
			
			return new DSAKeyContents(meta,p,q,g,y,x);
			
		} finally {
			lock.unlock();
		}
		
	}	

}
