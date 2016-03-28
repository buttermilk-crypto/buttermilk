package com.cryptoregistry.dsa;

import org.junit.Test;

public class DSATest {

	@Test
	public void test0() {
		char [] pass = {'p','a','s','s'};
		CryptoFactory.INSTANCE.generateKeys(pass);
	}

}
