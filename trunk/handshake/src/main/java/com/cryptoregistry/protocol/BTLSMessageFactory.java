package com.cryptoregistry.protocol;


import com.cryptoregistry.CryptoKey;
import com.cryptoregistry.protocol.msg.ConfirmData;
import com.cryptoregistry.protocol.msg.ConnectSecure;
import com.cryptoregistry.protocol.msg.Status;

public class BTLSMessageFactory {
	
	public static final ConnectSecure createConnectSecureReq(String handshakeConstant, String handle, CryptoKey key) {
		ConnectSecure sec = new ConnectSecure(handshakeConstant, key);
		sec.attributes.put("Version", "BTLS 1.0");
		sec.attributes.put("RegHandle", handle);
		return sec;
	}
	
	public static final ConfirmData createConfirmDataReq(String handle, BTLSData data) {
		ConfirmData enc = new ConfirmData(data);
		enc.put("Version", "BTLS 1.0");
		enc.put("RegHandle", handle);
		return enc;
	}
	
	public static final Status createStatusMsg(String regHandle, int statusCode, String statusMsg){
		return new Status(regHandle, statusCode, statusMsg);
	}
	
	public static final Status createStatusMsg(String regHandle, BTLSData data){
		return new Status("BTLS v1.0", regHandle, data);
	}
	
}
