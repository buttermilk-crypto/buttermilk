package com.cryptoregistry.protocol;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.Arrays;

import net.iharder.Base64;

import com.cryptoregistry.CryptoKey;
import com.cryptoregistry.KeyGenerationAlgorithm;
import com.cryptoregistry.KeyMaterials;
import com.cryptoregistry.MapData;
import com.cryptoregistry.btls.BTLSProtocol;
import com.cryptoregistry.c2.CryptoFactory;
import com.cryptoregistry.c2.key.Curve25519KeyContents;
import com.cryptoregistry.c2.key.Curve25519KeyForPublication;
import com.cryptoregistry.c2.key.SecretKey;
import com.cryptoregistry.protocol.frame.JSONFrameReader;
import com.cryptoregistry.protocol.frame.StringOutputFrame;
import com.cryptoregistry.protocol.msg.ConfirmData;
import com.cryptoregistry.protocol.msg.ConnectSecure;
import com.cryptoregistry.symmetric.AESCBCPKCS7;

import x.org.bouncycastle.crypto.digests.*;
import x.org.bouncycastle.crypto.io.*;

/**
 * Handshake #1 is a simple ECDH key exchange using either Curve25519 or ECC. 
 * A 256 bit key is expected to be generated.
 * 
 * @author Dave
 *
 */
public class Handshake1 implements Runnable {

	final DigestInputStream in;
	final DigestOutputStream out;
	final boolean isClient;
	final String regHandle;
	
	Curve25519KeyContents myKey;
	Curve25519KeyForPublication remoteKey;
	
	State currentState;
	
	public Handshake1(
			boolean isClient, 
			InputStream in, 
			OutputStream out, 
			Curve25519KeyContents myKey,
			String regHandle) {
		super();
		this.in = new DigestInputStream(in, new SHA256Digest());
		this.out = new DigestOutputStream(out, new SHA256Digest());
		this.isClient = isClient;
		this.myKey = myKey;
		this.regHandle = regHandle;
	
	}
	
	public void run() {
		if(isClient){
			try {	
				// 1.0 - send request for CONNECT-SECURE to server, includes our key
				ConnectSecure cs = BTLSMessageFactory.createConnectSecureReq(
						HandshakeConstants.HANDSHAKE1.ID, 
						regHandle, 
						myKey.keyForPublication());
				String json = cs.formatJSON();
				StringOutputFrame frame = new StringOutputFrame(BTLSProtocol.STRING, json);
				frame.writeFrame(out);
				currentState=State.Waiting;
				
				// response may contain a StatusCode
				JSONFrameReader reader = new JSONFrameReader(BTLSProtocol.STRING);
				KeyMaterials km = reader.readKM(in);
				
				// Presence of a status code would mean a problem
				if(km.mapData().get(0).data.containsKey("StatusCode")){
					int status = Integer.parseInt(km.mapData().get(0).data.get("StatusCode"));
					if(status > 0) {
						currentState=State.Error;
						return;
					}
				}
			
				if(km.keys().size() == 0){
					// no key was sent - which is an error for this handshake
					currentState = State.Error;
					return;
				}
				CryptoKey key = km.keys().get(0).getKeyContents();
				if(!key.getMetadata().getKeyAlgorithm().equals(KeyGenerationAlgorithm.Curve25519)){
					// expecting Curve25519 for Handshake1, so we have a problem
					currentState = State.Error;
					return;
				}else{
					remoteKey = (Curve25519KeyForPublication) key;
					System.err.println("Got key from server: "+remoteKey);
					currentState = State.GotKey;
				}
				
				
				// 2.0 - take snapshot of bytes got and sent
				
				byte [] clientSent = out.getDigest();
				byte [] clientGot = in.getDigest();
				
				// 3.0 - construct ConfirmData message
				
				// 3.1 - create secret key
				
				SecretKey secretKey = CryptoFactory.INSTANCE.keyAgreement(
							remoteKey.publicKey, 
							myKey.agreementPrivateKey
				);
				
				// prepare encryption of sample using our secret key and AES/CBC/PKCS7
				// secret key is not used raw, we digest it first with SHA-256
				byte [] iv = new byte[16];
				SecureRandom rand = SecureRandom.getInstanceStrong();
				rand.nextBytes(iv);
				AESCBCPKCS7 aes = new AESCBCPKCS7(secretKey.getSHA256Digest(),iv);
				aes.init(true);
				
				// aes encrypt the SHA-256 hash of all the bytes we have sent the server so far
				// also send a plain digest of what we think we have sent
				String encryptedEncoded64urlsafe = Base64.encodeBytes(aes.encrypt(clientSent),Base64.URL_SAFE);
				String rawEncoded64urlsafe = Base64.encodeBytes(clientSent,Base64.URL_SAFE);
				
				// create the MapData
				BTLSData data = new BTLSData();
				data.put("Sample.Data", rawEncoded64urlsafe);
				data.put("Sample.Data.Encrypted", encryptedEncoded64urlsafe);
				data.put("Sample.Encoding", "Base64url");
				data.put("EncryptionAlg", "AES/CBC/PKCS7");
				data.put("IV", Base64.encodeBytes(iv, Base64.URL_SAFE));
				
				ConfirmData confirmDataMsg = 
						BTLSMessageFactory.createConfirmDataReq(regHandle, data);
				json = confirmDataMsg.formatJSON();
				frame = new StringOutputFrame(BTLSProtocol.STRING, json);
				frame.writeFrame(out);
				currentState=State.Waiting;
				
				// should now get either an error or a ConfirmData message from server
				
				reader = new JSONFrameReader(BTLSProtocol.STRING);
				km = reader.readKM(in);
				
				// Presence of a status code would mean a problem
				if(km.mapData().get(0).data.containsKey("StatusCode")){
					int status = Integer.parseInt(km.mapData().get(0).data.get("StatusCode"));
					if(status > 0) {
						currentState=State.Error;
						return;
					}
				}
				
				String clientReqAction = km.mapData().get(0).data.get("Action");
				
					if("CONFIRM-ENCRYPT".equals(clientReqAction)){
						
						MapData datam = km.mapData().get(0);
						String sampleDataRaw = datam.get("Sample.Data");
						String sampleDataEnc = datam.get("Sample.Data.Encrypted");
						String sampleEncoding = datam.get("Sample.Encoding");
						String encAlg = datam.get("EncryptionAlg");
						String IV = datam.get("IV");
						
						if(!sampleEncoding.equals("Base64url")){
							currentState = State.Error;
							return;
						}
						
						// 3.1 confirm digest of bytes sent by client (sampleDataRaw) is the same 
						// as digest of what server actually got
						byte [] reportedBytes = Base64.decode(sampleDataRaw, Base64.URL_SAFE);
						
						if(!Arrays.equals(reportedBytes, clientGot)){
							// signal error - not the verb we were expecting here.
							currentState = State.Error;
							return;
						}
						
						// 3.2 confirm encrypt worked as expected - which proves client has same shared secret that we have
						byte [] encrypted = Base64.decode(sampleDataEnc, Base64.URL_SAFE);
						iv = Base64.decode(IV, Base64.URL_SAFE);
						
					
						aes = new AESCBCPKCS7(secretKey.getSHA256Digest(),iv);
						aes.init(false);
						byte [] decrypted = aes.decrypt(encrypted);
						
						if(!Arrays.equals(decrypted, clientGot)){
							// signal error - not the verb we were expecting here.
							currentState = State.Error;
							return;
						}
						
						// SUCCESS, client is validated
						// go set up frameInputStream and FrameOutputStream
						currentState = State.Success;
					}
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			

		}else{
			// this is the server side
			
			try {
			
				// 1.0 - expecting a CONNECT-SECURE request
				JSONFrameReader reader = new JSONFrameReader(BTLSProtocol.STRING);
				KeyMaterials km = reader.readKM(in);
				
				// Presence of a status code would mean a problem
				if(km.mapData().get(0).data.containsKey("StatusCode")){
					int status = Integer.parseInt(km.mapData().get(0).data.get("StatusCode"));
					if(status > 0) {
						currentState=State.Error;
						return;
					}
				}
				
				String clientReqAction = km.mapData().get(0).data.get("Action");
				String reqHandshake = km.mapData().get(0).data.get("Handshake");
				
				if("CONNECT-SECURE".equals(clientReqAction)){
					
					if(km.keys().size() == 0){
						// no key was sent - which is an error for this handshake
						currentState = State.Error;
						return;
					}
					
					CryptoKey key = km.keys().get(0).getKeyContents();
					// got request, get client key
					if(!key.getMetadata().getKeyAlgorithm().equals(KeyGenerationAlgorithm.Curve25519)){
						// expecting Curve25519 for Handshake1, so we have a problem
						currentState = State.Error;
						return;
					}else{
						remoteKey = (Curve25519KeyForPublication) key;
						System.err.println("Got key from client: "+remoteKey);
						currentState = State.GotKey;
					}
				}else{
					currentState = State.Error;
					return;
				}
				
				// now send our response
				ConnectSecure cs = BTLSMessageFactory.createConnectSecureReq(
						HandshakeConstants.HANDSHAKE1.ID, 
						regHandle, 
						myKey.keyForPublication());
				
				String json = cs.formatJSON();
				StringOutputFrame frame = new StringOutputFrame(BTLSProtocol.STRING, json);
				frame.writeFrame(out);
				System.err.println("Server sent frame ");
				
				// 2.0 - take snapshot of bytes got and sent
				
				byte [] serverSent = out.getDigest();
				byte [] serverGot = in.getDigest();
				
				// 3.0 receive CONFIRM-ENCRYPT request
				
				reader = new JSONFrameReader(BTLSProtocol.STRING);
				km = reader.readKM(in);
				
				// Presence of a status code would mean a problem
				if(km.mapData().get(0).data.containsKey("StatusCode")){
					int status = Integer.parseInt(km.mapData().get(0).data.get("StatusCode"));
					if(status > 0) {
						currentState=State.Error;
						return;
					}
				}
				
				clientReqAction = km.mapData().get(0).data.get("Action");
				
					if("CONFIRM-ENCRYPT".equals(clientReqAction)){
						
						MapData data = km.mapData().get(0);
						String sampleDataRaw = data.get("Sample.Data");
						String sampleDataEnc = data.get("Sample.Data.Encrypted");
						String sampleEncoding = data.get("Sample.Encoding");
						String encAlg = data.get("EncryptionAlg");
						String IV = data.get("IV");
						
						if(!sampleEncoding.equals("Base64url")){
							currentState = State.Error;
							return;
						}
						
						// 3.1 confirm digest of bytes sent by client (sampleDataRaw) is the same 
						// as digest of what server actually got
						byte [] reportedBytes = Base64.decode(sampleDataRaw, Base64.URL_SAFE);
						
						if(!Arrays.equals(reportedBytes, serverGot)){
							// signal error - not the verb we were expecting here.
							currentState = State.Error;
							return;
						}
						
						// 3.2 confirm encrypt worked as expected - which proves client has same shared secret that we have
						byte [] encrypted = Base64.decode(sampleDataEnc, Base64.URL_SAFE);
						byte [] iv = Base64.decode(IV, Base64.URL_SAFE);
						
						SecretKey secretKey = CryptoFactory.INSTANCE.keyAgreement(
								remoteKey.publicKey, 
								myKey.agreementPrivateKey
						);
					
						AESCBCPKCS7 aes = new AESCBCPKCS7(secretKey.getSHA256Digest(),iv);
						aes.init(false);
						byte [] decrypted = aes.decrypt(encrypted);
						
						if(!Arrays.equals(decrypted, serverGot)){
							// signal error - not the verb we were expecting here.
							currentState = State.Error;
							return;
						}
						
						// SUCCESS, client is validated
						// go set up frameInputStream and FrameOutputStream
						currentState = State.Success;
						
						// send the client a parallel message
						// after receipt of this message, client may begin sending encrypted frames
						
						// prepare encryption of sample using our secret key and AES/CBC/PKCS7
						// secret key is not used raw, we digest it first with SHA-256
						iv = new byte[16];
						SecureRandom rand = SecureRandom.getInstanceStrong();
						rand.nextBytes(iv);
						aes = new AESCBCPKCS7(secretKey.getSHA256Digest(),iv);
						aes.init(true);
						
						// aes encrypt the SHA-256 hash of all the bytes we have sent the server so far
						// also send a plain digest of what we think we have sent
						String encryptedEncoded64urlsafe = Base64.encodeBytes(aes.encrypt(serverSent),Base64.URL_SAFE);
						String rawEncoded64urlsafe = Base64.encodeBytes(serverSent,Base64.URL_SAFE);
						
						// create the MapData
						BTLSData data0 = new BTLSData();
						data0.put("Sample.Data", rawEncoded64urlsafe);
						data0.put("Sample.Data.Encrypted", encryptedEncoded64urlsafe);
						data0.put("Sample.Encoding", "Base64url");
						data0.put("EncryptionAlg", "AES/CBC/PKCS7");
						data0.put("IV", Base64.encodeBytes(iv, Base64.URL_SAFE));
						
						ConfirmData confirmDataMsg = 
								BTLSMessageFactory.createConfirmDataReq(regHandle, data0);
						json = confirmDataMsg.formatJSON();
						frame = new StringOutputFrame(BTLSProtocol.STRING, json);
						frame.writeFrame(out);
						
						return;
						
					} else{
						// error, not the expected message type
						currentState = State.Error;
						return;
					}
				
			
			}catch(Exception x){
				x.printStackTrace();
			}
		}
	}
	
	public State getCurrentState() {
		return currentState;
	}



	private enum State{
		Waiting, ConnectSecure, GotKey, Error, Success
	}
}
