/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2015 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.protocol.frame;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.cryptoregistry.proto.builder.StringProtoBuilder;

/**
 * StringOutputFrames have a max length payload of Integer.MAX_LENGTH. The String encoding may be of 
 * any Charset which is valid on both of the sending and receiving systems. 
 * 
 * @author Dave
 *
 */
public class StringOutputFrame extends OutputFrameBase implements OutputFrame {

	final byte contentType; // code for this message type, always will be BTLSProtocol.STRING
	final int length; // length of the remaining data in bytes not including the content type or length fields
	final byte [] data; // contents of a serialized StringProto
	
	public StringOutputFrame(int contentType, String msg) {
		this.contentType = (byte) contentType;
		StringProtoBuilder builder = new StringProtoBuilder(msg);
		data = builder.build().toByteArray();
		length = data.length;
	}
	
	public StringOutputFrame(int contentType, Charset charset, byte [] stringBytes) {
		this.contentType = (byte) contentType;
		StringProtoBuilder builder = new StringProtoBuilder(charset,stringBytes);
		data = builder.build().toByteArray();
		length = data.length;
	}
	
	public void writeFrame(OutputStream out) {
		int sz = data.length;
		try {
			this.writeByte(out, contentType);
			this.writeInt(out, sz); 
			out.write(data);
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
		return length;
	}

	public byte[] data() {
		// TODO Auto-generated method stub
		return data;
	}

	
}
