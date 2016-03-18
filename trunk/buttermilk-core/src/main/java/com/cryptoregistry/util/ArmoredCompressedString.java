/*
 *  This file is part of Buttermilk(TM) 
 *  Copyright 2013 David R. Smith for cryptoregistry.com
 *
 */
package com.cryptoregistry.util;

import net.iharder.Base64;

/** <pre>
 *
 * Wrapper for a Base64 URL-safe encoded String (base64url), in this case with 
 * gzip compression added to the encapsulated bytes. 
 * 
 * The idea for this class comes from my reading of
 * http://salmon-protocol.googlecode.com/svn/trunk/draft-panzer-magicsig-01.html
 * 
 * See also http://tools.ietf.org/html/rfc4648#page-7
 *
 * </pre>
 * @author Dave
 *
 */
public class ArmoredCompressedString extends ArmoredString {

	private static final long serialVersionUID = 1L;

	public ArmoredCompressedString(byte[] bytes) {
		super(bytes);
	}

	public ArmoredCompressedString(String encoded) {
		super(encoded);
	}
	
	protected int init() {
		return Base64.URL_SAFE | Base64.GZIP;
	}

}
