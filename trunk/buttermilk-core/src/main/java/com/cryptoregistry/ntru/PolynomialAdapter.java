/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2014 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.ntru;

import com.cryptoregistry.util.ArmoredCompressedString;
import com.cryptoregistry.util.ArrayUtil;

import org.bouncycastle.pqc.math.ntru.polynomial.DenseTernaryPolynomial;
import org.bouncycastle.pqc.math.ntru.polynomial.IntegerPolynomial;
import org.bouncycastle.pqc.math.ntru.polynomial.Polynomial;
import org.bouncycastle.pqc.math.ntru.polynomial.ProductFormPolynomial;
import org.bouncycastle.pqc.math.ntru.polynomial.SparseTernaryPolynomial;

public class PolynomialAdapter {
	
	// use with h and fp
	public static IntegerPolynomial unwrapIntegerPolynomial(String encoded){
		ArmoredCompressedString str = new ArmoredCompressedString(encoded);
		int [] array = ArrayUtil.unwrapIntArray(str);
		return new IntegerPolynomial(array);
	}

	public static Polynomial unwrapDense(String encoded){
		ArmoredCompressedString str = new ArmoredCompressedString(encoded);
		int [] array = ArrayUtil.unwrapIntArray(str);
		return new DenseTernaryPolynomial(array);
	}
	
	public static Polynomial unwrapSparse(String encoded){
		ArmoredCompressedString str = new ArmoredCompressedString(encoded);
		int [] array = ArrayUtil.unwrapIntArray(str);
		return new SparseTernaryPolynomial(array);
	}
	
	public static Polynomial unwrapProductForm(String encoded0){
		
		return null;
		
		//return new ProductFormPolynomial(sp0,sp1,sp2);
	}
}
