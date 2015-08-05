package com.cryptoregistry.btls.io;

/**
 * Thrown when something bad happens, like a failed hmac validation
 * 
 * @author Dave
 *
 */
public class SecurityException extends Exception {

	private static final long serialVersionUID = 1L;
	
	String msg;

	public SecurityException() {}

	public SecurityException(String msg) {
		super();
		this.msg = msg;
	}

}
