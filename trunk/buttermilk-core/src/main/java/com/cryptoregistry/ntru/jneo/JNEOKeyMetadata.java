/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.ntru.jneo;

import java.util.Date;
import java.util.UUID;

import com.cryptoregistry.CryptoKeyMetadata;
import com.cryptoregistry.KeyGenerationAlgorithm;
import com.cryptoregistry.formats.EncodingHint;
import com.cryptoregistry.formats.KeyFormat;
import com.cryptoregistry.formats.Mode;
import com.cryptoregistry.pbe.PBEParams;
import com.cryptoregistry.pbe.PBEParamsFactory;

public class JNEOKeyMetadata implements CryptoKeyMetadata {
	
	public final String handle;
	public final Date createdOn;
	public final KeyFormat format;

	public JNEOKeyMetadata(String handle, Date createdOn, KeyFormat format) {
		this.handle = handle;
		this.createdOn = createdOn;
		this.format = format;
	}
	
	/**
	 * Returns a default UUID-based handle, createdOn, and KeyFormat for EncodingHint.NoEncoding, Mode.UNSECURED
	 * @return
	 */
	public static JNEOKeyMetadata createDefault() {
		return new JNEOKeyMetadata(UUID.randomUUID().toString(), new Date(),new KeyFormat(EncodingHint.NoEncoding,Mode.UNSECURED,null));
	}
	
	public static JNEOKeyMetadata createForPublication() {
		return new JNEOKeyMetadata(UUID.randomUUID().toString(), new Date(),new KeyFormat(EncodingHint.NoEncoding,Mode.REQUEST_FOR_PUBLICATION,null));
	}
	
	public static JNEOKeyMetadata createSecureDefault(char[]passwordChars) {
		return new JNEOKeyMetadata(UUID.randomUUID().toString(), new Date(),
				new KeyFormat(EncodingHint.NoEncoding, 
						Mode.REQUEST_SECURE, 
						PBEParamsFactory.INSTANCE.createPBKDF2Params(passwordChars)));
	}
	
	public static JNEOKeyMetadata createSecureScrypt(char[]password) {
		return new JNEOKeyMetadata(UUID.randomUUID().toString(), new Date(),
				new KeyFormat(EncodingHint.NoEncoding,
						Mode.REQUEST_SECURE, 
						PBEParamsFactory.INSTANCE.createScryptParams(password)));
	}
	
	public static JNEOKeyMetadata createSecure(PBEParams params) {
		return new JNEOKeyMetadata(UUID.randomUUID().toString(), new Date(),
				new KeyFormat(EncodingHint.NoEncoding,
						Mode.REQUEST_SECURE, 
						params));
	}
	
	@Override
	public String getHandle() {
		return handle;
	}
	
	public String getDistinguishedHandle() {
		return handle+"-"+format.mode.code;
	}

	@Override
	public KeyGenerationAlgorithm getKeyAlgorithm() {
		return KeyGenerationAlgorithm.JNEO;
	}

	@Override
	public Date getCreatedOn() {
		return createdOn;
	}

	@Override
	public KeyFormat getFormat() {
		return format;
	}

}
