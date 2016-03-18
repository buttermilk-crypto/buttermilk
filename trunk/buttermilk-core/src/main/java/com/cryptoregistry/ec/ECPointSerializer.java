package com.cryptoregistry.ec;

import java.math.BigInteger;

import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

/**
 * Our approach to serializing an ECPoint
 * 
 * @author Dave
 *
 */
public class ECPointSerializer {

	ECPoint point;

	public ECPointSerializer(ECPoint point) {
		this.point = point;
	}

	public String serialize() {

		StringBuffer sb = new StringBuffer();
		sb.append(point.getAffineXCoord());
		sb.append(',');
		sb.append(point.getAffineXCoord());

		return sb.toString();
	}

}
