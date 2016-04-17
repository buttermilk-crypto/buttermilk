/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith. All Rights Reserved.
 *
 */
package com.cryptoregistry;

/**
 * These are tokens for the signature algorithms Buttermilk knows how to handle. 
 * 
 * @author Dave
 *
 */
public enum SignatureAlgorithm {
	DSA, ECDSA, ECKCDSA, RSA, Rainbow;
}
