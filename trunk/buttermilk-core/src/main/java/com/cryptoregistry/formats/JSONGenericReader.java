package com.cryptoregistry.formats;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.iharder.Base64;
import x.org.bouncycastle.crypto.digests.SHA256Digest;
import x.org.bouncycastle.crypto.macs.HMac;
import x.org.bouncycastle.crypto.params.KeyParameter;
import x.org.bouncycastle.util.Arrays;

import com.cryptoregistry.MapData;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * In some cases we need just to get the JSON data into Maps, we do not need to load it into value objects.
 * 
 * @author Dave
 *
 */

public class JSONGenericReader {
	
	public final Map<String,Object> map;
	
	JSONGenericReader(Map<String,Object> map){
		this.map = map;
	}

	@SuppressWarnings("unchecked")
	public JSONGenericReader(File path) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			map = mapper.readValue(path, Map.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public JSONGenericReader(Reader in) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			map = mapper.readValue(in, Map.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public String version() {
		return String.valueOf(map.get("Version"));
	}
	
	public String regHandle() {
		return String.valueOf(map.get("RegHandle"));
	}
	
	public String email() {
		return String.valueOf(map.get("Email"));
	}
	
	@SuppressWarnings("unchecked")
	public List<MapData> contacts() {
		List<MapData> list = new ArrayList<MapData>();
		Map<String, Object> uuids = (Map<String, Object>) map.get("Contacts");
		if(uuids == null) return list;
		Iterator<String> iter = uuids.keySet().iterator();
		while(iter.hasNext()) {
			String id = iter.next();
			Map<String, Object> keyData = (Map<String, Object>) uuids.get(id);
			MapData md = new MapData(id);
			Iterator<String> inner = keyData.keySet().iterator();
			while(inner.hasNext()){
				String key = inner.next();
				md.put(key, String.valueOf(keyData.get(key)));
			}
			list.add(md);
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<MapData> keys() {
		List<MapData> list = new ArrayList<MapData>();
		Map<String, Object> uuids = (Map<String, Object>) map.get("Keys");
		if(uuids == null) return list;
		Iterator<String> iter = uuids.keySet().iterator();
		while(iter.hasNext()) {
			String distinguishedKey = iter.next();
			Map<String, Object> keyData = (Map<String, Object>) uuids.get(distinguishedKey);
			MapData md = new MapData(distinguishedKey);
			Iterator<String> inner = keyData.keySet().iterator();
			while(inner.hasNext()){
				String key = inner.next();
				md.put(key, String.valueOf(keyData.get(key)));
			}
			list.add(md);
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<MapData> local() {
		List<MapData> list = new ArrayList<MapData>();
		Map<String, Object> data = (Map<String, Object>) map.get("Data");
		if(data == null) return list;
		Map<String, Object> uuids = (Map<String, Object>) data.get("Local");
		if(uuids == null) return list;
		Iterator<String> iter = uuids.keySet().iterator();
		while(iter.hasNext()) {
			String id = iter.next();
			Map<String, Object> keyData = (Map<String, Object>) uuids.get(id);
			MapData md = new MapData(id);
			Iterator<String> inner = keyData.keySet().iterator();
			while(inner.hasNext()){
				String key = inner.next();
				md.put(key, String.valueOf(keyData.get(key)));
			}
			list.add(md);
		}
		return list;
	}
	
	public void clearExistingMacs() {
		@SuppressWarnings("unchecked")
		Map<String, Object> uuids = (Map<String, Object>) map.get("Macs");
		if(uuids != null){
			uuids.clear();
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<MapData> macs() {
		
		ArrayList<MapData> list = new ArrayList<MapData>();
		
		Map<String, Object> uuids = (Map<String, Object>) map.get("Macs");
		
		// check for empty here, bail if none
		if(uuids == null) return list;
		Iterator<String> iter = uuids.keySet().iterator();
		while(iter.hasNext()) {
			String handle = iter.next();
			Map<String, String> localData = (Map<String, String>) uuids.get(handle);
			list.add(new MapData(handle,localData));
		}
			
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<MapData> signatures() {
		List<MapData> list = new ArrayList<MapData>();
		Map<String, Object> uuids = (Map<String, Object>) map.get("Signatures");
		if(uuids == null) return list;
		Iterator<String> iter = uuids.keySet().iterator();
		while(iter.hasNext()) {
			String id = iter.next();
			Map<String, Object> keyData = (Map<String, Object>) uuids.get(id);
			MapData md = new MapData(id);
			Iterator<String> inner = keyData.keySet().iterator();
			while(inner.hasNext()){
				String key = inner.next();
				md.put(key, String.valueOf(keyData.get(key)));
			}
			list.add(md);
		}
		return list;
	}
	
	public boolean hmacValidate(byte [] key, byte [] macValue){
		byte [] result = hmac(key);
		return Arrays.areEqual(result, macValue);
	}
	
	public void embedHMac(byte [] key) {
		byte [] result = hmac(key);
		try {
			String macValue = Base64.encodeBytes(result, Base64.URL_SAFE);
			MapData data = new MapData();
			data.put("HMac.data", macValue);
			@SuppressWarnings("unchecked")
			Map<String, Object> uuids = (Map<String, Object>) map.get("Macs");
			if(uuids == null){
				uuids = new LinkedHashMap<String,Object>();
				map.put("Macs", uuids);
			}
			uuids.put(data.uuid, data.data);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Does not iterate Macs, since that is where we might store the output of the calculation
	 * 
	 * @param key
	 * @return
	 */
	public byte [] hmac(byte [] key) {
		
		byte[] resBuf = null;
		try {
			SHA256Digest digest = new SHA256Digest();
			HMac hmac=new HMac(digest);
			resBuf=new byte[hmac.getMacSize()];
			hmac.init(new KeyParameter(key));
			
			byte [] bytes = version().getBytes(StandardCharsets.UTF_8);
			hmac.update(bytes, 0, bytes.length);
			bytes = regHandle().getBytes(StandardCharsets.UTF_8);
			hmac.update(bytes, 0, bytes.length);
			bytes = email().getBytes(StandardCharsets.UTF_8);
			hmac.update(bytes, 0, bytes.length);
			
			List<MapData> list = contacts();
			if(list.size() > 0){
				for(MapData _item: list){
					byte [] uuid = _item.uuid.getBytes(StandardCharsets.UTF_8);
					hmac.update(uuid, 0, uuid.length);
					Iterator<String> iter = _item.data.keySet().iterator();
					while(iter.hasNext()){
						String _key = iter.next();
						String value = _item.data.get(_key);
						bytes = _key.getBytes(StandardCharsets.UTF_8);
						hmac.update(bytes, 0, bytes.length);
						bytes = value.getBytes(StandardCharsets.UTF_8);
						hmac.update(bytes, 0, bytes.length);
					}
				}
			}
			
			list = keys();
			if(list.size() > 0){
				for(MapData _item: list){
					byte [] uuid = _item.uuid.getBytes(StandardCharsets.UTF_8);
					hmac.update(uuid, 0, uuid.length);
					Iterator<String> iter = _item.data.keySet().iterator();
					while(iter.hasNext()){
						String _key = iter.next();
						String value = _item.data.get(_key);
						bytes = _key.getBytes(StandardCharsets.UTF_8);
						hmac.update(bytes, 0, bytes.length);
						bytes = value.getBytes(StandardCharsets.UTF_8);
						hmac.update(bytes, 0, bytes.length);
					}
				}
			}
			
			list = local();
			if(list.size() > 0){
				for(MapData _item: list){
					byte [] uuid = _item.uuid.getBytes(StandardCharsets.UTF_8);
					hmac.update(uuid, 0, uuid.length);
					Iterator<String> iter = _item.data.keySet().iterator();
					while(iter.hasNext()){
						String _key = iter.next();
						String value = _item.data.get(_key);
						bytes = _key.getBytes(StandardCharsets.UTF_8);
						hmac.update(bytes, 0, bytes.length);
						bytes = value.getBytes(StandardCharsets.UTF_8);
						hmac.update(bytes, 0, bytes.length);
					}
				}
			}
			
			list = signatures();
			if(list.size() > 0){
				for(MapData _item: list){
					byte [] uuid = _item.uuid.getBytes(StandardCharsets.UTF_8);
					hmac.update(uuid, 0, uuid.length);
					Iterator<String> iter = _item.data.keySet().iterator();
					while(iter.hasNext()){
						String _key = iter.next();
						String value = _item.data.get(_key);
						bytes = _key.getBytes(StandardCharsets.UTF_8);
						hmac.update(bytes, 0, bytes.length);
						bytes = value.getBytes(StandardCharsets.UTF_8);
						hmac.update(bytes, 0, bytes.length);
					}
				}
			}
			
			hmac.doFinal(resBuf,0);
		}catch(Exception x){
			x.printStackTrace();
		}
			return resBuf;
	}
	
	/**
	 * Useful re-format
	 * 
	 * @param writer
	 */
	public void reformat(Writer writer){
		ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.writeValue(writer, this.map);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// TODO - add MapData addition interface
}
