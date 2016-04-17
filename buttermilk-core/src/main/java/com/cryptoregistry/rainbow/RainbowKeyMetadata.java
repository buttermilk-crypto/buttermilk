package com.cryptoregistry.rainbow;

import java.util.Date;
import java.util.UUID;

import com.cryptoregistry.CryptoKeyMetadata;
import com.cryptoregistry.KeyGenerationAlgorithm;
import com.cryptoregistry.formats.KeyFormat;


public class RainbowKeyMetadata implements CryptoKeyMetadata {

	public final String handle;
	public final Date createdOn;
	public final KeyFormat format;

	public RainbowKeyMetadata(String handle, Date createdOn, KeyFormat format) {
		super();
		this.handle = handle;
		this.createdOn = createdOn;
		this.format = format;
	}
	
	public RainbowKeyMetadata clone() {
		Date d = null;
		if(createdOn != null) d = new Date(createdOn.getTime());
		KeyFormat f = null;
		if(format != null) f = format.clone();
		return new RainbowKeyMetadata(this.handle,d,f);
	}
	
	
	public static RainbowKeyMetadata createUnsecured() {
		return new RainbowKeyMetadata(UUID.randomUUID().toString(), new Date(),KeyFormat.unsecured());
	}
	
	public static RainbowKeyMetadata createUnsecured(String handle) {
		return new RainbowKeyMetadata(handle, new Date(),KeyFormat.unsecured());
	}
	
	public static RainbowKeyMetadata createForPublication() {
		return new RainbowKeyMetadata(UUID.randomUUID().toString(), new Date(), KeyFormat.forPublication());
	}
	
	public static RainbowKeyMetadata createSecurePBKDF2(char[]passwordChars) {
		return new RainbowKeyMetadata(UUID.randomUUID().toString(), new Date(),KeyFormat.securedPBKDF2(passwordChars));
	}
	
	public static RainbowKeyMetadata createSecurePBKDF2(int iterations, char[]passwordChars) {
		return new RainbowKeyMetadata(UUID.randomUUID().toString(), new Date(),KeyFormat.securedPBKDF2(iterations, passwordChars));
	}
	
	public static RainbowKeyMetadata createSecureScrypt(char[]passwordChars) {
		return new RainbowKeyMetadata(UUID.randomUUID().toString(), new Date(),KeyFormat.securedSCRYPT(passwordChars));
	}
	
	public static RainbowKeyMetadata createSecureScrypt(int cpuCost, int parallelization, char[]passwordChars) {
		return new RainbowKeyMetadata(UUID.randomUUID().toString(), new Date(),KeyFormat.securedSCRYPT(cpuCost, parallelization, passwordChars));
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
		return  KeyGenerationAlgorithm.Rainbow;
	}

	@Override
	public Date getCreatedOn() {
		return createdOn;
	}

	@Override
	public KeyFormat getFormat() {
		return format;
	}
	public RainbowKeyMetadata cloneForPublication() {
		return new RainbowKeyMetadata(handle, createdOn, KeyFormat.forPublication());
	}
	
	public RainbowKeyMetadata cloneSecurePBKDF2(char[]passwordChars) {
		return new RainbowKeyMetadata(handle, createdOn, KeyFormat.securedPBKDF2(passwordChars));
	}
	
	public RainbowKeyMetadata cloneSecurePBKDF2(int iters, char[]passwordChars) {
		return new RainbowKeyMetadata(handle, createdOn, KeyFormat.securedPBKDF2(iters, passwordChars));
	}
	
	public RainbowKeyMetadata cloneSecureScrypt(char[]passwordChars) {
		return new RainbowKeyMetadata(handle, createdOn, KeyFormat.securedSCRYPT(passwordChars));
	}
	
	public RainbowKeyMetadata cloneSecureScrypt(int cpuCost, int parallelization, char[]passwordChars) {
		return new RainbowKeyMetadata(handle, createdOn, KeyFormat.securedSCRYPT(cpuCost,parallelization, passwordChars));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((createdOn == null) ? 0 : createdOn.hashCode());
		result = prime * result + ((format == null) ? 0 : format.hashCode());
		result = prime * result + ((handle == null) ? 0 : handle.hashCode());
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
		RainbowKeyMetadata other = (RainbowKeyMetadata) obj;
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
		return true;
	}

	@Override
	public String toString() {
		return "RainbowKeyMetadata [handle=" + handle + ", createdOn=" + createdOn
				+ ", format=" + format + "]";
	}

}
