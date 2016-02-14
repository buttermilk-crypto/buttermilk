package com.cryptoregistry.formats;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;


public class JSONGenericHmacTest {

	@Test
	public void test0() {
		File f = new File("src/test/resources/keys.test.json");
		JSONGenericReader reader = new JSONGenericReader(f);
		byte [] key = {'1','2','3','4','5','6','7','8','9',
						'0','1','2','3','4','5','6','7','8','9',
						'0','1','2','3','4','5','6','7','8','9',
						'0','1','2'};
		byte [] result = reader.hmac(key);
		Assert.assertTrue(reader.hmacValidate(key, result));
		reader.embedHMac(key);
		Assert.assertTrue(reader.macs().size()==1);
	}
	
	@Test
	public void test1() {
		File f = new File("src/test/resources/another-good-c2.json");
		JSONGenericReader reader = new JSONGenericReader(f);
		byte [] key = {'1','2','3','4','5','6','7','8','9',
						'0','1','2','3','4','5','6','7','8','9',
						'0','1','2','3','4','5','6','7','8','9',
						'0','1','2'};
		byte [] result = reader.hmac(key);
		Assert.assertTrue(reader.hmacValidate(key, result));
		
	}

}
