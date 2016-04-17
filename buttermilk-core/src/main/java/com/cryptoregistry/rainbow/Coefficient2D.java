package com.cryptoregistry.rainbow;

import com.cryptoregistry.util.ArrayUtil;

public class Coefficient2D {

	private final short[][] coeff;

	public Coefficient2D(short[][] coeff) {
		super();
		this.coeff = coeff;
	}

	/**
	 * Returns only a defensive copy
	 * 
	 * @return
	 */
	public short[][] getCoeff() {
		
		short[][]copy = new short[coeff.length][];
		for(int i = 0; i<coeff.length;i++){
			short[]array = coeff[i];
			short[]item = new short[array.length];
			System.arraycopy(array, 0, item, 0, array.length);
			copy[i] = item;
		}
		
		return copy;
	}
	  
	public String encode() {
		return ArrayUtil.encode2dShort(coeff);
	}
	
}
