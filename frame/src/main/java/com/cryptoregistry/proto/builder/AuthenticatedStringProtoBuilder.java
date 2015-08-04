/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2014 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.proto.builder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.cryptoregistry.protos.Frame.AuthenticatedStringProto;
import com.google.protobuf.ByteString;

/**
 * Make a proto from a String
 * 
 * @author Dave
 *
 */
public class AuthenticatedStringProtoBuilder {

	final Charset encoding;
	final byte [] data;
	final byte [] hmac;
	
	public AuthenticatedStringProtoBuilder(String in, byte [] hmac) {
		this.encoding = StandardCharsets.UTF_8;
		this.data = in.getBytes(this.encoding);
		this.hmac = hmac;
	}
	
	public AuthenticatedStringProtoBuilder(String charsetName, String in, byte [] hmac) {
		this.encoding = Charset.forName(charsetName);
		this.data = in.getBytes(this.encoding);
		this.hmac = hmac;
	}
	
	public AuthenticatedStringProtoBuilder(Charset charset, byte [] in, byte [] hmac) {
		this.encoding = charset;
		this.data = in;
		this.hmac = hmac;
	}
	
	public AuthenticatedStringProto build() {
		
		AuthenticatedStringProto.Builder builder = AuthenticatedStringProto.newBuilder();
		builder.setEncoding(encoding.name())
		    .setData(ByteString.copyFrom(data))
			.setHmac(ByteString.copyFrom(hmac));
		
		return builder.build();
	}
}
