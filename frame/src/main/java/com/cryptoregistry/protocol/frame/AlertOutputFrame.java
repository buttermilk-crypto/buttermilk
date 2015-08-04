/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2015 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.protocol.frame;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import com.cryptoregistry.proto.builder.AuthenticatedStringProtoBuilder;
import com.cryptoregistry.protos.Frame.AuthenticatedStringProto;

/**
 * Alerts are in UTF-8 and have a max length of 32767 bytes. This is somewhat arbitrary.
 * 
 * @author Dave
 *
 */
public class AlertOutputFrame extends OutputFrameBase implements OutputFrame {

	final byte contentType; // code for this message type, always BTLSProtocol.ALERT
	final short subcode; // subcode, something specific to the alert
	final String data; // assumed to be UTF-8, human readable message
	final byte [] hmac;
	
	
	public AlertOutputFrame(int contentType, int subcode,  String msg, byte [] hmac) {
		this.contentType = (byte) contentType;
		this.subcode = (short) subcode;
		this.data = msg;
		this.hmac = hmac;
	}
	
	public void writeFrame(OutputStream out) {
		AuthenticatedStringProtoBuilder builder = new AuthenticatedStringProtoBuilder(data,hmac);
		AuthenticatedStringProto proto = builder.build();
		byte [] bytes = proto.toByteArray();
		int sz = bytes.length;
		if(sz > 32767) throw new RuntimeException("payload length cannot be more than 32767 bytes");
		try {
			this.writeByte(out, contentType);
			this.writeShort(out, subcode);
			this.writeShort(out, sz); // TODO validate sz, length cannot exceed 32767
			out.write(bytes);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public byte [] outputFrameContents() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		writeFrame(out);
		return out.toByteArray();
	}
	
	public int contentType() {
		return contentType;
	}

	public int length() {
		return data.length();
	}

	public byte[] data() {
		// TODO Auto-generated method stub
		try {
			return data.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

}
