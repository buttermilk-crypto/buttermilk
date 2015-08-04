/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2014 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.proto.reader;

import java.io.UnsupportedEncodingException;

import com.cryptoregistry.protos.Frame.AuthenticatedStringProto;
import com.google.protobuf.InvalidProtocolBufferException;

public class AuthenticatedStringProtoReader {

	final AuthenticatedStringProto proto;

	public AuthenticatedStringProtoReader(AuthenticatedStringProto proto) {
		super();
		this.proto = proto;
	}
	
	public AuthenticatedStringProtoReader(byte [] bytes){
		try {
			this.proto = AuthenticatedStringProto.parseFrom(bytes);
		} catch (InvalidProtocolBufferException e) {
			throw new RuntimeException("StringProto failed to initialize");
		}
	}

	public String read() {
		try {
			String charset = proto.getEncoding();
			return proto.getData().toString(charset);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Bad encoding");
		}
	}
}
