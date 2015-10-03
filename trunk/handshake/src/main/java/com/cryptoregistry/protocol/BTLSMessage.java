package com.cryptoregistry.protocol;

/**
 * Uniform Interface for getting BTLS message-related data during a handshake from the messages exchanged.
 * 
 * @author Dave
 *
 */
public interface BTLSMessage {

	// for BTLS
	String action(); // verb such as "CONNECT-SECURE", or "STATUS"
	String handshake(); // handshake code
	
	// for error handling or status
	int statusCode();
	String statusMsg();
	String details();
}
