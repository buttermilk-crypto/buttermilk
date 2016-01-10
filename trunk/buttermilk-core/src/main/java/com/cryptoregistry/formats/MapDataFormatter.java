/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2014 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.formats;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.cryptoregistry.MapData;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;

public class MapDataFormatter {

	private List<MapData> mapData;

	public MapDataFormatter() {
		super();
		mapData = new ArrayList<MapData>();
	}

	public MapDataFormatter(List<MapData> mapData) {
		super();
		this.mapData = mapData;
	}
	
	public void add(MapData ld){
		mapData.add(ld);
	}

	public void format(JsonGenerator g, Writer writer) throws JsonGenerationException, IOException{
		Iterator<MapData>iter = mapData.iterator();
		while(iter.hasNext()){
			MapData c = iter.next();
			g.writeObjectFieldStart(c.uuid);
			Iterator<String> inner = c.data.keySet().iterator();
			while(inner.hasNext()){
				String key = inner.next();
				g.writeStringField(key, String.valueOf(c.data.get(key)));
			}
			g.writeEndObject();
		}
	}
	
	/**
	 * format an output like this:
	 * 
	 * "uuid0": {
	 * 		"Key0": "Item0",
	 *		"Key1: "Item1"
	 * },
	 * "uuid1": {
	 * 		"Key0": "Item0",
	 *		"Key1: "Item1"
	 * }
	 * ...
	 * 
	 * @return
	 */
	public String formatAsFragment(){
		StringBuffer buf = new StringBuffer();
		int overallCount = 1;
		for(MapData data: mapData){
			buf.append(quote(data.uuid));
			buf.append(": {\n");
			int size = data.data.size();
			int count = 1;
			Iterator<String> iter = data.data.keySet().iterator();
			while(iter.hasNext()){
				String key = iter.next();
				String value = data.data.get(key);
				buf.append("          ");
				if(count < size) buf.append(keyValuePair(key, value, true));
				else buf.append(keyValuePair(key, value, false));
				count++;
			}
			if(overallCount<mapData.size()) buf.append("},");
			else  buf.append("     }");
			overallCount++;
		}
		return buf.toString();
	}
	
	private String quote(String in){
		StringBuffer buf = new StringBuffer();
		buf.append(QUOTE);
		buf.append(in);
		buf.append(QUOTE);
		return buf.toString();
	}
	
	private String keyValuePair(String key, String value, boolean comma){
		StringBuffer buf = new StringBuffer();
		buf.append(quote(key));
		buf.append(":");
		buf.append(quote(value));
		if(comma) buf.append(",");
		buf.append("\n");
		return buf.toString();
	}
	
	private static final char QUOTE = '"';
	
}
