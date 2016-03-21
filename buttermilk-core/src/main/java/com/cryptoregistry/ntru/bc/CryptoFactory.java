/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2014 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.ntru.bc;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.pqc.crypto.ntru.NTRUEncryptionKeyGenerationParameters;
import org.bouncycastle.pqc.crypto.ntru.NTRUEncryptionKeyPairGenerator;
import org.bouncycastle.pqc.crypto.ntru.NTRUEncryptionPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.ntru.NTRUEncryptionPublicKeyParameters;
import org.bouncycastle.pqc.crypto.ntru.NTRUEngine;

public class CryptoFactory {
	
	public static final CryptoFactory INSTANCE = new CryptoFactory();

	private CryptoFactory() {}
	
	/**
	 * A reasonable default choice for parameters
	 */
	public NTRUKeyContents generateKeys() {
		NTRUEncryptionKeyPairGenerator gen = new NTRUEncryptionKeyPairGenerator();
		gen.init(NTRUEncryptionKeyGenerationParameters.EES1087EP2);
		AsymmetricCipherKeyPair pair = gen.generateKeyPair();
		NTRUEncryptionPublicKeyParameters pub  = (NTRUEncryptionPublicKeyParameters) pair.getPublic();
		NTRUEncryptionPrivateKeyParameters priv =  (NTRUEncryptionPrivateKeyParameters) pair.getPrivate();
		return new NTRUKeyContents(NTRUNamedParameters.EES1087EP2,pub.h,priv.t,priv.fp);
	}
	
	/**
	 * Similar in concept to EC well-defined CurveNames
	 * @param paramName
	 * @return
	 */
	public NTRUKeyContents generateKeys(NTRUNamedParameters paramName) {
		NTRUEncryptionKeyPairGenerator gen = new NTRUEncryptionKeyPairGenerator();
		gen.init(paramName.getKeyGenerationParameters());
		AsymmetricCipherKeyPair pair = gen.generateKeyPair();
		NTRUEncryptionPublicKeyParameters pub  = (NTRUEncryptionPublicKeyParameters) pair.getPublic();
		NTRUEncryptionPrivateKeyParameters priv =  (NTRUEncryptionPrivateKeyParameters) pair.getPrivate();
		return new NTRUKeyContents(paramName,pub.h,priv.t,priv.fp);
	}
	
	public NTRUKeyContents generateKeys(NTRUKeyMetadata metadata) {
		NTRUEncryptionKeyPairGenerator gen = new NTRUEncryptionKeyPairGenerator();
		gen.init(NTRUEncryptionKeyGenerationParameters.EES1087EP2);
		AsymmetricCipherKeyPair pair = gen.generateKeyPair();
		NTRUEncryptionPublicKeyParameters pub  = (NTRUEncryptionPublicKeyParameters) pair.getPublic();
		NTRUEncryptionPrivateKeyParameters priv =  (NTRUEncryptionPrivateKeyParameters) pair.getPrivate();
		return new NTRUKeyContents(metadata,NTRUNamedParameters.EES1087EP2,pub.h,priv.t,priv.fp);
	}
	
	public NTRUKeyContents generateKeys(NTRUKeyMetadata metadata, NTRUNamedParameters e) {
		NTRUEncryptionKeyPairGenerator gen = new NTRUEncryptionKeyPairGenerator();
		gen.init(e.getKeyGenerationParameters());
		AsymmetricCipherKeyPair pair = gen.generateKeyPair();
		NTRUEncryptionPublicKeyParameters pub  = (NTRUEncryptionPublicKeyParameters) pair.getPublic();
		NTRUEncryptionPrivateKeyParameters priv =  (NTRUEncryptionPrivateKeyParameters) pair.getPrivate();
		return new NTRUKeyContents(metadata,e,pub.h,priv.t,priv.fp);
	}
	
	public byte [] encrypt(NTRUKeyForPublication pKey, byte [] in){
		
		NTRUEngine engine = new NTRUEngine();
		engine.init(true, pKey.getPublicKey());
		if(in.length > pKey.parameterSetName.getParameters().maxMsgLenBytes)
			throw new RuntimeException("Max size we can work with = "+ pKey.parameterSetName.getParameters().maxMsgLenBytes);
	    try {
			return engine.processBlock(in, 0, in.length);
		} catch (InvalidCipherTextException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public byte [] decrypt(NTRUKeyContents sKey, byte [] encrypted){
		
		NTRUEngine engine = new NTRUEngine();
		engine.init(false, sKey.getPrivateKey());
	    try {
			return engine.processBlock(encrypted, 0, encrypted.length);
		} catch (InvalidCipherTextException e) {
			throw new RuntimeException(e);
		}
		
	}

}
