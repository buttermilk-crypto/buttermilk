package com.cryptoregistry.util;

import java.io.StringWriter;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

public class StringToLineFormattingTest {

	@Test
	public void test0() {
		RandomStringGenerator gen = new RandomStringGenerator();
		String data = gen.nextString(247);
		StringWriter writer = new StringWriter();
		JsonFactory f = new JsonFactory();
		JsonGenerator g = null;
		try {
			g = f.createGenerator(writer);
			g.useDefaultPrettyPrinter();
			DefaultPrettyPrinter pp = (DefaultPrettyPrinter) g.getPrettyPrinter();
			pp.indentArraysWith(new Lf2SpacesIndenter());
			
			g.writeObjectFieldStart("Root");
			g.writeArrayFieldStart("KeyData.EncryptedData");
			List<String> list = new StringToList(data).toList();
				for(String s: list){
					g.writeString(s);
				}
			g.writeEndArray();
			g.writeEndObject();
			g.close();
			System.err.println(writer.toString());
			
		}catch(Exception x){
			x.printStackTrace();
		}
	}

}
