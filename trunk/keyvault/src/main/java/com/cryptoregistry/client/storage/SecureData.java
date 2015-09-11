/*
 *  This file is part of Buttermilk(TM) 
 *  Copyright 2013 David R. Smith for cryptoregistry.com
 *
 */
package com.cryptoregistry.client.storage;

import java.io.Serializable;
import java.util.Arrays;

/**
 * The secure tuple has a byte array for the encrypted data and its own IV per record. The metadata parallels this
 * and provides a description of it
 *  
 * @author Dave
 *
 */
public class SecureData implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private byte [] data;
	private byte [] iv;
	
	private String datatype; //hint to help identify that the serialized form is, should be a className

	public SecureData(byte[] data, byte[] iv, String datatype) {
		this();
		this.data = data;
		this.iv = iv;
		this.datatype = datatype;
	}
	
	public SecureData(byte[] data, byte[] iv) {
		this();
		this.data = data;
		this.iv = iv;
		this.datatype = "String";
	}

	public SecureData() {
		super();
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public byte[] getIv() {
		return iv;
	}

	public void setIv(byte[] iv) {
		this.iv = iv;
	}

	public String getDatatype() {
		return datatype;
	}

	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}

	@Override
	public String toString() {
		return "SecureData [data=" + Arrays.toString(data) + ", iv="
				+ Arrays.toString(iv) + ", datatype=" + datatype + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(data);
		result = prime * result + Arrays.hashCode(iv);
		result = prime * result
				+ ((datatype == null) ? 0 : datatype.hashCode());
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
		SecureData other = (SecureData) obj;
		if (!Arrays.equals(data, other.data))
			return false;
		if (!Arrays.equals(iv, other.iv))
			return false;
		if (datatype == null) {
			if (other.datatype != null)
				return false;
		} else if (!datatype.equals(other.datatype))
			return false;
		return true;
	}

}
