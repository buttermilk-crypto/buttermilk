/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2014 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.pbe;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * Algorithms we currently support for key derivation when encrypting key materials for Secure mode
 * 
 * @author Dave
 *
 */
public enum PBEAlg {
	PBKDF2,SCRYPT;
	
	public static synchronized ArmoredPBEResult loadFrom(Map<String,Object> keyData){
		
		String pbeAlg = (String) keyData.get("KeyData.PBEAlgorithm");
		PBEAlg algEnum = null;
		
		try {
			algEnum = PBEAlg.valueOf(pbeAlg);
		}catch(IllegalArgumentException x){
			throw new RuntimeException("Unknown Alg: "+pbeAlg);
		}
		
		switch(algEnum){
			case SCRYPT:{
			//	String encryptedData = (String) keyData.get("KeyData.EncryptedData");
				String encryptedData = collectListData(keyData);
				String salt = (String) keyData.get("KeyData.PBESalt");
				String iv = (String) keyData.get("KeyData.IV");
				int blockSize = Integer.parseInt((String) keyData.get("KeyData.BlockSize"));
				int cpuCost = Integer.parseInt((String) keyData.get("KeyData.CpuMemoryCost"));
				int para = Integer.parseInt((String) keyData.get("KeyData.Parallelization"));
				return new ArmoredScryptResult(encryptedData,salt,iv,cpuCost,blockSize,para);
			}
			case PBKDF2: {
			//	String encryptedData = (String) keyData.get("KeyData.EncryptedData");
				String encryptedData = collectListData(keyData);
				String salt = (String) keyData.get("KeyData.PBESalt");
				int iterations = Integer.parseInt((String) keyData.get("KeyData.Iterations"));
				return new ArmoredPBKDF2Result(encryptedData,salt,iterations);
			}
			default: {
				// should never get here, but satisfy compiler
				throw new RuntimeException("Sorry, don't recognize "+pbeAlg);
			}
		}
	}
	
	private static synchronized String collectListData(Map<String,Object> keyData){
		Object obj = keyData.get("KeyData.EncryptedData");
		if(obj instanceof String) return (String) obj;
		else if(obj instanceof List){
			StringWriter writer = new StringWriter();
			@SuppressWarnings("rawtypes")
			List list = (List) keyData.get("KeyData.EncryptedData"); 
			for(Object o: list){
				writer.write(String.valueOf(o));
			}
			return writer.toString();
		}
		return null;
	}
}
