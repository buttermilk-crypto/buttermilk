package com.cryptoregistry.dsa;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.cryptoregistry.CryptoKeyWrapper;
import com.cryptoregistry.KeyMaterials;
import com.cryptoregistry.formats.JSONFormatter;
import com.cryptoregistry.formats.JSONReader;
import com.cryptoregistry.passwords.Password;

public class DSATest {

	@Test
	public void test0() {
		char [] pass = {'p','a','s','s'};
		DSAKeyContents contents = CryptoFactory.INSTANCE.generateKeys(pass, 1024,80);
		Assert.assertNotNull(contents);
		DSAKeyForPublication pub = contents.cloneForPublication();
		JSONFormatter formatter = new JSONFormatter("Chinese Eyes", "dave@cryptoregistry.com");
		formatter.add(pub);
		formatter.add(contents);
		StringWriter writer = new StringWriter();
		formatter.format(writer);
		
		String out = writer.toString();
		
		JSONReader reader = new JSONReader(new StringReader(out));
		KeyMaterials km = reader.parse();
		List<CryptoKeyWrapper> list = km.keys();
		CryptoKeyWrapper wrapper0 = list.get(0);
		Assert.assertTrue(wrapper0.isForPublication());
		
		char [] pass_again = {'p','a','s','s'};
		CryptoKeyWrapper wrapper1 = list.get(1);
		wrapper1.unlock(new Password(pass_again));
		Assert.assertTrue(!wrapper1.isForPublication());
		DSAKeyContents dsaRecoveredContents = (DSAKeyContents) wrapper1.getKeyContents();
		Assert.assertEquals(contents.formatJSON(), dsaRecoveredContents.formatJSON());
	}

}
