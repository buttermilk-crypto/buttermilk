package com.cryptoregistry.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.junit.Test;

import com.cryptoregistry.MapData;

public class ParseAPITest {
	

	@Test
	public void test0() {
		try (InputStream in = this.getClass().getResourceAsStream(
				"/believed-good-c2.json");
				JsonParser parser = Json.createParser(in)) {
			@SuppressWarnings("unused")
			List<MapData> list = new ArrayList<MapData>();
			
		//	while (parser.hasNext()) {
		//		Event e = parser.next();
		//		if(e == Event.KEY_NAME){
		//			System.err.println(parser.getString());
		//		}
		//	}
			
			while (parser.hasNext()) {
				Event e = parser.next();
				if (e == Event.START_OBJECT) {
					System.err.println("start");
				}else if(e == Event.END_OBJECT){
					System.err.println("end");
				}else if(e == Event.KEY_NAME){
					String key = parser.getString();
					System.err.println(key);
				}else if(e == Event.VALUE_STRING){
					String value = parser.getString();
					System.err.println(value);
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
