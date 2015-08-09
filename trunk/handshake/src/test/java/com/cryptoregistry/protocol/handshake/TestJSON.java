package com.cryptoregistry.protocol.handshake;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.cryptoregistry.btls.BTLSProtocol;
import com.cryptoregistry.c2.CryptoFactory;
import com.cryptoregistry.c2.key.C2KeyMetadata;
import com.cryptoregistry.c2.key.Curve25519KeyContents;
import com.cryptoregistry.formats.JSONFormatter;
import com.cryptoregistry.protocol.frame.JSONFrameReader;
import com.cryptoregistry.protocol.frame.StringOutputFrame;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestJSON {

	@Test
	public void test0() throws JsonGenerationException, JsonMappingException, IOException {
		
		ObjectMapper mapper = new ObjectMapper();
		
		C2KeyMetadata meta = C2KeyMetadata.createUnsecured("TEST"); // key handle will be set to the token "TEST"
		Curve25519KeyContents keys0 = CryptoFactory.INSTANCE.generateKeys(meta);
		JSONFormatter format = new JSONFormatter("Chinese Knees");
	    format.add(keys0.copyForPublication()); // makes a clone ready for publication
	    
		StringWriter writer = new StringWriter();
		format.format(writer);
		StringOutputFrame frame = new StringOutputFrame(BTLSProtocol.STRING, writer.toString());
		
		ByteArrayInputStream in = new ByteArrayInputStream(frame.outputFrameContents());
		
		try {
			
			Map m = new JSONFrameReader(BTLSProtocol.STRING).read(in);
			System.err.println(in.available());
			
			int val = -1;
			while((val = in.read()) != -1){
				System.err.println((char)val);
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
