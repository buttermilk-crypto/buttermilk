/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2014 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.formats;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.cryptoregistry.CryptoKey;
import com.cryptoregistry.CryptoKeyMetadata;
import com.cryptoregistry.CryptoContact;
import com.cryptoregistry.KeyGenerationAlgorithm;
import com.cryptoregistry.MapData;
import com.cryptoregistry.ListData;
import com.cryptoregistry.Version;
import com.cryptoregistry.c2.key.Curve25519KeyForPublication;
import com.cryptoregistry.ec.ECKeyForPublication;
import com.cryptoregistry.ntru.jneo.JNEOKeyForPublication;
import com.cryptoregistry.rsa.RSAKeyForPublication;
import com.cryptoregistry.signature.CryptoSignature;
import com.cryptoregistry.symmetric.SymmetricKeyContents;
import com.cryptoregistry.util.Lf2SpacesIndenter;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

/**
 * A builder which will generate the canonical data structure for a buttermilk format JSON wrapper 
 *    
 * @author Dave
 *
 */
public class JSONFormatter {

	protected String version;
	protected String registrationHandle;
	protected String email;
	protected List<CryptoKey> keys;
	protected List<CryptoContact> contacts;
	protected List<CryptoSignature> signatures;
	protected List<MapData> mapData;
	protected List<ListData> listData;
	protected List<MapData> macs;
	
	protected boolean includeEmpty;
	
	public JSONFormatter() {
		this("");
	}
	
	public JSONFormatter(String handle) {
		version = Version.OVERALL_VERSION;
		this.registrationHandle = handle;
		this.email = "";
		keys = new ArrayList<CryptoKey>();
		contacts = new ArrayList<CryptoContact>();
		signatures = new ArrayList<CryptoSignature>();
		mapData = new ArrayList<MapData>();
		listData = new ArrayList<ListData>();
		macs = new ArrayList<MapData>();
	}
	
	public JSONFormatter(String handle, String email) {
		version = Version.OVERALL_VERSION;
		this.registrationHandle = handle;
		this.email = email;
		keys = new ArrayList<CryptoKey>();
		contacts = new ArrayList<CryptoContact>();
		signatures = new ArrayList<CryptoSignature>();
		mapData = new ArrayList<MapData>();
		listData = new ArrayList<ListData>();
		macs = new ArrayList<MapData>();
	}

	public JSONFormatter(String version, 
			String registrationHandle, 
			String email,
			List<CryptoKey> keys, 
			List<CryptoContact> contacts,
			List<CryptoSignature> signatures, 
			List<MapData> mapData, 
			List<ListData> listData, 
			List<MapData> macs) {
		super();
		this.version = version;
		this.registrationHandle = registrationHandle;
		this.email = email;
		this.keys = keys;
		this.contacts = contacts;
		this.signatures = signatures;
		this.mapData = mapData;
		this.listData = listData;
		this.macs = macs;
	}
	
	public JSONFormatter add(CryptoContact e) {
		 contacts.add(e);
		 return this;
	}

	public JSONFormatter addContacts(Collection<? extends CryptoContact> c) {
		contacts.addAll(c);
		return this;
	}
	
	public JSONFormatter add(CryptoKey e) {
		if(keys.contains(e)) throw new RuntimeException("Sorry, this exact key already present, it must be added exactly once");
		keys.add(e);
		return this;
	}
	
	public JSONFormatter addKey(CryptoKey e) {
		keys.add(e);
		return this;
	}

	public JSONFormatter addKeys(Collection<? extends CryptoKey> c) {
		keys.addAll(c);
		return this;
	}
	
	public JSONFormatter add(CryptoSignature e) {
		signatures.add(e);
		return this;
	}

	public JSONFormatter addSignatures(Collection<? extends CryptoSignature> c) {
		signatures.addAll(c);
		return this;
	}
	
	public JSONFormatter add(MapData e) {
		mapData.add(e);
		return this;
	}

	public JSONFormatter addLocalData(Collection<? extends MapData> c) {
		mapData.addAll(c);
		return this;
	}
	
	public JSONFormatter add(ListData e) {
		listData.add(e);
		return this;
	}

	public JSONFormatter addRemoteData(Collection<? extends ListData> c) {
		listData.addAll(c);
		return this;
	}

	public void format(Writer writer){
		format(writer, true);
	}
	
	public void format(Writer writer, boolean prettyPrint) {
		JsonFactory f = new JsonFactory();
		JsonGenerator g = null;
		try {
			g = f.createGenerator(writer);
			if(prettyPrint) {
				g.useDefaultPrettyPrinter();
				DefaultPrettyPrinter pp = (DefaultPrettyPrinter) g.getPrettyPrinter();
				pp.indentArraysWith(new Lf2SpacesIndenter());
			}
			
			g.writeStartObject();
			g.writeStringField("Version", Version.OVERALL_VERSION);
			g.writeStringField("RegHandle", registrationHandle);
			g.writeStringField("Email", email);
			
			if(contacts.size()> 0) {
				
				g.writeObjectFieldStart("Contacts");
				
				ContactFormatter cf = new ContactFormatter(contacts);
				cf.format(g, writer);
				
				g.writeEndObject();
			}else{
				if(this.includeEmpty){
					g.writeObjectFieldStart("Contacts");
					g.writeEndObject();
				}
			}
			
			if(mapData.size()>0 || listData.size()>0){
				
				g.writeObjectFieldStart("Data");
				
				if(mapData.size()>0){
					
					g.writeObjectFieldStart("Local");
					
						MapDataFormatter ldf = new MapDataFormatter(mapData);
						ldf.format(g, writer);
					
					g.writeEndObject();
				}
				
				
				if(listData.size()>0){
					
					g.writeArrayFieldStart("Remote");
					
						ListDataFormatter rdf = new ListDataFormatter(listData);
						rdf.format(g, writer);
				
					g.writeEndArray();
				}
				
				g.writeEndObject();
				
			}else{
				if(this.includeEmpty){
					g.writeObjectFieldStart("Data");
						g.writeObjectFieldStart("Local");
						g.writeEndObject();
					g.writeEndObject();
				}
			}
			
			if(keys.size()> 0) {
				
				g.writeObjectFieldStart("Keys");
				
				// TODO allow public keys to work
				for(CryptoKey key: keys){
					final CryptoKeyMetadata meta = key.getMetadata();
					final KeyGenerationAlgorithm alg = meta.getKeyAlgorithm();
					switch(alg){
						case Symmetric: {
							SymmetricKeyContents contents = (SymmetricKeyContents)key;
							SymmetricKeyFormatter formatter = new SymmetricKeyFormatter(contents);
							formatter.formatKeys(g, writer);
							break;
						}
						case Curve25519: {
							Curve25519KeyForPublication contents = (Curve25519KeyForPublication)key;
							C2KeyFormatter formatter = new C2KeyFormatter(contents);
							formatter.formatKeys(g, writer);
							break;
						}
						case EC: {
							ECKeyForPublication contents = (ECKeyForPublication)key;
							ECKeyFormatter formatter = new ECKeyFormatter(contents);
							formatter.formatKeys(g, writer);
							break;
						}
						case JNEO: {
							JNEOKeyForPublication contents = (JNEOKeyForPublication)key;
							JNEOKeyFormatter formatter = new JNEOKeyFormatter(contents);
							formatter.formatKeys(g, writer);
							break;
						}
						case RSA: {
							RSAKeyForPublication contents = (RSAKeyForPublication)key;
							RSAKeyFormatter formatter = new RSAKeyFormatter(contents);
							formatter.formatKeys(g, writer);
							break;
						}
						default: throw new RuntimeException("alg not recognized: "+alg);
					}
				}
				
				g.writeEndObject();
			}else{
				if(this.includeEmpty){
					g.writeObjectFieldStart("Keys");
					g.writeEndObject();
				}
			}
			
			if(macs.size()>0){
				
				g.writeObjectFieldStart("Macs");
				
					MapDataFormatter ldf = new MapDataFormatter(macs);
					ldf.format(g, writer);
				
				g.writeEndObject();
			}else{
				if(this.includeEmpty){
					g.writeObjectFieldStart("Macs");
					g.writeEndObject();
				}
			}
			
			if(signatures.size()> 0) {
				
				g.writeObjectFieldStart("Signatures");
				
				SignatureFormatter sf = new SignatureFormatter(signatures);
				sf.format(g, writer);
				
				g.writeEndObject();
			}else{
				if(this.includeEmpty){
					g.writeObjectFieldStart("Signatures");
					g.writeEndObject();
				}
			}
			
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
	}

	public void setRegistrationHandle(String registrationHandle) {
		this.registrationHandle = registrationHandle;
	}

	public boolean isIncludeEmpty() {
		return includeEmpty;
	}

	public void setIncludeEmpty(boolean includeEmpty) {
		this.includeEmpty = includeEmpty;
	}

}
