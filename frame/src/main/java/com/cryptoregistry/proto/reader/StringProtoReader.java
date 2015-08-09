/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2014 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.proto.reader;

import java.io.UnsupportedEncodingException;

import com.cryptoregistry.protos.Frame.StringProto;
import com.google.protobuf.InvalidProtocolBufferException;

public class StringProtoReader {

	final StringProto proto;

	public StringProtoReader(StringProto proto) {
		super();
		this.proto = proto;
	}
	
	public StringProtoReader(byte [] bytes){
		try {
			this.proto = StringProto.parseFrom(bytes);
		} catch (InvalidProtocolBufferException e) {
			throw new RuntimeException(e);
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
