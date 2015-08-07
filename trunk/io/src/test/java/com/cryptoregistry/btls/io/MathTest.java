package com.cryptoregistry.btls.io;

import java.math.BigDecimal;

import junit.framework.Assert;

import org.junit.Test;

public class MathTest {

	@Test
	public void test0() {
		int sz = 1024;
		int length = 4678;
		BigDecimal szBd = new BigDecimal(sz);
		BigDecimal szLength = new BigDecimal(length);
		BigDecimal [] result = szLength.divideAndRemainder(szBd);
		int div = result[0].intValue();
		int rem = result[1].intValue();
		Assert.assertEquals(length, sz*div+rem);
	}
}
