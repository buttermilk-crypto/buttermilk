package com.cryptoregistry.proto;

import java.io.ByteArrayInputStream;
import junit.framework.Assert;
import org.junit.Test;

import com.cryptoregistry.protocol.frame.StringFrameReader;
import com.cryptoregistry.protocol.frame.StringOutputFrame;

public class TestFrames {

	@Test
	public void test0() {
		String inVal = "Hello";
		StringOutputFrame frame = new StringOutputFrame(24,inVal);
		byte [] bytes = frame.outputFrameContents();
		System.err.println(bytes.length);
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		StringFrameReader reader = new StringFrameReader(24);
		String outVal = reader.read(in);
		Assert.assertEquals(inVal, outVal);
	}

}
