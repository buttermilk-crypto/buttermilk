package com.cryptoregistry.ec;

import java.math.BigInteger;

import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

/**
 * Should work for any ECCurve, not just the ones Buttermilk currently knows about
 * 
 * @author Dave
 * @see ECPointSerializer
 *
 */
public class ECPointDeserializer {
	
	String curveName;
	String in;

	public ECPointDeserializer(String curveName, String in) {
		this.curveName = curveName;
		this.in = in;
		if(!in.contains(",")) throw new RuntimeException("Expecting format like String,String");
	}
	
	public ECPoint deserialize(String in) {
		String [] xy = in.split("\\,");
		BigInteger biX = new BigInteger(xy[0],16);
		BigInteger biY = new BigInteger(xy[1],16);
		ECCurve curve = CurveFactory.getCurveForName(curveName).getCurve();
		return curve.createPoint(biX, biY);
	}

}
