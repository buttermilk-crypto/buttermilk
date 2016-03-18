/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2014 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.ec;

import java.math.BigInteger;

import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.math.ec.ECConstants;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

public class Secp160k1 extends ECParametersHolderBase {

	private static final String NAME = "secp160k1";

	private Secp160k1(ECDomainParameters params) {
		super(params);
	}

	static Secp160k1 instance() {
		ECDomainParameters p = init(NAME);
		return new Secp160k1(p);
	}

	private static ECDomainParameters init(String name) {
		 // p = 2^160 - 2^32 - 2^14 - 2^12 - 2^9 - 2^8 - 2^7 - 2^3 - 2^2 - 1
        BigInteger p = fromHex("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFAC73");
        BigInteger a = ECConstants.ZERO;
        BigInteger b = BigInteger.valueOf(7);
        byte[] S = null;
        BigInteger n = fromHex("0100000000000000000001B8FA16DFAB9ACA16B6B3");
        BigInteger h = BigInteger.valueOf(1);

        ECCurve curve = new ECCurve.Fp(p, a, b);
        ECPoint G = curve.decodePoint(Hex.decode("04"
            + "3B4C382CE37AA192A4019E763036F4F5DD4D7EBB"
            + "938CF935318FDCED6BC28286531733C3F03C4FEE"));

		return new NamedECDomainParameters(curve, G, n, h, S, name);
	}

	@Override
	public ECDomainParameters createParameters() {
		return cached;
	}

}
