/*
 *  This file is part of Buttermilk(TM) 
 *  Copyright 2013-2015 David R. Smith for cryptoregistry.com
 *
 */
package com.cryptoregistry.client.security;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.iharder.Base64;

public interface QueryHardware {

	String getBIOSSerialNumber();
	String getCPUProcessorId();
	String getDiskDriveSerialNumber();
	String getCSProductUUID();
	
	public static final String UNKNOWN = "Unknown";
	
	final static class Factory {
		
		static String result;
		
		private final static HardwareData hardwareData() {
			
			final String os = System.getProperty("os.name");
			if(os.contains("Windows")){
				QueryHardware ch = new QueryHardwareWin();
				return new HardwareData(
						ch.getBIOSSerialNumber(),ch.getCSProductUUID(),ch.getCPUProcessorId(),ch.getDiskDriveSerialNumber());
			}else if(os.contains("Mac")){
				throw new UnsupportedOperationException("Mac not yet supported.");
			}else if(os.contains("Linux")){
				throw new UnsupportedOperationException("Linux not yet supported.");
			}else{
				throw new UnsupportedOperationException(os+" not yet supported.");
			}
		}
		
		public final static String generateId() {
			
			if(result != null) return result;
			
			HardwareData data = hardwareData();
			
			if(data.quality() != HardwareData.Quality.COMPLETE) {
				// TODO log WARN level
				System.err.println("CAUTION: may not be sufficient data to make unique identifier: "+data.quality());
			}
			
			MessageDigest md;
			try {
				md = MessageDigest.getInstance("SHA-256");
				StringBuffer buf = new StringBuffer();
				buf.append(data.baseboardSerialNumber);
				buf.append(data.csproductUUID);
				buf.append(data.processorId);
				buf.append(data.diskDriveSerialNumber);
			//	buf.append(remoteToken);
				String text = buf.toString();

				md.update(text.getBytes("UTF-8"));
				byte[] digest = md.digest();
			//	result = Factory.getHexString(digest);
				result = Base64.encodeBytes(digest, Base64.URL_SAFE);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return result;
		}
	}
	
	
}

