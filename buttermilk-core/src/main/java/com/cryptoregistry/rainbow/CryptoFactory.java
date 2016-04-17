package com.cryptoregistry.rainbow;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.locks.ReentrantLock;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.digests.SHA224Digest;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.pqc.crypto.DigestingMessageSigner;
import org.bouncycastle.pqc.crypto.rainbow.RainbowKeyGenerationParameters;
import org.bouncycastle.pqc.crypto.rainbow.RainbowKeyPairGenerator;
import org.bouncycastle.pqc.crypto.rainbow.RainbowParameters;
import org.bouncycastle.pqc.crypto.rainbow.RainbowPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.rainbow.RainbowPublicKeyParameters;
import org.bouncycastle.pqc.crypto.rainbow.RainbowSigner;

import com.cryptoregistry.SignatureAlgorithm;
import com.cryptoregistry.signature.RainbowCryptoSignature;
import com.cryptoregistry.signature.RainbowSignature;
import com.cryptoregistry.signature.SignatureMetadata;

public class CryptoFactory {

	private static SecureRandom rand;
	private final ReentrantLock lock;

	public static final CryptoFactory INSTANCE = new CryptoFactory();

	private CryptoFactory() {
		lock = new ReentrantLock();
		try {
			rand = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	public RainbowKeyContents generateKeys() {
		try {
		lock.lock();
		RainbowParameters params = new RainbowParameters();

		RainbowKeyPairGenerator rainbowKeyGen = new RainbowKeyPairGenerator();
		
		// not sure if this is the correct way to use rand
		RainbowKeyGenerationParameters genParam = new RainbowKeyGenerationParameters(rand, params);

		rainbowKeyGen.init(genParam);

		AsymmetricCipherKeyPair pair = rainbowKeyGen.generateKeyPair();
		RainbowPrivateKeyParameters privKey = (RainbowPrivateKeyParameters) pair.getPrivate();
		RainbowPublicKeyParameters pubKey = (RainbowPublicKeyParameters) pair.getPublic();
		return new RainbowKeyContents(pubKey,privKey);
		}finally{
			lock.unlock();
		}
	}
	
	public RainbowCryptoSignature sign(String signedBy, RainbowKeyContents contents, byte [] msgDigest){
		
		ParametersWithRandom paramWithRandom = new ParametersWithRandom(
				contents.getPrivateKey(), rand);

		SHA224Digest digest = new SHA224Digest();
		DigestingMessageSigner rainbowSigner = new DigestingMessageSigner(
				new RainbowSigner(), digest);
		rainbowSigner.init(true, paramWithRandom);

		rainbowSigner.update(msgDigest, 0, msgDigest.length);
		byte[] sigBytes = rainbowSigner.generateSignature();
		RainbowSignature sig = new RainbowSignature(sigBytes);
		SignatureMetadata meta = new SignatureMetadata(
				SignatureAlgorithm.Rainbow,
				digest.getAlgorithmName(),
				contents.getMetadata().getHandle(),
				signedBy);
		return new RainbowCryptoSignature(meta, sig);
	}
	
	public boolean verify(RainbowCryptoSignature sig, RainbowKeyForPublication verifierKey, byte [] msgDigest){
		
		return false;
	}
}
