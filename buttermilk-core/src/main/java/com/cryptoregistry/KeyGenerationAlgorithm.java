/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith. All Rights Reserved.
 *
 */
package com.cryptoregistry;

/**
 * These are names and codes for the types of cryptographic keys Buttermilk knows how to generate
 * 
 * @author Dave
 *
 */
public enum KeyGenerationAlgorithm {
	Symmetric, 
	Curve25519, 
	EC, 
	RSA, 
	DSA, 
	JNEO,
	Rainbow;

	private KeyGenerationAlgorithm() {
		
	}
	
	public static KeyGenerationAlgorithm [] usableForSignature(){
		KeyGenerationAlgorithm [] a = new KeyGenerationAlgorithm[4];
		a[0]=Curve25519;
		a[1]=EC;
		a[2]=RSA;
		a[3]=Rainbow;
		return a;
	}
	
	public static boolean isUsableForSignature(KeyGenerationAlgorithm alg){
		for(KeyGenerationAlgorithm a: usableForSignature()){
			if(alg==a) return true;
		}
		return false;
	}
	
	public static boolean isRSA(KeyGenerationAlgorithm alg) {
		return alg == RSA ? true : false;
	}
	
	public static boolean isEC(KeyGenerationAlgorithm alg) {
		return alg == EC ? true : false;
	}
	
	public static boolean isCurve25519(KeyGenerationAlgorithm alg) {
		return alg == Curve25519 ? true : false;
	}
	
}
