package com.cryptoregistry.btls.io;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import x.org.bouncycastle.crypto.Digest;
import x.org.bouncycastle.crypto.digests.SHA256Digest;
import x.org.bouncycastle.crypto.engines.AESFastEngine;
import x.org.bouncycastle.crypto.io.CipherOutputStream;
import x.org.bouncycastle.crypto.macs.HMac;
import x.org.bouncycastle.crypto.modes.CBCBlockCipher;
import x.org.bouncycastle.crypto.paddings.PKCS7Padding;
import x.org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import x.org.bouncycastle.crypto.params.KeyParameter;
import x.org.bouncycastle.crypto.params.ParametersWithIV;

import com.cryptoregistry.btls.BTLSProtocol;
import com.cryptoregistry.proto.reader.AuthenticatedStringProtoReader;
import com.cryptoregistry.protos.Frame.AuthenticatedStringProto;

/**
 * <p>Collector runs as a different thread and gathers bytes as they come in from the underlying inputstream 
 * which has been wrapped by a FrameInputStream. The bytes are in the form of frames which must be
 * decoded or decrypted. Using a different thread helps to provide for consumption of bytes (I/O) on
 * the main thread while decryption might be going using the other.</p>
 * 
 * @author Dave
 *
 */
public class Collector implements Runnable {
	
	protected ParametersWithIV params;
	
	final byte [] key;
	final KeyParameter hmacParam;
	final Digest digest;
	final HMac mac;
	protected PaddedBufferedBlockCipher aesCipher;

	final BlockingQueue<byte[]> queue;
	final InputStream in;
	final Set<AlertListener> alertListeners;
	
	boolean running;
	protected Exception exception;
	
	static final int COLLECTOR_PAUSE_MILLISEC = 10;
	static final int SMALL_BUF_SIZE = 1024;
	static final BigDecimal szBd = new BigDecimal(SMALL_BUF_SIZE);
	
	public Collector(FrameEventConsumer frameInputStream) {
		this.queue= frameInputStream.queue();
		this.in = frameInputStream.in();
		this.key = frameInputStream.key();
		this.hmacParam = new KeyParameter(key,0,key.length);
		this.alertListeners = frameInputStream.alertListeners();
		this.digest = new SHA256Digest();
		this.mac = this.createHMac(hmacParam);
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
				e.printStackTrace();
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
				//	hmacParam = new KeyParameter(key,0,key.length);
					
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
					
					// collect the encrypted contents and decrypt as we collect it
					
					ByteArrayOutputStream collectorStream = new ByteArrayOutputStream(contentsSz);
					CBCBlockCipher blockCipher = new CBCBlockCipher(new AESFastEngine());
					aesCipher = new PaddedBufferedBlockCipher(blockCipher, new PKCS7Padding());
					aesCipher.init(false, buildKey(key,iv));
					CipherOutputStream collector = new CipherOutputStream(collectorStream,aesCipher);
					
					this.collect(in, collector, contentsSz);
					byte [] contents = collectorStream.toByteArray();
					
					// third, validate
					int macSz = this.readShort16(in);
					if(macSz == 0) break;// bail if no hmac provided
				
					byte [] hmacBytes = new byte[macSz];
					this.readFully(in, hmacBytes, 0, macSz);
					
					byte [] hmacCollected = new byte[mac.getMacSize()];
					
					mac.doFinal(hmacCollected, 0);
					if(Arrays.equals(hmacBytes, hmacCollected)) {
						// all good
					}else{
						throw new SecurityException("HMac does not match - Application Frame appears to have been tampered with...bailing out.");
					}
					
					queue.put(contents);
				
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
	
	/**
	 * Fill the buf array. Blocks if required
	 * @param in
	 * @param buf
	 * @param off
	 * @param len
	 * @throws IOException
	 */
	private final void readFully(InputStream in, byte [] buf, int off, int len)
			throws IOException {
		if (len < 0)
			throw new IndexOutOfBoundsException();
		int n = 0;
		while (n < len) {
			int count = in.read(buf, off + n, len - n);
			if (count < 0)
				throw new EOFException();
			n += count;
		}
	}
	
	/**
	 * get length bytes from "in" and write those to the output stream called "collector"
	 * 
	 * @param in
	 * @param collector
	 * @param length
	 * @throws IOException
	 */
	private final void collect(InputStream in, OutputStream collector, int length)
			throws IOException {
		
		// if length is small then just fill one buffer. If length is large, run a loop to fill the buffer repeatedly
		if(length<=SMALL_BUF_SIZE){
			byte [] buf = new byte[length];
			readFully(in, buf,0,length);
			collector.write(buf,0,buf.length);
			collector.flush();
			collector.close(); //needed to call doFinal
			mac.update(buf, 0, buf.length);
		}else{
			
			BigDecimal szLength = new BigDecimal(length);
			BigDecimal [] result = szLength.divideAndRemainder(szBd);
			int loopCount = result[0].intValue();
			int rem = result[1].intValue();
			byte [] buf = new byte[SMALL_BUF_SIZE];
			
			// loop for loopCount iterations
			for(int i = 0;i<loopCount;i++){
				readFully(in, buf,0,SMALL_BUF_SIZE);
				collector.write(buf,0,buf.length);
				mac.update(buf, 0, buf.length);
			}
			// now collect the remainder
			buf = new byte[rem];
			readFully(in, buf,0,rem);
			collector.write(buf,0,buf.length);
			mac.update(buf, 0, buf.length);
			
			collector.flush();
			collector.close();
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
	
	
	private final HMac createHMac(KeyParameter kp){
		HMac mac = new HMac(digest);
		mac.init(kp);
		return mac;
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
