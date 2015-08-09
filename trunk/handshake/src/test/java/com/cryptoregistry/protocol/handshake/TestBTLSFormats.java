package com.cryptoregistry.protocol.handshake;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;

import org.junit.Test;

import com.cryptoregistry.btls.BTLSProtocol;
import com.cryptoregistry.c2.CryptoFactory;
import com.cryptoregistry.c2.key.C2KeyMetadata;
import com.cryptoregistry.c2.key.Curve25519KeyContents;
import com.cryptoregistry.protocol.BTLSMessageFactory;
import com.cryptoregistry.protocol.ConnectSecure;
import com.cryptoregistry.protocol.HandshakeConstants;
import com.cryptoregistry.protocol.frame.JSONFrameReader;
import com.cryptoregistry.protocol.frame.StringOutputFrame;

public class TestBTLSFormats {
	
	public final void writeInt(OutputStream out, int v) throws IOException {
        out.write((v >>> 24) & 0xFF);
        out.write((v >>> 16) & 0xFF);
        out.write((v >>>  8) & 0xFF);
        out.write((v >>>  0) & 0xFF);
    }
	
	public final int readInt32(InputStream in) throws IOException {
		int ch1 = in.read();
		int ch2 = in.read();
		int ch3 = in.read();
		int ch4 = in.read();
		if ((ch1 | ch2 | ch3 | ch4) < 0)
			throw new EOFException();
		return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
	}
	
	@Test
	public void testVals() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		writeInt(out, 500);
		byte [] bytes = out.toByteArray();
		System.err.println(Arrays.toString(bytes));
		
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		int val = readInt32(in);
		System.err.println(val);
	}

	@Test
	public void test0() throws IOException, InterruptedException {

		C2KeyMetadata meta0 = C2KeyMetadata.createUnsecured(); 
		Curve25519KeyContents keys0 = CryptoFactory.INSTANCE.generateKeys(meta0);
		
		C2KeyMetadata meta1 = C2KeyMetadata.createUnsecured(); 
		Curve25519KeyContents keys1 = CryptoFactory.INSTANCE.generateKeys(meta1);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ConnectSecure cs = BTLSMessageFactory.createConnectSecureReq(
				HandshakeConstants.HANDSHAKE1.ID, "Chinese Knees", keys0.keyForPublication());
		String json = cs.formatJSON();
		StringOutputFrame frame = new StringOutputFrame(BTLSProtocol.STRING, json);
		frame.writeFrame(out);
		
		byte [] output = out.toByteArray();
		
		System.err.println(Arrays.toString(output));
		
		ByteArrayInputStream in = new ByteArrayInputStream(output);
		
		JSONFrameReader reader = new JSONFrameReader(BTLSProtocol.STRING);
		Map<String,Object> result = reader.read(in);
		String reqVal = (String) result.get("Request");
		System.err.println(reqVal);
		
		
	}

}
