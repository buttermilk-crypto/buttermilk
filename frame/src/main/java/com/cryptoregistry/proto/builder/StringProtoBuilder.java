/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2014 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.proto.builder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.cryptoregistry.protos.Frame.StringProto;
import com.google.protobuf.ByteString;

/**
 * Make a proto from a String. We try to go a little further than Google Protobuf's string type, which is only UTF-8
 * 
 * @author Dave
 *
 */
public class StringProtoBuilder {

	final Charset encoding;
	final byte [] data;

	public StringProtoBuilder(String in) {
		this.encoding = StandardCharsets.UTF_8;
		this.data = in.getBytes(this.encoding);
		
	}
	
	public StringProtoBuilder(String charsetName, String in) {
		this.encoding = Charset.forName(charsetName);
		this.data = in.getBytes(this.encoding);
	}
	
	public StringProtoBuilder(Charset charset, byte [] in) {
		this.encoding = charset;
		this.data = in;
	}
	
	public StringProto build() {
		
		StringProto.Builder builder = StringProto.newBuilder();
		builder.setEncoding(encoding.name())
			.setData(ByteString.copyFrom(data));
		
		return builder.build();
	}
}
