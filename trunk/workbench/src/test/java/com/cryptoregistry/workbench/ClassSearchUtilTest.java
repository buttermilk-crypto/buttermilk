package com.cryptoregistry.workbench;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

public class ClassSearchUtilTest {

	@Test
	public void classSearchUtilTest() {
		@SuppressWarnings("rawtypes")
		List list = ClassSearchUtils.searchClassPath("demo", ".properties");
		Assert.assertEquals(4, list.size());
	}

}
