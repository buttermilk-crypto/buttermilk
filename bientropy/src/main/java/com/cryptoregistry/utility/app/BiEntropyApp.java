/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2014 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.utility.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.iharder.Base64;
import x.org.bouncycastle.util.encoders.Hex;

import com.cryptoregistry.util.CmdLineParser;
import com.cryptoregistry.util.CmdLineParser.Option;
import com.cryptoregistry.util.CmdLineParser.OptionException;
import com.cryptoregistry.util.FileUtil;
import com.cryptoregistry.util.ShowBits;
import com.cryptoregistry.util.ShowHelpUtil;
import com.cryptoregistry.util.entropy.ShannonEntropy;
import com.cryptoregistry.util.entropy.TresBiEntropy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class BiEntropyApp {
	
	static SecureRandom rand;

	public static final void main(String[] argv) {
		
		try {
			rand = SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
			return;
		}
		
		CmdLineParser parser = new CmdLineParser();
		
		Option<Boolean> streamOpt = parser.addBooleanOption('s', "stdin");
		
		Option<String> itemOpt = parser.addStringOption('i', "item");
		Option<String> armorOpt = parser.addStringOption('a', "armor");
		Option<String> outputFormatOpt = parser.addStringOption('f', "outputFormat");
		
		// for random bytes
		Option<Integer> lengthOpt = parser.addIntegerOption('l', "length");
		Option<Integer> countOpt = parser.addIntegerOption('c', "count");
		
		// for show bits
		Option<Boolean> columnOpt = parser.addBooleanOption("column");
		Option<Boolean> longOpt = parser.addBooleanOption("long");
		
		String randAlg = rand.getAlgorithm();
		
		try {
			parser.parse(argv);
			
			String [] commands = parser.getRemainingArgs();
			String command = "BIENTROPY";
			if(argv.length > 0 && commands != null && commands.length>0){
				command = commands[0].toUpperCase();
			}else {
				ShowHelpUtil.showHelp("/bientropy-help-message.txt");
				return;
			}
			
			if(		!command.equals("BIENTROPY") 
					&& !command.equals("RAND") 
					&& !command.equals("SHANNON")
					&& !command.equals("SHOWBITS")
				){
				throw new RuntimeException("invalid command: "+command);
			}
			
			String item = parser.getOptionValue(itemOpt,null);
			Boolean stream = parser.getOptionValue(streamOpt,false);
			if(item != null && stream ){
				throw new RuntimeException("Cannot use -i|--item option when also using -s|-sdtin");
			}
			
			if(command.equals("SHOWBITS")){
				Boolean columnFormat = parser.getOptionValue(columnOpt, false);
				Boolean longFormat = parser.getOptionValue(longOpt, false);
				if(item != null) {
					ShowBits bits = new ShowBits(item);
					if(columnFormat.booleanValue()){
						System.out.println(bits.showColumnForm());
					}
					if(longFormat.booleanValue()){
						System.out.println(bits.showLongForm());
					}
				}else if(stream){
					try (BufferedReader breader = new BufferedReader(
							new InputStreamReader(System.in, Charset.forName("UTF-8")))){
						
						String line = null;
						while((line = breader.readLine()) != null){
							line = line.trim();
							ShowBits bits = new ShowBits(line);
							if(columnFormat.booleanValue()){
								System.out.println(bits.showColumnForm());
							}
							if(longFormat.booleanValue()){
								System.out.println(bits.showLongForm());
							}
						}
					}catch(IOException x){}
				}
				
				return;
			}
			
			if(command.equals("SHANNON")){
				if(item != null) {
					com.cryptoregistry.util.entropy.ShannonEntropy.Result res = ShannonEntropy.shannonEntropy(item);
					System.out.println(res.toString());
					return;
				}else if(stream){
					try (BufferedReader breader = new BufferedReader(
							new InputStreamReader(System.in, Charset.forName("UTF-8")))){
						
						String line = null;
						while((line = breader.readLine()) != null){
							line = line.trim();
							com.cryptoregistry.util.entropy.ShannonEntropy.Result res = ShannonEntropy.shannonEntropy(line);
							System.out.println(res.toString());
						}
					}catch(IOException x){}
				}
				
				return;
			}
			
			String format = parser.getOptionValue(outputFormatOpt,"JSON").toUpperCase();
				
			FileUtil.ARMOR armor = FileUtil.ARMOR.valueOf(parser.getOptionValue(armorOpt, FileUtil.ARMOR.none.toString()));
			
			if(command.equals("BIENTROPY")){
		
			  if(item != null){
					// process item
					if(item.length() ==1){
						com.cryptoregistry.util.entropy.BiEntropy bi = new com.cryptoregistry.util.entropy.BiEntropy(item.getBytes()[0]);
						com.cryptoregistry.util.entropy.BiEntropy.Result res = bi.calc();
						if(format.equals("JSON")) {
							System.out.println(res.toJSON());
						}else if(format.equals("CSV")) {
							System.out.println(res.toCSV());
						}else{
							System.out.println(res.toString());
						}
						return;
					}
					
					TresBiEntropy bi = new TresBiEntropy(item,armor);
					com.cryptoregistry.util.entropy.TresBiEntropy.Result res = bi.calc();
					if(format.equals("JSON")) {
						System.out.println(res.toJSON());
					}else if(format.equals("CSV")) {
						System.out.println(res.toCSV());
					}else{
						System.out.println(res.toString());
					}
				
				}else if(stream){
					
					List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
					
					try (BufferedReader breader = new BufferedReader(
								new InputStreamReader(System.in, Charset.forName("UTF-8")))){
						
						String line = null;
						
						if(format.equalsIgnoreCase("CSV")){
							StringBuffer buf = new StringBuffer();
							buf.append("version");
							buf.append(",");
							buf.append("algorithm");
							buf.append(",");
							buf.append("input");
							buf.append(",");
							buf.append("bits");
							buf.append(",");
							buf.append("biEntropy");
							buf.append(",");
							buf.append("bitsOfEntropy");
							buf.append("\n");
							System.out.println(buf.toString());
						}
						while((line = breader.readLine()) != null){
						
							line = line.trim();
							TresBiEntropy bi = new TresBiEntropy(line,armor);
							com.cryptoregistry.util.entropy.TresBiEntropy.Result res = bi.calc();
							if(format.equals("JSON")) {
								list.add(res.toMap());
							}else if(format.equals("CSV")) {
								System.out.println(res.toCSVLine());
							}else{
								System.out.println(res.toString());
							}
						}
						
						if(format.equalsIgnoreCase("JSON")){
							ObjectMapper mapper = new ObjectMapper();
							mapper.enable(SerializationFeature.INDENT_OUTPUT);
							Map<String,Object> wrapper = new LinkedHashMap<String,Object>();
							wrapper.put("version", "Buttermilk BiEntropy Version 1.0");
							wrapper.put("data", list);
							System.out.println(mapper.writeValueAsString(wrapper));
						}
						
					}catch(IOException x){
						x.printStackTrace();
					}
				}
			}
				
			if(command.equals("RAND")){
				
				Integer length = parser.getOptionValue(lengthOpt,32);
				Integer count = parser.getOptionValue(countOpt,1);
				if (length <= 0) throw new RuntimeException("length must be greater than 0");
				if (count <= 0) throw new RuntimeException("count must be greater than 0");
				if (length > 1024) throw new RuntimeException("length max size = 1024");
				if (count > 1024) throw new RuntimeException("count max size = 1024");
				
				System.out.println("PRNG Algorithm: "+randAlg);
				System.out.println("Armor: "+armor.toString());
				for(int i = 0;i<count;i++){
				byte[] data = new byte[length];
				rand.nextBytes(data);

					switch (armor) {
					case hex:
						System.out.println(new String(Hex.encode(data), "UTF-8").toUpperCase());
						break;
					case base16:
						System.out.println(new String(Hex.encode(data), "UTF-8"));
						break;
					case base64:
						System.out.println(Base64.encodeBytes(data));
						break;
					case base64url:
						System.out.println(Base64.encodeBytes(data, Base64.URL_SAFE));
						break;
					default:
						break;
					}
				}
				return;
			}
			
				
		} catch (OptionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	

}
