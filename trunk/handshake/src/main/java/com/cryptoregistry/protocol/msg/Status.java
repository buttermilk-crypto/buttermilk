package com.cryptoregistry.protocol.msg;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.cryptoregistry.formats.MapDataFormatter;
import com.cryptoregistry.protocol.BTLSData;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

/**
 * {
  "Version" : "bTLS 1.0",
  "RegHandle" : "Chinese Eyes",
   "Data" : {
      "Local" : {
        "3cfd0ec2-c667-440b-b90b-44203060a54f" : {
          "Action" : "Status",
          "ErrorCode" : "2",
          "ErrorMsg" : "You suck",
          "Details" : "No details required"
        }
      }
  }
}
 * @author Dave
 *
 */
public class Status {

	// just for the header
	final Map<String,String> attributes;
	// specific data embedded here
	final BTLSData data;
	
	public Status(String version, String regHandle, int statusCode, String statusMsg, String details) {
		super();
		attributes = new LinkedHashMap<String,String>();
		attributes.put("Version", version);
		attributes.put("RegHandle", regHandle);
		
		data = new BTLSData();
		data.put("StatusCode", String.valueOf(statusCode));
		data.put("StatusMsg", statusMsg);
		
		if(details != null) data.put("Details", details);
	
	}
	
	public Status(String version, String regHandle, BTLSData data) {
		super();
		attributes = new LinkedHashMap<String,String>();
		attributes.put("Version", version);
		attributes.put("RegHandle", regHandle);
		this.data = data;
	}
	
	public Status(String regHandle, int statusCode, String statusMsg) {
		this("bTLS 1.0",regHandle,statusCode,statusMsg, null);
	}
	
	public String formatJSON() {
		StringWriter writer = new StringWriter();
		JsonFactory f = new JsonFactory();
		JsonGenerator g = null;
		try {
			g = f.createGenerator(writer);
			g.useDefaultPrettyPrinter();
			g.writeStartObject();
			Iterator<String> inner = attributes.keySet().iterator();
			while(inner.hasNext()){
				String key = inner.next();
				g.writeStringField(key, attributes.get(key));
			}
				
			g.writeObjectFieldStart("Data");
				g.writeObjectFieldStart("Local");
				  MapDataFormatter ldf = new MapDataFormatter();
				  ldf.add(data);
				  ldf.format(g, writer);
				g.writeEndObject();
			  g.writeEndObject();
			g.writeEndObject();
		} catch (IOException x) {
			throw new RuntimeException(x);
		} finally {
			try {
				if (g != null)
					g.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
			
		return writer.toString();
	}

}
