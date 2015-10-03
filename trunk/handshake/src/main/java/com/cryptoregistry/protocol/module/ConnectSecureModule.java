package com.cryptoregistry.protocol.module;

import com.cryptoregistry.CryptoKey;
import com.cryptoregistry.KeyGenerationAlgorithm;
import com.cryptoregistry.KeyMaterials;
import com.cryptoregistry.btls.BTLSProtocol;
import com.cryptoregistry.c2.key.Curve25519KeyForPublication;
import com.cryptoregistry.protocol.BTLSMessageFactory;
import com.cryptoregistry.protocol.Handshake;
import com.cryptoregistry.protocol.HandshakeConstants;
import com.cryptoregistry.protocol.Module;
import com.cryptoregistry.protocol.State;
import com.cryptoregistry.protocol.frame.JSONFrameReader;
import com.cryptoregistry.protocol.frame.StringOutputFrame;
import com.cryptoregistry.protocol.msg.ConnectSecure;

public class ConnectSecureModule implements Module {

	private Handshake h;
	
	public ConnectSecureModule(Handshake handshake) {
		this.h = handshake;
	}
	
	public void run() {
		if(h.isClient){
			try {	
				// 1.0 - send request for CONNECT-SECURE to server, includes our key
				System.err.println(" 1.0 - send request for CONNECT-SECURE to server, includes our key");
				ConnectSecure cs = BTLSMessageFactory.createConnectSecureReq(
						HandshakeConstants.HANDSHAKE1.ID, 
						h.regHandle, 
						h.myC2Key.keyForPublication());
				String json = cs.formatJSON();
				StringOutputFrame frame = new StringOutputFrame(BTLSProtocol.STRING, json);
				frame.writeFrame(h.out);
				h.currentState=State.Waiting;
				System.err.println("Current Client state="+h.currentState);
				
				// response may contain a StatusCode
				JSONFrameReader reader = new JSONFrameReader(BTLSProtocol.STRING);
				KeyMaterials km = reader.readKM(h.in);
				
				// Presence of a status code would mean a problem
				if(km.mapData().get(0).data.containsKey("StatusCode")){
					int status = Integer.parseInt(km.mapData().get(0).data.get("StatusCode"));
					if(status > 0) {
						h.currentState=State.Error;
						System.err.println("Current Client state="+h.currentState);
						return;
					}
				}
			
				if(km.keys().size() == 0){
					// no key was sent - which is an error for this handshake
					h.currentState = State.Error;
					System.err.println("Current Client state="+h.currentState);
					return;
				}
				CryptoKey key = km.keys().get(0).getKeyContents();
				if(!key.getMetadata().getKeyAlgorithm().equals(KeyGenerationAlgorithm.Curve25519)){
					// expecting Curve25519 for Handshake1, so we have a problem
					h.currentState = State.Error;
					System.err.println("Current Client state="+h.currentState);
					return;
				}else{
					h.remoteC2Key = (Curve25519KeyForPublication) key;
					System.err.println("Got key from server: "+h.remoteC2Key);
					h.currentState = State.GotKey;
					System.err.println("Current Client state="+h.currentState);
				}
			}catch(Exception x){
				x.printStackTrace();
			}
		}else{
			// server
			try {
				
				// 1.0 - expecting a CONNECT-SECURE request
				System.err.println("Server; 1.0 - expecting a CONNECT-SECURE request from client");
				JSONFrameReader reader = new JSONFrameReader(BTLSProtocol.STRING);
				KeyMaterials km = reader.readKM(h.in);
				
				// Presence of a status code would mean a problem
				if(km.mapData().get(0).data.containsKey("StatusCode")){
					int status = Integer.parseInt(km.mapData().get(0).data.get("StatusCode"));
					if(status > 0) {
						h.currentState=State.Error;
						System.err.println("Current Server state="+h.currentState);
						return;
					}
				}
				
				String clientReqAction = km.mapData().get(0).data.get("Action");
				String reqHandshake = km.mapData().get(0).data.get("Handshake");
				
				if("CONNECT-SECURE".equals(clientReqAction)){
					
					if(km.keys().size() == 0){
						// no key was sent - which is an error for this handshake
						h.currentState = State.Error;
						System.err.println("Current Server state="+h.currentState);
						return;
					}
					
					CryptoKey key = km.keys().get(0).getKeyContents();
					// got request, get client key
					if(!key.getMetadata().getKeyAlgorithm().equals(KeyGenerationAlgorithm.Curve25519)){
						// expecting Curve25519 for Handshake1, so we have a problem
						h.currentState = State.Error;
						System.err.println("Current Server state="+h.currentState);
						return;
					}else{
						h.remoteC2Key = (Curve25519KeyForPublication) key;
						System.err.println("Got key from client: "+h.remoteC2Key);
						h.currentState = State.GotKey;
						System.err.println("Current Server state="+h.currentState);
					}
				}else{
					h.currentState = State.Error;
					System.err.println("Current Server state="+h.currentState);
					return;
				}
				
				// now send our response
				System.err.println("Server; now sending response");
				ConnectSecure cs = BTLSMessageFactory.createConnectSecureReq(
						HandshakeConstants.HANDSHAKE1.ID, 
						h.regHandle, 
						h.myC2Key.keyForPublication());
				
				String json = cs.formatJSON();
				StringOutputFrame frame = new StringOutputFrame(BTLSProtocol.STRING, json);
				frame.writeFrame(h.out);
				System.err.println("Server sent ConnectSecure frame:"+frame);
				
			}catch(Exception x){
				x.printStackTrace();
			}
		}
		
		// all we really need here is a key from the other server
		if(h.currentState == State.GotKey){
			h.currentState = State.Success;
		}
	}

}
