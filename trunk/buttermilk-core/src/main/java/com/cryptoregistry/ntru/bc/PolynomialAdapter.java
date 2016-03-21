/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2014 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.ntru.bc;

import com.cryptoregistry.util.ArmoredCompressedString;
import com.cryptoregistry.util.ArrayUtil;

import org.bouncycastle.pqc.math.ntru.polynomial.IntegerPolynomial;
import org.bouncycastle.pqc.math.ntru.polynomial.Polynomial;

public class PolynomialAdapter {
	
	// use with h and fp
	public static IntegerPolynomial unwrapIntegerPolynomial(String encoded){
		ArmoredCompressedString str = new ArmoredCompressedString(encoded);
		int [] array = ArrayUtil.unwrapCompressed(str);
		return new IntegerPolynomial(array);
	}

	public static Polynomial unwrapDense(String encoded){
		ArmoredCompressedString str = new ArmoredCompressedString(encoded);
		int [] array = ArrayUtil.unwrapCompressed(str);
		return new ButtermilkDenseTernaryPolynomial(array);
	}
	
	public static Polynomial unwrapSparse(String encoded){
		ArmoredCompressedString str = new ArmoredCompressedString(encoded);
		int [] array = ArrayUtil.unwrapCompressed(str);
		return new ButtermilkSparseTernaryPolynomial(array);
	}
	
	public static Polynomial unwrapProductForm(String encoded0, String encoded1, String encoded2){
		
		ArmoredCompressedString str0 = new ArmoredCompressedString(encoded0);
		int [] array0 = ArrayUtil.unwrapCompressed(str0);
		ButtermilkSparseTernaryPolynomial sp0 = new ButtermilkSparseTernaryPolynomial(array0);
		
		ArmoredCompressedString str1 = new ArmoredCompressedString(encoded1);
		int [] array1 = ArrayUtil.unwrapCompressed(str1);
		ButtermilkSparseTernaryPolynomial sp1 = new ButtermilkSparseTernaryPolynomial(array1);
		
		ArmoredCompressedString str2 = new ArmoredCompressedString(encoded2);
		int [] array2 = ArrayUtil.unwrapCompressed(str2);
		ButtermilkSparseTernaryPolynomial sp2 = new ButtermilkSparseTernaryPolynomial(array2);
		
		return new ButtermilkProductFormPolynomial(sp0,sp1,sp2);
	}
}
