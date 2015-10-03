package com.cryptoregistry.protocol.frame;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Map;

import com.cryptoregistry.KeyMaterials;
import com.cryptoregistry.formats.JSONReader;
import com.cryptoregistry.proto.reader.StringProtoReader;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Expects a String frame which contains a valid JSON that needs parsing
 * 
 * @author Dave
 *
 */
public class JSONFrameReader extends InputFrameBase {

	final int expectedCode;
	
	byte [] readBytes;
	
	public JSONFrameReader(int expectedCode) {
		this.expectedCode=expectedCode;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,Object> readFromBytes(byte [] bytes) {
		try {
			int code = bytes[0];
			if(code != expectedCode){
				throw new RuntimeException("Not expected code: "+code);
			}
			
			int length = this.intFromBytes(bytes,1);
			System.out.println("Length from StringFrameReader:"+length);
			if(length < 0){
				throw new EOFException();
			}
			byte [] buf = new byte[length];
			System.arraycopy(bytes, 5, buf, 0, length);
			StringProtoReader reader = new StringProtoReader(buf);
			String json = reader.read();
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(json, Map.class);
			
		}catch(IOException x) {
			throw new RuntimeException(x);
		}
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,Object> read(InputStream in) {
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
			readBytes = buf;
			String json = reader.read();
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(json, Map.class);
		}catch(IOException x) {
			throw new RuntimeException(x);
		}
	}
	
	public KeyMaterials readKM(InputStream in) {
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
			readBytes = buf;
			String json = reader.read();
			JSONReader jreader = new JSONReader(new StringReader(json));
			return jreader.parse();
		}catch(IOException x) {
			throw new RuntimeException(x);
		}
	}

	public byte[] getReadBytes() {
		return readBytes;
	}

}
