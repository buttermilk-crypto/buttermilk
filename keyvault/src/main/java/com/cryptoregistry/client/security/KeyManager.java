/*
 *  This file is part of Buttermilk(TM) 
 *  Copyright 2013-2015 David R. Smith for cryptoregistry.com
 *
 */
package com.cryptoregistry.client.security;

import com.cryptoregistry.passwords.SensitiveBytes;

public interface KeyManager {

	SensitiveBytes loadKey();
	
}
