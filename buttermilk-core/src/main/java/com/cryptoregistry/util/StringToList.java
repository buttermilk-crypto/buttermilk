package com.cryptoregistry.util;

import java.util.ArrayList;
import java.util.List;

public class StringToList {

	final String input;
	final int length;
	
	public StringToList(String in) {
		input = in;
		length = 72;
	}
	
	public StringToList(int length, String in) {
		input = in;
		this.length = length;
	}
	
	public List<String> toList() {
		List<String> list = new ArrayList<String>();
		if(input.length() <= length) {
			list.add(input);
			return list;
		}
		int lineCount = (input.length() / length);
		int charCount = 0;
		for(int i = 0;i<lineCount;i++){
			int start = charCount;
			int end = start+length;
			String substring = input.substring(start, end);
			list.add(substring);
			charCount+=length;
		}
		String last = input.substring(charCount, input.length());
		list.add(last);
		return list;
	}

}
