package com.cryptoregistry.proto;

import java.io.ByteArrayInputStream;
import junit.framework.Assert;
import org.junit.Test;

import com.cryptoregistry.protocol.frame.StringFrameReader;
import com.cryptoregistry.protocol.frame.StringOutputFrame;

public class TestFrames {

	@Test
	public void test0() {
		// long enough to use more than one byte to express length
		String inVal = "Hello, this is a string. Hello, this is a string. Hello, this is a string. "+
				"Hello, this is a string. Hello, this is a string.";
		StringOutputFrame frame = new StringOutputFrame(24,inVal);
		byte [] bytes = frame.outputFrameContents();
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		StringFrameReader reader = new StringFrameReader(24);
		String outVal = reader.read(in);
		Assert.assertEquals(inVal, outVal);
	}

}
