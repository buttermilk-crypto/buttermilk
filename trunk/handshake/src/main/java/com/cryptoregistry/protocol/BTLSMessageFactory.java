package com.cryptoregistry.protocol;


import com.cryptoregistry.CryptoKey;
import com.cryptoregistry.MapData;

public class BTLSMessageFactory {
	
	public static final ConnectSecure createConnectSecureReq(String handshake, String handle, CryptoKey key) {
		ConnectSecure sec = new ConnectSecure(key);
		sec.attributes.put("Version", "BTLS 1.0");
	//	sec.data.put("MessageId", UUID.randomUUID().toString());
		//sec.data.put("Timestamp", TimeUtil.now());
		sec.attributes.put("Request", "CONNECT-SECURE");
		sec.attributes.put("HandshakeProtocol", String.valueOf(handshake));
		sec.attributes.put("RegHandle", handle);
		
		return sec;
	}
	
	public static final ConnectSecure createConnectSecureResp(String handle, CryptoKey key) {
		ConnectSecure sec = new ConnectSecure(key);
		sec.attributes.put("Version", "BTLS 1.0");
	//	sec.data.put("MessageId", UUID.randomUUID().toString());
	//	sec.data.put("Timestamp", TimeUtil.now());
		sec.attributes.put("Response", "OK");
	//	sec.data.put("HandshakeProtocol", String.valueOf(handshake));
		sec.attributes.put("RegHandle", handle);
		
		return sec;
	}
	
	public static final ConfirmData createConfirmDataReq(String handle, String action, MapData data) {
		ConfirmData enc = new ConfirmData(data);
		enc.put("Version", "BTLS 1.0");
		enc.put("Request", action);
		enc.put("RegHandle", handle);
		return enc;
	}
	

}
