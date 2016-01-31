package com.cryptoregistry.workbench;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

public class ClassSearchUtilTest {

	@Test
	public void classSearchUtilTest() {
		@SuppressWarnings("rawtypes")
		List list = ClassSearchUtils.searchClassPath("template", ".properties");
		Assert.assertEquals(6, list.size());
	}

}
