package com.cryptoregistry.protocol;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.cryptoregistry.MapData;
import com.cryptoregistry.formats.MapDataFormatter;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

/**
<pre>
Client sends client CONFIRM-ENCRYPT request:

{
  "Version" : "bTLS 1.0",
  "Request" : "CONFIRM-ENCRYPT",
  "HandshakeNumber" : "1" 
  "RegHandle" : "Chinese Eyes",
   "Data" : {
      "Local" : {
        "3cfd0ec2-c667-440b-b90b-44203060a54e" : {
          "Sample.Data" : "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
          "Sample.Data.Encrypted" : "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
          "Sample.Size" : "32",
          "Sample.Encoding" : "Base64url",
          "EncryptionAlg" : "AES/CBC/PKCS7Padding"
        }
      }
  }
}


</pre>
 * @author Dave
 *
 */
public class ConfirmData {
	
	final Map<String,String> attributes;
	final MapData data;
	
	public ConfirmData(MapData data) {
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
