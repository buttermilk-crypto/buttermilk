package com.cryptoregistry.protocol.frame;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import com.cryptoregistry.proto.reader.StringProtoReader;

public class StringFrameReader extends InputFrameBase {

	final int expectedCode;
	
	public StringFrameReader(int expectedCode) {
		this.expectedCode=expectedCode;
	}
	
	public String readFromBytes(byte [] bytes) {
		try {
			int code = bytes[0];
			if(code != expectedCode){
				throw new RuntimeException("Not expected code: "+code);
			}
			byte [] four = new byte [4];
			System.arraycopy(bytes, 1, four, 0, 4);
			int length = this.intFromBytes(four);
			if(length < 0){
				throw new EOFException();
			}
			byte [] buf = new byte[length];
			System.arraycopy(bytes, 5, buf, 0, length);
			StringProtoReader reader = new StringProtoReader(buf);
			return reader.read();
		}catch(IOException x) {
			throw new RuntimeException(x);
		}
	}
	
	public String read(InputStream in) {
		try {
			int code = in.read();
			if(code != expectedCode){
				throw new RuntimeException("Not expected code: "+code);
			}
			
			int length = this.readInt32(in);
		
			if(length < 0){
				throw new EOFException();
			}
			byte [] buf = new byte[length];
			in.read(buf, 0, length);
			StringProtoReader reader = new StringProtoReader(buf);
			return reader.read();
		}catch(IOException x) {
			throw new RuntimeException(x);
		}
	}

}
