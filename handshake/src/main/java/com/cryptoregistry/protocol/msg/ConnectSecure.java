package com.cryptoregistry.protocol.msg;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.cryptoregistry.CryptoKey;
import com.cryptoregistry.CryptoKeyMetadata;
import com.cryptoregistry.KeyGenerationAlgorithm;
import com.cryptoregistry.Verifier;
import com.cryptoregistry.c2.key.Curve25519KeyForPublication;
import com.cryptoregistry.ec.ECKeyForPublication;
import com.cryptoregistry.formats.C2KeyFormatter;
import com.cryptoregistry.formats.ECKeyFormatter;
import com.cryptoregistry.formats.MapDataFormatter;
import com.cryptoregistry.protocol.BTLSData;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

/**
 * <pre>
 * Client sends client CONNECT-SECURE request:
 * 
 * {
 *   "Version" : "BTLS 1.0",
 *   "RegHandle" : "Chinese Knees",
 *   "Data" : {
 *     "Local" : {
 *   	  "Action" : "CONNECT-SECURE",
 *   	  "Handshake" : "ECDH/PeersOnly"
 *   	},
 *   },
 *   "Keys" : {
 *     "abb25bb8-bc55-480e-a910-bd37b2c86c38-P" : {
 *       "KeyAlgorithm" : "Curve25519",
 *       "CreatedOn" : "2015-08-08T08:07:14+0000",
 *       "Encoding" : "Base64url",
 *       "P" : "N2r1UVHfPkMItw2OKnHV3jqR4YAEQ-rTYce5bN10rkM="
 *     }
 *   }
 * }
 * 
 * Server responds with either "OK" and the Server's intended key...
 * 
 * {
 *   "Version" : "BTLS 1.0",
 *   "RegHandle" : "cryptoregistry.com",
 *    "Data" : {
 *     "Local" : {
 *   	  "Response" : "OK",
 *   	},
 *   },
 *   "Keys" : {
 *     "abb25bb8-bc55-480e-a910-bd37b2c86c38-P" : {
 *       "KeyAlgorithm" : "Curve25519",
 *       "CreatedOn" : "2015-08-08T08:07:14+0000",
 *       "Encoding" : "Base64url",
 *       "P" : "N2r1UVHfPkMItw2OKnHV3jqR4YAEQ-rTYce5bN10rkM="
 *     }
 *   }
 * }
 * 
 * ...Or "Rejected" and a human readable error message
 * 
 * {
 *   "Version" : "BTLS 1.0",
 *   "RegHandle" : "cryptoregistry.com",
 *   "Data" : {
 *     "Local" : {
 *   	  "Response" : "Rejected",
 *   	  "ErrorCode" : "2",
 *   	  "ErrorMsg" : "You suck"
 *   	},
 *   },
 * </pre>
 * 
 * @author Dave
 *
 */
public class ConnectSecure {

	public final Map<String, String> attributes;
	public final BTLSData data;
	public final CryptoKey key;

	public ConnectSecure(String handshake, CryptoKey key) {
		super();
		this.attributes = new LinkedHashMap<String, String>();
		this.key = key;
		if (!(key instanceof Verifier)) {
			throw new RuntimeException("Please use a key for publication here");
		}
		data = new BTLSData();
		data.put("Action", "CONNECT-SECURE");
		data.put("Handshake", handshake);
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
			while (inner.hasNext()) {
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

			g.writeObjectFieldStart("Keys");

			final CryptoKeyMetadata meta = key.getMetadata();
			final KeyGenerationAlgorithm alg = meta.getKeyAlgorithm();
			switch (alg) {
			case Curve25519: {
				Curve25519KeyForPublication contents = (Curve25519KeyForPublication) key;
				C2KeyFormatter formatter = new C2KeyFormatter(contents);
				formatter.formatKeys(g, writer);
				break;
			}
			case EC: {
				ECKeyForPublication contents = (ECKeyForPublication) key;
				ECKeyFormatter formatter = new ECKeyFormatter(contents);
				formatter.formatKeys(g, writer);
				break;
			}
			default:
				throw new RuntimeException("alg not recognized: " + alg);
			}

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
