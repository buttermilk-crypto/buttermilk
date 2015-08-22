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
<pre>
Client sends server CONFIRM-ENCRYPT request:

{
  "Version" : "bTLS 1.0",
  "RegHandle" : "Chinese Eyes",
   "Data" : {
      "Local" : {
        "3cfd0ec2-c667-440b-b90b-44203060a54e" : {
          "Action" : "CONFIRM-ENCRYPT",
          "Sample.Data" : "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
          "Sample.Data.Encrypted" : "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
          "Sample.Encoding" : "Base64url",
          "EncryptionAlg" : "AES/CBC/PKCS7Padding"
        }
      }
  }
}

Server checks data. 


</pre>
 * @author Dave
 *
 */
public class ConfirmData {
	
	// just for the header
	final Map<String,String> attributes;
	// specific data embedded here
	final BTLSData data;
	
	public ConfirmData(BTLSData data) {
		super();
		this.attributes = new LinkedHashMap<String,String>();
		this.data = data;
	}
	
	public String put(String key, String value) {
		return attributes.put(key, value);
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
