package com.cryptoregistry.dsa;

import java.io.StringWriter;

import junit.framework.Assert;

import org.junit.Test;

import com.cryptoregistry.formats.JSONFormatter;

public class DSATest {

	@Test
	public void test0() {
		char [] pass = {'p','a','s','s'};
		DSAKeyContents contents = CryptoFactory.INSTANCE.generateKeys(pass);
		Assert.assertNotNull(contents);
		DSAKeyForPublication pub = contents.cloneForPublication();
		JSONFormatter formatter = new JSONFormatter("Chinese Eyes", "dave@cryptoregistry.com");
		formatter.add(pub);
		formatter.add(contents);
		StringWriter writer = new StringWriter();
		formatter.format(writer);
		System.err.println(writer.toString());
	}

}
