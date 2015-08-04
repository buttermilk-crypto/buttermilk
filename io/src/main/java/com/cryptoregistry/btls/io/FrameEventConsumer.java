package com.cryptoregistry.btls.io;

import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * Interface for FrameInputStream to make interacting with the Collector a bit easier
 * 
 * @author Dave
 *
 */
public interface FrameEventConsumer {

	// things that a Collector needs from the caller
	public byte [] key();
	public BlockingQueue<byte[]> queue();
	public Set<AlertListener> alertListeners();
	public InputStream in();
	
}
