package com.cryptoregistry.protocol;

/**
 * Uniform Interface for getting BTLS message-related data
 * 
 * @author Dave
 *
 */
public interface BTLSMessage {

	// for BTLS
	String action(); // verb such as "CONNECT-SECURE", or "STATUS"
	String handshake(); 
	
	// for error handling or status
	int statusCode();
	String statusMsg();
	String details();
}
