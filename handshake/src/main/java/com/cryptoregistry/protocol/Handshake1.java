package com.cryptoregistry.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import com.cryptoregistry.btls.BTLSProtocol;
import com.cryptoregistry.c2.key.Curve25519KeyContents;
import com.cryptoregistry.c2.key.Curve25519KeyForPublication;
import com.cryptoregistry.protocol.frame.JSONFrameReader;
import com.cryptoregistry.protocol.frame.StringOutputFrame;

/**
 * Handshake #1 is a simple ECDH key exchange using either Curve25519 or ECC. A 256 bit key is expected. 
 * 
 * @author Dave
 *
 */
public class Handshake1 implements Runnable {

	final InputStream in;
	final OutputStream out;
	final boolean isClient;
	final String regHandle;
	
	Curve25519KeyContents myKey;
	Curve25519KeyForPublication remoteKey;
	
	public Handshake1(
			boolean isClient, 
			InputStream in, 
			OutputStream out, 
			Curve25519KeyContents myKey,
			String regHandle) {
		super();
		this.in = in;
		this.out = out;
		this.isClient = isClient;
		this.myKey = myKey;
		this.regHandle = regHandle;
	}
	
	public void run() {
		if(isClient){
			try {	
				// 1.0 - send request for CONNECT-SECURE
				ConnectSecure cs = BTLSMessageFactory.createConnectSecureReq(
						HandshakeConstants.HANDSHAKE1.ID, regHandle, myKey.keyForPublication());
				String json = cs.formatJSON();
				StringOutputFrame frame = new StringOutputFrame(BTLSProtocol.STRING, json);
				frame.writeFrame(out);
				out.flush();
				
				
				// we expect either an OK, or a "Rejected" response
			//	JSONFrameReader reader = new JSONFrameReader(BTLSProtocol.STRING);
			//	Map<String,Object> result = reader.read(in);
				
			} catch (IOException e) {
				e.printStackTrace();
			}

		}else{
			// this is the server side
			
			// 1.0 - expecting a CONNECT-SECURE request
			JSONFrameReader reader = new JSONFrameReader(BTLSProtocol.STRING);
			Map<String,Object> result = reader.read(in);
			String reqVal = (String) result.get("Request");
			System.err.println(reqVal);
			
		}
	}
}
