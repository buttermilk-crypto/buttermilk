package com.cryptoregistry.protocol.module;

import com.cryptoregistry.protocol.BTLSData;
import com.cryptoregistry.protocol.BTLSMessageFactory;
import com.cryptoregistry.protocol.Handshake;
import com.cryptoregistry.protocol.Module;
import com.cryptoregistry.protocol.msg.Status;

public class HandshakeNegotiationModule implements Module {

	Handshake h;
	String protocol;
	
	public HandshakeNegotiationModule(Handshake h, String protocol) {
		this.h = h;
		this.protocol = protocol;
	}

	@Override
	public void run() {
		
		if(h.isClient){
			BTLSData data = new BTLSData();
			data.data.put("Action", "HANDSHAKE-PROTOCOL");
			data.data.put("Requested.Handshake", protocol);
		
			Status query = BTLSMessageFactory.createStatusMsg(h.regHandle, data);
			
		}else{
			// server
			
		}
		
	}

}
