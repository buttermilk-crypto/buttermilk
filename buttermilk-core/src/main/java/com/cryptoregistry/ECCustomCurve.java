/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith. All Rights Reserved.
 *
 */
package com.cryptoregistry;

import org.bouncycastle.crypto.params.ECDomainParameters;

/**
 * Classes which implement this interface can function as custom curve definitions (as opposed
 * to named curves, which have pre-defined parameters) for Elliptic Curve cryptography
 * 
 * @author Dave
 * @see ECCustomParameters
 * 
 */
public interface ECCustomCurve {
	public ECDomainParameters getParameters();
}
