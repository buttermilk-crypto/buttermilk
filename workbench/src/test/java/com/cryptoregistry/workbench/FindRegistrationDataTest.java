package com.cryptoregistry.workbench;

import java.io.InputStream;

import org.junit.Test;

import asia.redact.bracket.properties.Properties;

public class FindRegistrationDataTest {

	@Test
	public void findRegData() {
		InputStream in = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("regwizard.properties");
		Properties props = Properties.Factory.getInstance(in);
		RegHandleDataRetriever r = new RegHandleDataRetriever(props);
		System.err.println(r.retrieve("Chinese Knees"));
		
	}

}
