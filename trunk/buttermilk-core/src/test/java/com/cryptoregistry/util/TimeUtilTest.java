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
	
	@Test
	public void test1(){
		// fix or at least still parse previously generated incorrect values
		String formatted = "2016-01-24T04:18:27+0000";
		Date date = TimeUtil.getISO8601FormatDate(formatted);
		Assert.assertNotNull(date);
		
	}

}
