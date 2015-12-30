package com.cryptoregistry.workbench;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import asia.redact.bracket.properties.Properties;
import asia.redact.bracket.properties.mgmt.PropertiesReference;
import asia.redact.bracket.properties.mgmt.ReferenceType;

public class ExternalPropsManagerTest {

	@Test
	public void test0() throws IOException {
		
		// first remove any traces
		File home = new File(System.getProperty("user.home"));
		File regwizExternal = new File(home,"regwizard.properties");
		regwizExternal.delete();
		
		List<PropertiesReference> refs = new ArrayList<PropertiesReference>();
	    refs.add(new PropertiesReference(ReferenceType.CLASSLOADED,"regwizard.properties"));
		refs.add(new PropertiesReference(ReferenceType.EXTERNAL,regwizExternal.getCanonicalPath()));
		
	   Properties props = Properties.Factory.loadReferences(refs);
	   Assert.assertTrue(props.containsKey("app.version"));
	   ExternalPropsManager mgr = new ExternalPropsManager(props);
	   Assert.assertTrue(!mgr.externalFileExists());
	   Assert.assertTrue(!mgr.hasDefaultKeyDirectoryLocation());
	   
	   mgr.write();
	   Assert.assertTrue(mgr.externalFileExists());
	   
	   mgr.put("default.key.directory","C:\\");
	   mgr.write();
	   mgr.read();
	   
	   Assert.assertTrue(mgr.externalFileExists());
	   Assert.assertTrue(mgr.hasDefaultKeyDirectoryLocation());
	   Assert.assertTrue(mgr.getProps().containsKey("default.key.directory"));
	   
	 //  props = Properties.Factory.loadReferences(refs);
	//   Assert.assertTrue(props.containsKey("default.key.directory"));
	   
	   
	}

}
