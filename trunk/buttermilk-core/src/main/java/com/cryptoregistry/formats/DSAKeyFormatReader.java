/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2014 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.formats;

import java.math.BigInteger;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import com.cryptoregistry.dsa.DSAKeyContents;
import com.cryptoregistry.dsa.DSAKeyMetadata;
import com.cryptoregistry.util.TimeUtil;

/**
 * This class is used to read the contents of an Armored DSA key which must be in the Unsecure format
 * 
 * @author Dave
 * 
 */
public class DSAKeyFormatReader {

	final Map<String, Object> map;

	public DSAKeyFormatReader(Map<String, Object> map) {
		this.map = map;
	}

	@SuppressWarnings("unchecked")
	public DSAKeyContents read() {
		Iterator<String> iter = map.keySet().iterator();
		if (iter.hasNext()) {
			String distUUID = iter.next();
			Map<String, Object> inner = (Map<String, Object>) map.get(distUUID);
			if (distUUID.endsWith("-U")) {
				Date createdOn = TimeUtil.getISO8601FormatDate(String
						.valueOf(inner.get("CreatedOn")));
				
				EncodingHint enc = EncodingHint.valueOf(String.valueOf(inner.get("Encoding")));
				
				int lengthL = Integer.parseInt(String.valueOf(inner.get("Strength")));
				BigInteger P = FormatUtil.unwrap(enc, String.valueOf(inner.get("P")));
				BigInteger Q = FormatUtil.unwrap(enc, String.valueOf(inner.get("Q")));
				BigInteger G = FormatUtil.unwrap(enc, String.valueOf(inner.get("G")));
				BigInteger X = FormatUtil.unwrap(enc, String.valueOf(inner.get("X")));
				BigInteger Y = FormatUtil.unwrap(enc, String.valueOf(inner.get("Y")));

				DSAKeyMetadata meta = new DSAKeyMetadata(
						distUUID.substring(0, distUUID.length() - 2), 
						createdOn, 
						new KeyFormat(enc, Mode.UNSECURED, null)
				);
				
				meta.setLengthL(lengthL);

				return new DSAKeyContents(meta,P,Q,G,Y,X);

			} else {
				throw new RuntimeException(
						"unexpected Mode, needs to be Unsecure");
			}
		} else {
			throw new RuntimeException("Count not find the uuid, fail");
		}
	}
}
