package com.cryptoregistry.client.storage;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.cryptoregistry.client.security.BasicKeyManager;
import com.cryptoregistry.passwords.Password;

import asia.redact.bracket.properties.Obfuscate;

public class KeyVaultTest {

	@Test
	public void test0() throws IOException {
		
		File pathToKey = new File("./data/symmetric-key.json");
		String passwordObfuscated = "uodOSMv48olb28GfVVCchpZc6p6CK8GKQ3Bhz8IWi8k=";
		Password pass = new Password(Obfuscate.FACTORY.decrypt(passwordObfuscated).toCharArray());
		
		BasicKeyManager keyManager = new BasicKeyManager(pathToKey,pass);
		File datastorePath = new File("./data/db");
		KeyVault vault = new KeyVault(datastorePath.getCanonicalPath(), keyManager );
		System.err.println(vault.getViews().getDbStatus());
		vault.close();
	}

}
