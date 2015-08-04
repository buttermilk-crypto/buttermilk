package com.cryptoregistry.proto;

import junit.framework.Assert;

import org.junit.Test;

import com.cryptoregistry.proto.builder.StringProtoBuilder;
import com.cryptoregistry.proto.reader.StringProtoReader;
import com.cryptoregistry.protos.Frame.StringProto;

public class TestProtos {

	@Test
	public void test0() {
		String in = "Hi!";
		StringProtoBuilder builder = new StringProtoBuilder("Hi!");
		StringProto proto = builder.build();
		StringProtoReader reader = new StringProtoReader(proto);
		String out = reader.read();
		Assert.assertEquals(in, out);
	}

}
