/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.ntru.jneo;

import com.securityinnovation.jneo.OID;
import com.securityinnovation.jneo.ntruencrypt.KeyParams;

public enum JNEONamedParameters {
	
	EES401EP1(KeyParams.getKeyParams(OID.ees401ep1)),
	EES449EP1(KeyParams.getKeyParams(OID.ees449ep1)),
	EES677EP1(KeyParams.getKeyParams(OID.ees677ep1)),
	EES1087EP2(KeyParams.getKeyParams(OID.ees1087ep2)),
	EES541EP1(KeyParams.getKeyParams(OID.ees541ep1)),
	EES613EP1(KeyParams.getKeyParams(OID.ees613ep1)),
	EES887EP1(KeyParams.getKeyParams(OID.ees887ep1)),
	EES1171EP1(KeyParams.getKeyParams(OID.ees1171ep1)),
	EES659EP1(KeyParams.getKeyParams(OID.ees659ep1)),
	EES761EP1(KeyParams.getKeyParams(OID.ees761ep1)),
	EES1087EP1(KeyParams.getKeyParams(OID.ees1087ep1)),
	EES1499EP1(KeyParams.getKeyParams(OID.ees1499ep1));
	
	public final KeyParams params;
	
	private JNEONamedParameters(KeyParams params){
		this.params = params;
	}
}
