/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2014 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.btls.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cryptoregistry.btls.BTLSProtocol;
import com.cryptoregistry.protocol.frame.StringFrameReader;

/**
 * FrameInputStream parses the BTLs Protocol frames coming over the underlying InputStream. A
 * Collector is used to do some of this work on a different thread. What it does is to unpackage
 * byte arrays from the frames and push them into a blocking queue. The FrameInputStream then has
 * the simple task of pulling a unified stream of bytes from the byte arrays it sequentially
 * consumes out of the BlockingQueue. Currently the BlockingQueue is an ArrayBlockingQueue with
 * a 1024 size. This is not a byte capacity but a byte array capacity. It basically means the
 * Collector can read ahead by 1024 frames prior to blocking. Because the collector is doing the
 * hard work of decryption, it is conjectured that this capacity will rarely be met if the 
 * FrameInputStream is being steadily drained. 
 * 
 * FrameInputStream provides the normal read() input methods as well as some frame reading methods. 
 * 
 * "Frames" are essentially Google ProtocolBuffers prepended with a type code and a length as
 * a header. See the frames package for more details.
 * 
 * @author Dave
 * @see Collector
 *
 */
public class FrameInputStream extends FilterInputStream implements FrameEventConsumer {
	
	static final Logger logger = LogManager.getLogger(FrameInputStream.class.getName());
	
	private byte [] key; // encryption key
	
	private ArrayBlockingQueue<byte[]> queue;
	private Collector collector;
	
	private byte [] currentSource;
	private int index = 0;
	
	final int POLLING_TIMEOUT_SECS = 30;
	
	private Set<AlertListener> alertListeners;
	
	private Thread collectorThread;

	public FrameInputStream(InputStream in,byte [] key) {
		super(in);
		queue = new ArrayBlockingQueue<byte[]>(1024);
		this.key = key;
		this.alertListeners = new HashSet<AlertListener>();
		collector = new Collector(this);
	}
	
	public void start(){
		collectorThread = new Thread(collector);
		collectorThread.start();
	}
	
	/**
	 * For this call to work the FrameOutputStream must have actually sent a StringOutputFrame:
	 * 
	 * byte0 = BTLSProtocol.STRING
	 * byte1-5 = integer length of the StringProto data
	 * remainder = StringProto data
	 *  
	 * @return
	 */
	public String readStringFrame() {
		StringFrameReader sr = new StringFrameReader(BTLSProtocol.STRING);
		return sr.read(this);
	}

	/**
	 * General stream business method - Read a byte off the decrypted underlying buffer, return -1 if depleted
	 * 
	 */
	public int read() throws IOException {
		
		// initial condition
		if(currentSource == null){
			try {
				currentSource = queue.poll(POLLING_TIMEOUT_SECS, TimeUnit.SECONDS);
				index=0;
			} catch (InterruptedException e) {
				e.printStackTrace();
				return -1;
			}
			
			if(currentSource == null) return -1;
			if(currentSource.length == 0) return -1;
		}
		
		// edge condition - if true, end of current source has been reached and we need to check for more
		if(index >= currentSource.length){
			// collector is shut down, we must have hit -1
			if(!this.collector.running) return -1;
			try {
				currentSource = queue.poll(POLLING_TIMEOUT_SECS, TimeUnit.SECONDS);
				index=0;
			} catch (InterruptedException e) {
				e.printStackTrace();
				return -1;
			}
		}
		
		if(currentSource == null) return -1;
		if(currentSource.length == 0) return -1;
	
		// typical condition, just read from currentSource
		int b = currentSource[index];
		index++;
		return b;
	}
	
	/**
	 * General stream business method - read bytes into a byte buffer, return how many bytes read, return -1 if depleted
	 * 
	 */
	public int read(byte[] buffer) throws IOException {
		// initial condition
		if(currentSource == null){
			try {
				currentSource = queue.poll(POLLING_TIMEOUT_SECS, TimeUnit.SECONDS);
				index=0;
			} catch (InterruptedException e) {
				e.printStackTrace();
				return -1;
			}
			
			if(currentSource == null) return -1;
			if(currentSource.length == 0) return -1;
		}
		
		// edge condition - if true, end of current source has been reached and we need to check for more
		if(index >= currentSource.length){
			// collector is shut down, we must have hit -1
			if(!this.collector.running) return -1;
			try {
				currentSource = queue.poll(POLLING_TIMEOUT_SECS, TimeUnit.SECONDS);
				index=0;
			} catch (InterruptedException e) {
				e.printStackTrace();
				return -1;
			}
		}
		
		if(currentSource == null) return -1;
		if(currentSource.length == 0) return -1;
	
		// typical condition, just read from currentSource
		
		// this is the case where we have more than required available bytes to fill buffer
		if(this.available() >=buffer.length){
			System.arraycopy(currentSource, index, buffer, 0, buffer.length);
			index += buffer.length;
			return buffer.length;
		}else{
			// this is the case where we only partially fill buffer
			int available = this.available();
			System.arraycopy(currentSource, index, buffer, 0, available);
			index += available;
			return available;
		}
		
	}

	/**
	 * General stream business method - read a byte [] off the decrypted underlying buffer, -1 if depleted
	 * 
	 */
	public int read(byte[] buffer, int offset, int length) throws IOException {
		// initial condition
		if(currentSource == null){
			try {
				currentSource = queue.poll(POLLING_TIMEOUT_SECS, TimeUnit.SECONDS);
				index=0;
			} catch (InterruptedException e) {
				e.printStackTrace();
				return -1;
			}
			
			if(currentSource == null) return -1;
			if(currentSource.length == 0) return -1;
		}
		
		// edge condition - if true, end of current source has been reached and we need to check for more
		if(index >= currentSource.length){
			// collector is shut down, we must have hit -1
			if(!this.collector.running) return -1;
			try {
				currentSource = queue.poll(POLLING_TIMEOUT_SECS, TimeUnit.SECONDS);
				index=0;
			} catch (InterruptedException e) {
				e.printStackTrace();
				return -1;
			}
		}
		
		if(currentSource == null) return -1;
		if(currentSource.length == 0) return -1;
	
		// typical condition, just read from currentSource
		
		// this is the case where we have more than required available bytes to fill the required portion of buffer
		if(this.available() >=length){
			System.arraycopy(currentSource, index, buffer, offset, length);
			index += length;
			return length;
		}else{
			// this is the case where we only can partially fill the required portion of buffer
			int available = this.available();
			System.arraycopy(currentSource, index, buffer, offset, available);
			index += available;
			return available;
		}
	}
	
	/**
	 * Return what is in the current source byte array. 
	 */
	@Override
	public int available() throws IOException {
		if(currentSource == null || currentSource.length==0) return 0;
		return currentSource.length-index;
	}
	
	@Override
	public void close() throws IOException {
		super.close();
	}
	
	/**
	 * Application clients interested in receiving alerts register with this method
	 * 
	 * @param listener
	 */
	public void addAlertListener(AlertListener listener){
		this.alertListeners.add(listener);
	}
	
	// Implement the FrameEventConsumer Interface
	// this interface exists to make the Collector more reusable

	@Override
	public byte[] key() {
		return key;
	}

	@Override
	public BlockingQueue<byte[]> queue() {
		return queue;
	}

	@Override
	public Set<AlertListener> alertListeners() {
		return alertListeners;
	}

	@Override
	public InputStream in() {
		return in;
	}
	
}
