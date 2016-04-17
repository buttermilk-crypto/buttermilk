package com.cryptoregistry.rainbow;

import com.cryptoregistry.util.ArrayUtil;

public class Coefficient1D {

	private final short [] coeff;

	public Coefficient1D(short[] coeff) {
		super();
		this.coeff = coeff;
	}

	/**
	 * Return a defensive copy only 
	 * 
	 * @return
	 */
	public short[] getCoeff() {
		
		short[]item = new short[coeff.length];
		System.arraycopy(coeff, 0, item, 0, coeff.length);
		
		return item;
	}
	
	public String encode() {
		return ArrayUtil.encode1dShort(coeff);
	}
	
	
}
