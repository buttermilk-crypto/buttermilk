/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith. All Rights Reserved.
 *
 */
package com.cryptoregistry.dsa;

import java.util.Date;
import java.util.UUID;

import com.cryptoregistry.CryptoKeyMetadata;
import com.cryptoregistry.KeyGenerationAlgorithm;
import com.cryptoregistry.formats.EncodingHint;
import com.cryptoregistry.formats.KeyFormat;
import com.cryptoregistry.formats.Mode;
import com.cryptoregistry.pbe.PBEParams;

public class DSAKeyMetadata implements CryptoKeyMetadata {

	public final String handle;
	public final Date createdOn;
	public final KeyFormat format;
	
	// these are set on creation of the key from the key generation params
	public int lengthL, lengthN; // requested strength on creation - e.g., 3078, 256 bits

	public static final int DESIRABLE_STRENGTH_L = 3078; 
	public static final int DESIRABLE_STRENGTH_N = 256; 
	
	public DSAKeyMetadata(String handle, Date createdOn, KeyFormat format) {
		super();
		this.handle = handle;
		this.createdOn = createdOn;
		this.format = format;
	}
	
	public DSAKeyMetadata(String handle, Date createdOn, KeyFormat format, int strength, int L, int N) {
		super();
		this.handle = handle;
		this.createdOn = createdOn;
		this.format = format;
		this.lengthL=L;
		this.lengthN=N;
	}

	/**
	 * Returns a default handle, createOn, and KeyFormat for base64Encode, Mode.OPEN
	 * @return
	 */
	public static DSAKeyMetadata createDefault() {
		return new DSAKeyMetadata(UUID.randomUUID().toString(), new Date(),KeyFormat.unsecured());
	}
	
	public static DSAKeyMetadata createDefault(String handle) {
		return new DSAKeyMetadata(handle, new Date(), KeyFormat.unsecured());
	}
	
	public static DSAKeyMetadata createForPublication() {
		return new DSAKeyMetadata(UUID.randomUUID().toString(), new Date(), KeyFormat.forPublication());
	}
	
	public static DSAKeyMetadata createSecurePBKDF2(char[]passwordChars) {
		return new DSAKeyMetadata(UUID.randomUUID().toString(), new Date(),
				KeyFormat.securedPBKDF2(passwordChars));
	}
	
	public static DSAKeyMetadata createSecureScrypt(char[]passwordChars) {
		return new DSAKeyMetadata(UUID.randomUUID().toString(), new Date(),
				KeyFormat.securedSCRYPT(passwordChars));
	}
	
	public static DSAKeyMetadata createSecure(PBEParams params) {
		return new DSAKeyMetadata(UUID.randomUUID().toString(), new Date(),
				new KeyFormat(EncodingHint.Base64url,Mode.REQUEST_SECURE, params));
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
		return KeyGenerationAlgorithm.DSA;
	}

	@Override
	public Date getCreatedOn() {
		return createdOn;
	}

	@Override
	public KeyFormat getFormat() {
		return format;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((createdOn == null) ? 0 : createdOn.hashCode());
		result = prime * result + ((format == null) ? 0 : format.hashCode());
		result = prime * result + ((handle == null) ? 0 : handle.hashCode());
		result = prime * result + lengthL;
		result = prime * result + lengthN;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DSAKeyMetadata other = (DSAKeyMetadata) obj;
		if (createdOn == null) {
			if (other.createdOn != null)
				return false;
		} else if (!createdOn.equals(other.createdOn))
			return false;
		if (format == null) {
			if (other.format != null)
				return false;
		} else if (!format.equals(other.format))
			return false;
		if (handle == null) {
			if (other.handle != null)
				return false;
		} else if (!handle.equals(other.handle))
			return false;
		if (lengthL != other.lengthL)
			return false;
		if (lengthN != other.lengthN)
			return false;
		return true;
	}

	public int getLengthL() {
		return lengthL;
	}

	public void setLengthL(int lengthL) {
		this.lengthL = lengthL;
	}

	public int getLengthN() {
		return lengthN;
	}

	public void setLengthN(int lengthN) {
		this.lengthN = lengthN;
	}

	public DSAKeyMetadata cloneForPublication() {
		DSAKeyMetadata m = new DSAKeyMetadata(handle, createdOn, KeyFormat.forPublication());
		m.setLengthL(this.lengthL);
		m.setLengthN(this.lengthN);
		return m;
	}

	

}
