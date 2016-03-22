/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2014 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.formats;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import com.cryptoregistry.ntru.jneo.FullPolynomialDecoder;
import com.cryptoregistry.ntru.jneo.JNEOKeyContents;
import com.cryptoregistry.ntru.jneo.JNEOKeyMetadata;
import com.cryptoregistry.ntru.jneo.JNEONamedParameters;
import com.cryptoregistry.util.ListToString;
import com.cryptoregistry.util.TimeUtil;
import com.securityinnovation.jneo.math.FullPolynomial;

/**
 * This class is used to read the contents of a JNEO NTRU key. the key must be in the Unsecured mode
 
 * @author Dave
 * 
 */
public class JNEOKeyFormatReader {

	final Map<String, Object> map;

	public JNEOKeyFormatReader(Map<String, Object> map) {
		this.map = map;
	}

	@SuppressWarnings("unchecked")
	public JNEOKeyContents read() {
		Iterator<String> iter = map.keySet().iterator();
		if (iter.hasNext()) {
			String distUUID = iter.next();
			Map<String, Object> inner = (Map<String, Object>) map.get(distUUID);
			if (distUUID.endsWith("-U")) {
				Date createdOn = TimeUtil.getISO8601FormatDate(String
						.valueOf(inner.get("CreatedOn")));
				
				EncodingHint enc = EncodingHint.valueOf(String.valueOf(inner.get("Encoding")));
				JNEOKeyMetadata meta = new JNEOKeyMetadata(distUUID, createdOn, new KeyFormat(enc, Mode.UNSECURED, null));
				JNEONamedParameters param = JNEONamedParameters.valueOf((String)inner.get("ParameterSet"));
				
				ListToString lts = new ListToString(inner);
				FullPolynomialDecoder decoderH = new FullPolynomialDecoder(lts.collectListData("h"));
				FullPolynomialDecoder decoderF = new FullPolynomialDecoder(lts.collectListData("f"));
				FullPolynomial h = decoderH.decode();
				FullPolynomial f = decoderF.decode();
				
				return new JNEOKeyContents(meta, param, h, f);
			} else {
				throw new RuntimeException(
						"unexpected Mode, needs to be Unsecure");
			}
		} else {
			throw new RuntimeException("Count not find the uuid, fail");
		}
	}
}
