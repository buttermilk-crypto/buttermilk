package com.cryptoregistry.protocol;

import java.util.Map;

import com.cryptoregistry.MapData;

/**
 * BTLSData is a type of local message data used in the BTLS protocol
 *  
 * @author Dave
 *
 */
public class BTLSData extends MapData implements BTLSMessage {

	public BTLSData() {
		super();
	}

	public BTLSData(String uuid) {
		super(uuid);
	}

	public BTLSData(String uuid, Map<String, String> in) {
		super(uuid, in);
	}

	@Override
	public String action() {
		if(!data.containsKey("Action")){
			return "";
		}else{
			return data.get("Action");
		}
	}
	
	@Override
	public String handshake() {
		if(!data.containsKey("HandshakeNumber")){
			return "";
		}else{
			return data.get("HandshakeNumber");
		}
	}
	
	/**
	 * Return -1 if not defined
	 */
	@Override
	public int statusCode() {
		if(!data.containsKey("ErrorCode")){
			return -1;
		}else{
			return Integer.parseInt(String.valueOf(data.get("ErrorCode")));
		}
	}

	@Override
	public String statusMsg() {
		if(!data.containsKey("ErrorMsg")){
			return "";
		}else{
			return String.valueOf(data.get("ErrorMsg"));
		}
	}

	@Override
	public String details() {
		if(!data.containsKey("Details")){
			return "";
		}else{
		return String.valueOf(data.get("Details"));
		}
	}

}
