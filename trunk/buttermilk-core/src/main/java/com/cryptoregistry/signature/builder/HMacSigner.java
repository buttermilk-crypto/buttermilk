package com.cryptoregistry.signature.builder;

import java.util.List;

import x.org.bouncycastle.crypto.macs.HMac;

import com.cryptoregistry.MapData;
import com.cryptoregistry.formats.JSONGenericReader;

public class HMacSigner {

	public HMacSigner(JSONGenericReader reader, HMac mac) {
		
		List<MapData> contacts = reader.contacts();
		List<MapData> keys = reader.keys();
		List<MapData> data = reader.local();
		List<MapData> sigs = reader.signatures();
		if(contacts.size()>0){
			
		}
		
	}

}
