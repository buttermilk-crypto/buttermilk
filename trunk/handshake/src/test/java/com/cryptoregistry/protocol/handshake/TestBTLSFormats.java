package com.cryptoregistry.protocol.handshake;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Iterator;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.cryptoregistry.btls.BTLSProtocol;
import com.cryptoregistry.c2.CryptoFactory;
import com.cryptoregistry.c2.key.C2KeyMetadata;
import com.cryptoregistry.c2.key.Curve25519KeyContents;
import com.cryptoregistry.protocol.BTLSData;
import com.cryptoregistry.protocol.BTLSMessageFactory;
import com.cryptoregistry.protocol.Handshake1;
import com.cryptoregistry.protocol.HandshakeConstants;
import com.cryptoregistry.protocol.frame.JSONFrameReader;
import com.cryptoregistry.protocol.frame.StringOutputFrame;
import com.cryptoregistry.protocol.msg.ConfirmData;
import com.cryptoregistry.protocol.msg.ConnectSecure;
import com.cryptoregistry.protocol.msg.Status;

public class TestBTLSFormats {
	

	@Test
	public void test0() throws IOException, InterruptedException {

		C2KeyMetadata meta0 = C2KeyMetadata.createUnsecured(); 
		Curve25519KeyContents keys0 = CryptoFactory.INSTANCE.generateKeys(meta0);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ConnectSecure cs = BTLSMessageFactory.createConnectSecureReq(
				HandshakeConstants.HANDSHAKE1.ID, 
				"Chinese Knees", 
				keys0.keyForPublication());
		
		String json = cs.formatJSON();
		System.err.println(json);
		
		StringOutputFrame frame = new StringOutputFrame(BTLSProtocol.STRING, json);
		frame.writeFrame(out);
		
		byte [] output = out.toByteArray();
		
		ByteArrayInputStream in = new ByteArrayInputStream(output);
		
		JSONFrameReader reader = new JSONFrameReader(BTLSProtocol.STRING);
		Map<String,Object> result = reader.read(in);
	
		Assert.assertEquals("CONNECT-SECURE", dig(result, "Action"));
		Assert.assertEquals(HandshakeConstants.HANDSHAKE1.ID, dig(result, "Handshake"));
	}
	
	@Test
	public void test0a() throws IOException, InterruptedException {

		BTLSData data = new BTLSData();
		 data.put("Action", "CONFIRM-DATA");
	     data.put("Sample.Data", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
	     data.put("Sample.Data.Encrypted", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
	     data.put("Sample.Encoding", "Base64url");
	     data.put("EncryptionAlg", "AES/CBC/PKCS7Padding");
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ConfirmData cd = BTLSMessageFactory.createConfirmDataReq("Chinese Knees", data);
		
		String json = cd.formatJSON();
		System.err.println(json);
		
		StringOutputFrame frame = new StringOutputFrame(BTLSProtocol.STRING, json);
		frame.writeFrame(out);
		
		byte [] output = out.toByteArray();
		
		ByteArrayInputStream in = new ByteArrayInputStream(output);
		
		JSONFrameReader reader = new JSONFrameReader(BTLSProtocol.STRING);
		Map<String,Object> result = reader.read(in);
	
		Assert.assertEquals("CONFIRM-DATA", dig(result, "Action"));
		Assert.assertEquals("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", dig(result, "Sample.Data"));
	}
	
	@Test
	public void test0b() throws IOException, InterruptedException {
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Status st = BTLSMessageFactory.createStatusMsg("Chinese Knees", 32, "Greetings");
		
		String json = st.formatJSON();
		System.err.println(json);
		
		StringOutputFrame frame = new StringOutputFrame(BTLSProtocol.STRING, json);
		frame.writeFrame(out);
		
		byte [] output = out.toByteArray();
		
		ByteArrayInputStream in = new ByteArrayInputStream(output);
		
		JSONFrameReader reader = new JSONFrameReader(BTLSProtocol.STRING);
		Map<String,Object> result = reader.read(in);
	
		Assert.assertEquals("32", dig(result, "StatusCode"));
		Assert.assertEquals("Greetings", dig(result, "StatusMsg"));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String dig(Map<String,Object> result, String key){
		Map data = (Map)result.get("Data");
		Map local = (Map) data.get("Local");
		Iterator<String> iter = local.keySet().iterator();
		String item = "";
		if(iter.hasNext()){
			String uuid = iter.next();
			Map map = (Map)local.get(uuid);
			if(!map.containsKey(key)) throw new RuntimeException("Key "+key+" not found");
			item = (String) map.get(key);
		}
		return item;
	}
	
	@Test
	public void test2() throws IOException, InterruptedException {
		
		C2KeyMetadata meta0 = C2KeyMetadata.createUnsecured(); 
		Curve25519KeyContents keys0 = CryptoFactory.INSTANCE.generateKeys(meta0);
		
		C2KeyMetadata meta1 = C2KeyMetadata.createUnsecured(); 
		Curve25519KeyContents keys1 = CryptoFactory.INSTANCE.generateKeys(meta1);
		
		PipedOutputStream clientOut = new PipedOutputStream();
		PipedOutputStream serverOut = new PipedOutputStream();
		
		PipedInputStream clientIn = new PipedInputStream(serverOut);
		PipedInputStream serverIn = new PipedInputStream(clientOut);
		
		
		Handshake1 hClient = new Handshake1(true, 
				clientIn, 
				clientOut, 
				keys0,
				"Chinese Knees");
		
		Handshake1 hServer = new Handshake1(false, 
				serverIn, 
				serverOut, 
				keys1,
				"cryptoregistry.com");
		
		Thread t0 = new Thread(hClient);
		Thread t1 = new Thread(hServer);
		
		t1.start();
		t0.start();
		t0.join();
		t1.join();
		System.err.println(hClient.getCurrentState());
		System.err.println(hServer.getCurrentState());
	}

}
