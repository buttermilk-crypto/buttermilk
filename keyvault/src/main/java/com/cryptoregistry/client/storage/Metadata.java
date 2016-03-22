/*
 *  This file is part of Buttermilk(TM) 
 *  Copyright 2013 David R. Smith for cryptoregistry.com
 *
 */
package com.cryptoregistry.client.storage;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.function.BiConsumer;


/**
 * used for describing the encrypted data 
 * 
 * @author Dave
 *
 */
public class Metadata implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String handle;
	private Map<String,String> attributes; // this is a map describing what is encrypted.

	private boolean ignore; // you can mark a record as out of scope - if true, it will not be returned by queries
	private boolean ephemeral; // if true, intended to be an ephemeral record, will be removed via a background process
	private long timestamp; // record creation time
	
	public Metadata(String handle) {
		super();
		this.handle = handle;
		this.timestamp = new Date().getTime();
	}

	public String getHandle() {
		return handle;
	}

	public boolean isIgnore() {
		return ignore;
	}

	public void setIgnore(boolean ignore) {
		this.ignore = ignore;
	}

	public boolean isEphemeral() {
		return ephemeral;
	}

	public void setEphemeral(boolean ephemeral) {
		this.ephemeral = ephemeral;
	}

	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * For example, map.forEach((k, v) -> System.out.println(k + "=" + v));
     *
	 * @param action
	 */
	public void forEach(BiConsumer<? super String, ? super String> action) {
		attributes.forEach(action);
	}

	public String get(Object key) {
		return attributes.get(key);
	}
	
	

}
