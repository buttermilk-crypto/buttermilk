/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2014 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.ntru;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.cryptoregistry.KeyGenerationAlgorithm;
import com.cryptoregistry.Signer;
import com.cryptoregistry.formats.NTRUParametersFormatter;
import com.cryptoregistry.passwords.Password;
import com.cryptoregistry.pbe.PBEParams;
import com.cryptoregistry.util.ArmoredCompressedString;
import com.cryptoregistry.util.ArmoredString;
import com.cryptoregistry.util.ArrayUtil;
import com.cryptoregistry.util.TimeUtil;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import org.bouncycastle.pqc.crypto.ntru.NTRUEncryptionParameters;
import org.bouncycastle.pqc.crypto.ntru.NTRUEncryptionPrivateKeyParameters;
import org.bouncycastle.pqc.math.ntru.polynomial.DenseTernaryPolynomial;
import org.bouncycastle.pqc.math.ntru.polynomial.IntegerPolynomial;
import org.bouncycastle.pqc.math.ntru.polynomial.Polynomial;
import org.bouncycastle.pqc.math.ntru.polynomial.ProductFormPolynomial;
import org.bouncycastle.pqc.math.ntru.polynomial.SparseTernaryPolynomial;


public class NTRUKeyContents extends NTRUKeyForPublication implements Signer{
	
	public final Polynomial t;
	public final IntegerPolynomial fp;

	public NTRUKeyContents(NTRUEncryptionParameters params, 
			IntegerPolynomial h, Polynomial t, IntegerPolynomial fp) {
		super(params, h);
		this.t = t;
		this.fp = fp;
	}
	
	public NTRUKeyContents(NTRUNamedParameters e, 
			IntegerPolynomial h, Polynomial t, IntegerPolynomial fp) {
		super(e, h);
		this.t = t;
		this.fp = fp;
	}

	public NTRUKeyContents(NTRUKeyMetadata metadata, 
			NTRUEncryptionParameters params, IntegerPolynomial h,
			Polynomial t, IntegerPolynomial fp) {
		super(metadata, params, h);
		this.t = t;
		this.fp = fp;
	}
	
	public NTRUKeyContents(NTRUKeyMetadata metadata, 
			NTRUNamedParameters e, IntegerPolynomial h,
			Polynomial t, IntegerPolynomial fp) {
		super(metadata, e, h);
		this.t = t;
		this.fp = fp;
	}

	public NTRUEncryptionPrivateKeyParameters getPrivateKey() {
		return new NTRUEncryptionPrivateKeyParameters(h,t,fp,params);
	}
	
	public ArmoredCompressedString wrappedFp() {
		return ArrayUtil.wrapAndCompressIntArray(fp.coeffs);
	}
	
	/**
	 * Return a map with a key of td, ts, or tp (for Dense, Sparse or Product Form polynomials)
	 * 
	 * @return
	 */
	
	public Map<String,ArmoredString> wrappedT() {
		
		Map<String,ArmoredString> map = new HashMap<String,ArmoredString>();
		if(t instanceof DenseTernaryPolynomial) {
			DenseTernaryPolynomial poly = (DenseTernaryPolynomial) t;
			map.put("td", ArrayUtil.wrapAndCompressIntArray(poly.coeffs));
		}else if(t instanceof SparseTernaryPolynomial) {
			SparseTernaryPolynomial poly = (SparseTernaryPolynomial) t;
			byte [] polyBytes = poly.toBinary();
			map.put("ts", new ArmoredString(polyBytes));
		}else if(t instanceof ProductFormPolynomial) {
			ProductFormPolynomial poly = (ProductFormPolynomial) t;
			byte [] polyBytes = poly.toBinary();
			map.put("tp", new ArmoredString(polyBytes));
		}
		
		return map;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((fp == null) ? 0 : fp.hashCode());
		result = prime * result + ((t == null) ? 0 : t.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		NTRUKeyContents other = (NTRUKeyContents) obj;
		if (fp == null) {
			if (other.fp != null)
				return false;
		} else if (!fp.equals(other.fp))
			return false;
		if (t == null) {
			if (other.t != null)
				return false;
		} else if (!t.equals(other.t))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "NTRUKeyContents [t=" + t + ", fp=" + fp + "h="+ h+"]";
	}
	
	public NTRUKeyForPublication forPublication(){
		NTRUKeyMetadata meta = this.metadata.cloneForPublication();
		return new NTRUKeyForPublication(meta,this.params,this.h);
	}

	/**
	 * If a password is set in the KeyFormat, clean that out. This call can be made once we're done
	 * with the key materials in this cycle of use. 
	 */
	@Override
	public void scrubPassword() {
		PBEParams params = this.metadata.format.pbeParams;
		if(params != null) {
			Password password = params.getPassword();
			if(password != null && password.isAlive()) password.selfDestruct();
		}
	}
	
	@Override
	public String formatJSON() {
		StringWriter writer = new StringWriter();
		JsonFactory f = new JsonFactory();
		JsonGenerator g = null;
		try {
			g = f.createGenerator(writer);
			g.useDefaultPrettyPrinter();
			g.writeStartObject();
			g.writeObjectFieldStart(metadata.handle+"-U");
			g.writeStringField("KeyAlgorithm", KeyGenerationAlgorithm.NTRU.toString());
			g.writeStringField("CreatedOn", TimeUtil.format(metadata.createdOn));
			//g.writeStringField("Encoding", enc.toString());
			
			g.writeStringField("h", wrappedH().toString());
			
			g.writeStringField("fp", wrappedFp().toString());
			Object obj = wrappedT();
			// product form
			if(obj.getClass().isArray()){
				ArmoredCompressedString [] ar = (ArmoredCompressedString[])obj;
				g.writeStringField("t0", ar[0].toString());
				g.writeStringField("t1", ar[1].toString());
				g.writeStringField("t2", ar[2].toString());
			}else{
				if(params.sparse){
					ArmoredCompressedString ar = (ArmoredCompressedString)obj;
					g.writeStringField("ts", ar.toString());
				}else{
					ArmoredCompressedString ar = (ArmoredCompressedString)obj;
					g.writeStringField("td", ar.toString());
				}
			}
			
			NTRUParametersFormatter pFormat = null;
			if(parameterEnum == null) pFormat = new NTRUParametersFormatter(params);
			else pFormat = new NTRUParametersFormatter(parameterEnum);
			pFormat.format(g, writer);
			
			g.writeEndObject();
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				g.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		return writer.toString();
	}

}
