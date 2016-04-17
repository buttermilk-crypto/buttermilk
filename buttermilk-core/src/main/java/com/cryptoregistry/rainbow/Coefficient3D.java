package com.cryptoregistry.rainbow;

import com.cryptoregistry.util.ArrayUtil;

public class Coefficient3D {

	private final short[][][] coeff;

	public Coefficient3D(short[][][] coeff) {
		super();
		this.coeff = coeff;
	}

	public short[][][] getCoeff() {
		return coeff;
	}
	  
	public String encode() {
		return ArrayUtil.encode3dShort(coeff);
	}
	
}
