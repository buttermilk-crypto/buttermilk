package com.cryptoregistry.protocol;

public class HandshakeFailedException extends Exception {

	private static final long serialVersionUID = 1L;

	public HandshakeFailedException() {
		super();
	}

	public HandshakeFailedException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public HandshakeFailedException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public HandshakeFailedException(String arg0) {
		super(arg0);
	}

	public HandshakeFailedException(Throwable arg0) {
		super(arg0);
	}
	
	

}
