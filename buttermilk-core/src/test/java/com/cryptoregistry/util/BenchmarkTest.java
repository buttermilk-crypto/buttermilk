package com.cryptoregistry.util;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.Test;

public class BenchmarkTest {
	
	@Test 
	public void test1() {
		Microbenchmark m = new Microbenchmark("test0");
		m.getTimes().add(TimeUnit.NANOSECONDS.convert(1000, TimeUnit.MILLISECONDS));
		m.getTimes().add(TimeUnit.NANOSECONDS.convert(2000, TimeUnit.MILLISECONDS));
		m.getTimes().add(TimeUnit.NANOSECONDS.convert(3000, TimeUnit.MILLISECONDS));
		Assert.assertEquals(1000, m.min());
		Assert.assertEquals(3000, m.max());
		Assert.assertEquals(2000.00, m.avg());
	}

	@Test
	public void test0() {
		
		String name="test";
		Random rand = new Random();
		StopWatch.INSTANCE.add(name);
		
		try {
			for(int i = 0;i<10;i++){
				StopWatch.INSTANCE.find(name).start();
				Thread.sleep(rand.nextInt(1000));
				StopWatch.INSTANCE.stop(name);
			}
			StopWatch.INSTANCE.printAll();
		
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

}
