package com.cryptoregistry.utility.app;

import org.junit.Test;

public class BiEntropyTest {

	@Test
	public void test0() {
		
		String [] empty = {};
		BiEntropyApp.main(empty);
		
		String [] argsZ = {"bientropy", "-i", "0", "-a", "none", "-f", "json"};
		BiEntropyApp.main(argsZ);
		
		String [] args0 = {"bientropy", "-i", "word", "-a", "none", "-f", "json"};
		BiEntropyApp.main(args0);
		
		String [] argsZcsv = {"bientropy", "-i", "0", "-a", "none", "-f", "csv"};
		BiEntropyApp.main(argsZcsv);
		
		String [] args0csv = {"bientropy", "-i", "word", "-a", "none", "-f", "csv"};
		BiEntropyApp.main(args0csv);
		
		String [] args1 = {"bientropy", "-i", "wordwordword", "-a", "none", "-f", "csv"};
		BiEntropyApp.main(args1);
		
		String [] args2 = {"rand", "-l", "32", "-a", "base64url"};
		BiEntropyApp.main(args2);
		
		String [] args3 = {"rand", "-l", "64", "-a", "hex"};
		BiEntropyApp.main(args3);
		
		String [] args4 = {"rand", "-l", "64", "-a", "base16", "-c", "10"};
		BiEntropyApp.main(args4);
	}
	

}
