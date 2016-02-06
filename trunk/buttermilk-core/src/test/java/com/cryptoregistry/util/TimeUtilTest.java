package com.cryptoregistry.util;

import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;

public class TimeUtilTest {

	@Test
	public void test0(){
		Date now = new Date();
		String formatted = TimeUtil.format(now);
		System.err.println(formatted);
		Date _now = TimeUtil.getISO8601FormatDate(formatted);
		Assert.assertEquals(now, _now);
	}

}
