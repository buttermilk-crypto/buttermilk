package com.cryptoregistry.ntru.jneo;

import java.util.Date;
import java.util.UUID;

import com.cryptoregistry.formats.EncodingHint;
import com.cryptoregistry.formats.KeyFormat;
import com.cryptoregistry.formats.Mode;
import com.cryptoregistry.ntru.NTRUKeyMetadata;
import com.cryptoregistry.pbe.PBEParams;
import com.cryptoregistry.pbe.PBEParamsFactory;

public class JNEOKeyMetadata extends NTRUKeyMetadata {

	public JNEOKeyMetadata(String handle, Date createdOn, KeyFormat format) {
		super(handle, createdOn, format);
	}
	
	/**
	 * Returns a default handle, createOn, and KeyFormat for base64Encode, Mode.OPEN
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

}
