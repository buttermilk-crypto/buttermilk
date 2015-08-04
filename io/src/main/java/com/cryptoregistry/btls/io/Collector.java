package com.cryptoregistry.btls.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import x.org.bouncycastle.crypto.Digest;
import x.org.bouncycastle.crypto.digests.SHA256Digest;
import x.org.bouncycastle.crypto.macs.HMac;
import x.org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import x.org.bouncycastle.crypto.params.KeyParameter;
import x.org.bouncycastle.crypto.params.ParametersWithIV;

import com.cryptoregistry.btls.BTLSProtocol;
import com.cryptoregistry.proto.reader.AuthenticatedStringProtoReader;
import com.cryptoregistry.protos.Frame.AuthenticatedStringProto;
import com.cryptoregistry.symmetric.AESCBCPKCS7;

/**
 * Collector runs as a different thread and gathers bytes from the underlying incoming stream 
 * which has been wrapped by a FrameInputStream. The bytes are in the form of frames which must be
 * decoded or decrypted. 
 * 
 * @author Dave
 *
 */
public class Collector implements Runnable {
	
	final byte [] key;
	protected ParametersWithIV params;
	protected KeyParameter hmacParam;
	final Digest digest;
	protected PaddedBufferedBlockCipher aesCipher;

	final BlockingQueue<byte[]> queue;
	final InputStream in;
	final Set<AlertListener> alertListeners;
	
	boolean running;
	protected Exception exception;
	
	final int COLLECTOR_PAUSE_MILLISEC = 10;
	
	public Collector(FrameEventConsumer frameInputStream) {
		this.queue= frameInputStream.queue();
		this.in = frameInputStream.in();
		this.key = frameInputStream.key();
		this.alertListeners = frameInputStream.alertListeners();
		this.digest = new SHA256Digest();
	}

	@Override
	public void run() {
		running=true;
		loop:while(true){
			try {
				int code = readFrame();
				if(code == -1) break loop;
				Thread.sleep(COLLECTOR_PAUSE_MILLISEC);
			} catch (Exception e) {
				running = false;
				exception=e;
			}
		}
		running = false;
		boolean accepted = queue.offer(new byte[0]); // poison message to stop processing
		if(!accepted){
			
		}
		
	}
	
	protected int readFrame() throws SecurityException, InterruptedException{
		
	//	System.err.println("Entering readFrame()");
		
		int code = -1;
		
		try {
			code = this.readByte(in);
			if(code == -1) return -1;
			
		//	System.err.println("code="+code);

			switch(code){
			
				// alerts are not encrypted (because the alert code is self-revealing) but we do validate them using an HMac
				case BTLSProtocol.ALERT: {
					
					int alertCode = this.readShort16(in); // consume this?
					int sz = this.readShort16(in);
					byte [] b = new byte[sz];
					this.readFully(in, b, 0, sz);
					AuthenticatedStringProto asp = AuthenticatedStringProto.parseFrom(b);
					AuthenticatedStringProtoReader aspr = new AuthenticatedStringProtoReader(asp);
					String alertText = aspr.read();
					byte [] hmacBytes = asp.getHmac().toByteArray();
					hmacParam = new KeyParameter(key,0,key.length);
					
					if(!validateHMac(hmacParam, hmacBytes, alertText.getBytes("UTF-8"))) {
						throw new SecurityException("Alert Frame appears to have been tampered with...bailing out.");
					}else{
					//	System.err.println("Alert looks valid...");
					}
					
					// consume the alert
					Iterator<AlertListener> iter = alertListeners.iterator();
					while(iter.hasNext()){
						AlertListener al = iter.next();
						al.alertReceived(new AlertEvent(this,alertCode,alertText));
					}
					break;
				}
				
				// application messages are encrypted and validated using an HMac
				case BTLSProtocol.APPLICATION: {
					
					// first there is a Byte String with the IV size and contents
					int IVSz = this.readShort16(in);
					byte [] iv = new byte[IVSz];
					this.readFully(in, iv, 0, IVSz);
					
					// second there is the encrypted contents, max length is Integer.MAX_VALUE, about 2.15Gb
					int contentsSz = this.readInt32(in);
					byte [] contents = new byte[contentsSz];
					this.readFully(in, contents, 0, contentsSz);
					
					// third, validate
					int macSz = this.readShort16(in);
					if(macSz == 0) break;// bail if no hmac provided
					hmacParam = new KeyParameter(key,0,key.length);
					byte [] hmacBytes = new byte[macSz];
					this.readFully(in, hmacBytes, 0, macSz);
					if(!validateHMac(hmacParam, hmacBytes, contents)) {
						throw new SecurityException("Application Frame appears to have been tampered with...bailing out.");
					}else{
					//	System.err.println("validation of hmac successful!");
					}
					
					// we've passed validation, decrypt
					
					// use the iv from the message along with the key to initialize the cipher
					params = buildKey(key,iv);
					
					//decrypt
					AESCBCPKCS7 aes = new AESCBCPKCS7(key,iv);
					byte [] unencrypted = aes.decrypt(contents);
					
					queue.put(unencrypted);
				
					break;
				}
				default: throw new SecurityException("unknown code: "+code);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return 1;
		
	}
	
	/** 
	 * Read a byte off the underlying stream 
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	private final byte readByte(InputStream in)throws IOException {
		int ch1 = in.read();
		return (byte) ch1;
	}
	
	private final int readShort16(InputStream in) throws IOException {
		int ch1 = in.read();
		int ch2 = in.read();
		if ((ch1 | ch2) < 0)
			throw new EOFException();
		return ((ch1 << 8) + (ch2 << 0));
	}

	private final int readInt32(InputStream in) throws IOException {
		int ch1 = in.read();
		int ch2 = in.read();
		int ch3 = in.read();
		int ch4 = in.read();
		if ((ch1 | ch2 | ch3 | ch4) < 0)
			throw new EOFException();
		return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
	}
	
	private final void readFully(InputStream in, byte b[], int off, int len)
			throws IOException {
		if (len < 0)
			throw new IndexOutOfBoundsException();
		int n = 0;
		while (n < len) {
			int count = in.read(b, off + n, len - n);
			if (count < 0)
				throw new EOFException();
			n += count;
		}
	}
	
	private final boolean validateHMac(KeyParameter kp, byte [] hmac, byte [] contents){
		HMac mac = new HMac(digest);
		mac.init(kp);
		mac.update(contents,0,contents.length);
		byte [] result = new byte[hmac.length];
		mac.doFinal(result, 0);
		return Arrays.equals(hmac, result);
	}
	
	private final ParametersWithIV buildKey(byte [] key, byte[] iv) {
		ParametersWithIV holder = new ParametersWithIV(
				new KeyParameter(key, 0, key.length), 
				iv, 
				0, 
				iv.length);
		return holder;
	}

}
