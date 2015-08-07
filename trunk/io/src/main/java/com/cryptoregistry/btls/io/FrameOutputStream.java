/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2014 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.btls.io;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

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
import com.cryptoregistry.protocol.frame.AlertOutputFrame;
import com.cryptoregistry.protocol.frame.OutputFrame;

/**
 * FrameOutputStream is a FilterOutputStream with frame-writing capability in addition to the normal
 * "business methods" of an output stream.
 * 
 * 
 * @author Dave
 *
 */
public class FrameOutputStream extends FilterOutputStream {
	
	protected Logger logger = Logger.getLogger(this.getClass().getName());
	protected ByteArrayOutputStream buffer;
	protected CipherOutputStream cOut;
	protected byte [] key;
	protected ParametersWithIV params;
	protected KeyParameter macParam;
	protected Digest digest;
	protected PaddedBufferedBlockCipher aesCipher;
	protected SecureRandom rand; //initialized if required
	
	// how many bytes have we written to the cOut stream?
	int count = 0;
	
	protected Lock lock = new ReentrantLock();
	
	// if true each frame will use a unique IV. This requires creating a cipher per frame but is very secure
	protected boolean IVPerFrame = false;
	
	/**
	 * Generates a random IV and as a side effect initializes rand. 
	 * This IV is written into the application message frame header so each message has a unique IV
	 * 
	 * @param out
	 * @param key
	 */
	public FrameOutputStream(OutputStream out, byte [] key) {
		super(out);
		lock.lock();
		try {
			buffer = new ByteArrayOutputStream();
			digest = new SHA256Digest();
			this.key = key;
			// done here for testing purposes
			params = buildKeyWithRandomIV(key);
			macParam = buildKey(key);
			CBCBlockCipher blockCipher = new CBCBlockCipher(new AESFastEngine());
			aesCipher = new PaddedBufferedBlockCipher(blockCipher, new PKCS7Padding());
			aesCipher.init(true, params);
			cOut = new CipherOutputStream(buffer,aesCipher);
		}finally {
			lock.unlock();
		}
	}

	/**
	 * Used mainly for testing - supply both key and IV directly. Warning: Do not hard-code IVs!
	 * 
	 * @param out
	 * @param key
	 * @param iv
	 */
	public FrameOutputStream(OutputStream out, byte [] key, byte [] iv) {
		super(out);
		lock.lock();
		try {
			buffer = new ByteArrayOutputStream();
			digest = new SHA256Digest();
			
			// done here for testing purposes
			params = buildKeyWithIV(key,iv);
			macParam = buildKey(key);
			CBCBlockCipher blockCipher = new CBCBlockCipher(new AESFastEngine());
			aesCipher = new PaddedBufferedBlockCipher(blockCipher, new PKCS7Padding());
			aesCipher.init(true, params);
			cOut = new CipherOutputStream(buffer,aesCipher);
		}finally {
			lock.unlock();
		}
	}
	
	/**
	 * General constructor - key acquired via handshake and set using one of the buildKey() methods
	 * 
	 * @param out
	 */
	public FrameOutputStream(OutputStream out) {
		super(out);
		buffer = new ByteArrayOutputStream();
		digest = new SHA256Digest();
	}
	
	/**
	 * Send a frame as data on the encrypted stream. This is using the frame concept on
	 * top of the encrypted channel.
	 * 
	 * @param frame
	 * @throws IOException
	 */
	public void writeFrameContents(OutputFrame frame) throws IOException{
		lock.lock();
		try {
			
			byte [] bytes = frame.outputFrameContents();
			if(buffer.size()+bytes.length >= Integer.MAX_VALUE) {
				flush(); // force the creation of a new frame
			}
			cOut.write(bytes);
			count+=bytes.length;
		}finally{
			lock.unlock();
		}
	}
	
	/**
	 * Alerts are not encrypted, but we do authenticate them with a HMac
	 * 
	 * @param subcode
	 * @param msg
	 */
	public void writeAlert(int subcode,String msg){
		lock.lock();
		try {
			HMac mac = new HMac(digest);
			mac.init(macParam);
			byte[] msgBytes = null;
			try {
				msgBytes = msg.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {}
			mac.update(msgBytes, 0, msgBytes.length);
			byte [] hmac = new byte[digest.getDigestSize()];
			mac.doFinal(hmac, 0);
			AlertOutputFrame frame = new AlertOutputFrame(BTLSProtocol.ALERT, subcode, msg, hmac);
			frame.writeFrame(out);
			digest.reset();
		}finally{
			lock.unlock();
		}
	}
	
	/**
	 * Alerts are not encrypted but we authenticate with an HMac
	 * 
	 * @param msg
	 */
	public void writeInformationalAlert(String msg){
		writeAlert(BTLSProtocol.INFORMATION, msg);
	}
	
	protected void writeShort(OutputStream out, int v) throws IOException {
		lock.lock();
		try {
			short s = (short) v;
			out.write((s >>>  8) & 0xFF);
			out.write((s >>>  0) & 0xFF);
		}finally{
			lock.unlock();
		}
    }
	
	protected void writeInt(OutputStream out, int v) throws IOException {
		lock.lock();
		try {
			out.write((v >>> 24) & 0xFF);
			out.write((v >>> 16) & 0xFF);
			out.write((v >>>  8) & 0xFF);
			out.write((v >>>  0) & 0xFF);
		}finally{
			lock.unlock();
		}
    }
	
	/**
	 * General stream interface - Write a confidential byte
	 */
	public void write(int in) throws IOException{
		lock.lock();
		try {
			if(buffer.size() >= Integer.MAX_VALUE) {
				flush(); // force the completion of the current application frame and creation of a new one
			}
			cOut.write(in);
			count++;
		}finally {
			lock.unlock();
		}
	}
	
	/**
	 * General stream interface - write a confidential byte array. 
	 * Note that if the byte array is of zero length, it is not processed and is ignored.
	 */
	public void write(byte [] in) throws IOException{
		lock.lock();
		try {
			// do nothing if empty
			if(in == null || in.length == 0) return;
			
			if(buffer.size()+in.length >= Integer.MAX_VALUE) {
				flush(); // force the creation of a new frame
			}
			cOut.write(in);
			count+=in.length;
		}finally{
			lock.unlock();
		}
	}
	
	/**
	 * General stream interface - write a confidential byte array. 
	 * Note that if the byte array is of zero length, it is not processed and is ignored.
	 */
	public void write(byte[] in, int offset, int length) throws IOException {
		lock.lock();
		try {
			// do nothing if empty
			if(in == null || in.length == 0) return;
			if(buffer.size()+length >= Integer.MAX_VALUE) {
				flush(); // force the creation of a new frame
			}
			cOut.write(in);  
			count+=length;
		}finally{
			lock.unlock();
		}
	}
	
	/**
	 * IMPORTANT: Controls the underlying processing on the internal buffer. Calling flush() 
	 * forces the CipherOutputStream to call close(), which in turn does a doFinal() and 
	 * completes the encryption for that message. We will then generate a frame with an 
	 * Application contentType and send it.
	 * 
	 * @throws IOException 
	 */
	public void flush() throws IOException {
		lock.lock();
		try {
			
			// flush only possible if we had something written to us. Thus, flush() has no effect of nothing has been written.
			if(count == 0) {
				return;
			}
			
			cOut.flush();
			cOut.close(); // complete any encryption and finalize
			
			// get the encrypted bytes
			byte [] payload = buffer.toByteArray();
			
			// make a mac of the encrypted bytes
			HMac mac = new HMac(digest);
			mac.init(macParam);
			mac.update(payload, 0, payload.length);
			byte [] hmac = new byte[digest.getDigestSize()];
			mac.doFinal(hmac, 0);
			
			// start writing application msg frame to the underlying stream
			// application content type
			out.write(BTLSProtocol.APPLICATION);
			
			// first write the iv size and iv bytes
			// the size is allowed two bytes so the max iv length must fit in a WORD
			writeShort(out, params.getIV().length);
			out.write(params.getIV(), 0, params.getIV().length);
			
			// write the length of the payload and then the encrypted payload itself
			writeInt(out,payload.length);
			out.write(payload);
			
			// last of all write the hmac size and then the bytes
			writeShort(out, hmac.length);
			out.write(hmac, 0, hmac.length);
			
			out.flush();
			
			// frame complete, reset and cleanup locally
			
			if(IVPerFrame){
				// build a new cipher with a new random IV - more expensive than reset()
				params = this.buildKeyWithRandomIV(key);
				CBCBlockCipher blockCipher = new CBCBlockCipher(new AESFastEngine());
				aesCipher = new PaddedBufferedBlockCipher(blockCipher, new PKCS7Padding());
				aesCipher.init(true, params);
				cOut = new CipherOutputStream(buffer,aesCipher);
				digest.reset();
			}else{
				// keep using the same IV
				buffer = new ByteArrayOutputStream();
				aesCipher.reset();
				cOut = new CipherOutputStream(buffer,aesCipher);
				digest.reset();
			}
			
			count = 0;
			
		}finally{
			lock.unlock();
		}
	}
	
	protected ParametersWithIV buildKeyWithRandomIV(byte [] key) {
		if(rand != null) rand = new SecureRandom();
		byte [] iv = new byte[16];
		rand.nextBytes(iv);
		ParametersWithIV holder = new ParametersWithIV(
				new KeyParameter(key, 0, key.length), 
				iv, 
				0, 
				iv.length);
		return holder;
	}
	
	protected ParametersWithIV buildKeyWithIV(byte [] key, byte [] iv) {
		ParametersWithIV holder = new ParametersWithIV(
				new KeyParameter(key, 0, key.length), 
				iv, 
				0, 
				iv.length);
		return holder;
	}
	
	protected KeyParameter buildKey(byte [] key) {
			return new KeyParameter(key, 0, key.length);	
	}

	public void setIVPerFrame(boolean iVPerFrame) {
		IVPerFrame = iVPerFrame;
	}

	/**
	 * Return the number of bytes written so far via the write() methods. This does
	 * not take into account flushing (is not a measure of what was sent). It only
	 * measures what has been fed in
	 * 
	 * @return
	 */
	public int getCount() {
		return count;
	}
	
	

}
