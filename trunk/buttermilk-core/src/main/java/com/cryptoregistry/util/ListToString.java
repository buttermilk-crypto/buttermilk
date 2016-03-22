package com.cryptoregistry.util;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

public class ListToString {

	final Map<String,Object> keyData;
	
	public ListToString(Map<String,Object> keyData) {
		this.keyData = keyData;
	}
	
	/**
	 * Works for Strings or Lists as the values in the map
	 * 
	 * @param key
	 * @param keyData
	 * @return
	 */
	public String collectListData(String key){
		Object obj = keyData.get(key);
		if(obj instanceof String) return (String) obj;
		else if(obj instanceof List){
			StringWriter writer = new StringWriter();
			@SuppressWarnings("rawtypes")
			List list = (List) keyData.get(key); 
			for(Object o: list){
				writer.write(String.valueOf(o));
			}
			return writer.toString();
		}
		return null;
	}

}
