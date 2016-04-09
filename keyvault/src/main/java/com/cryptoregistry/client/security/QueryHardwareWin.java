/*
 *  This file is part of Buttermilk(TM) 
 *  Copyright 2013-2015 David R. Smith for cryptoregistry.com
 *
 */
package com.cryptoregistry.client.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

/**
 *<pre>
 * Generate unique hardware-based identifier.
 * 
 * Trivial approach, this is too easy to spoof. Need to call COM interfaces for WMI directly or some such.
 * 
 * using wmic call comes from example here:
 * 
 * https://github.com/sarxos/secure-tokens/blob/master/src/main/java/com/github/sarxos/securetoken/impl/Hardware4Win.java
 * 
 * The below should work on any contemporary Windows host BUT wmic has to be in the PATH. This is by no means
 * guaranteed which is why I am not currently using it.
 * 
 * </pre>
 * 
 * @author Dave
 *
 */
final class QueryHardwareWin implements QueryHardware {
	
	private static String serialNumber;
	private static String csproductUUID;
	private static String processorId;
	private static String diskDriveSerialNumber;

	public QueryHardwareWin() {
		
	}

	public String getBIOSSerialNumber() {
		
		   if (serialNumber != null) {
               return serialNumber;
		   }
		   
		   OutputStream os = null;
           InputStream is = null;

           Runtime runtime = Runtime.getRuntime();
           Process process = null;
           try {
                   process = runtime.exec(new String[] { "wmic", "bios", "get", "serialnumber" });
           } catch (IOException e) {
                   throw new RuntimeException(e);
           }

           os = process.getOutputStream();
           is = process.getInputStream();

           try {
                   os.close();
           } catch (IOException e) {
                   throw new RuntimeException(e);
           }

           Scanner sc = new Scanner(is);
           try {
                   while (sc.hasNext()) {
                           String next = sc.next();
                           if ("SerialNumber".equals(next)) {
                                   serialNumber = sc.next().trim();
                                   break;
                           }
                   }
           } finally {
                   try {
                           is.close();
                           sc.close();
                   } catch (IOException e) {}
           }

           if (serialNumber == null) {
                  serialNumber = QueryHardware.UNKNOWN;
           }

           return serialNumber;
		   
	}

	public String getCPUProcessorId() {
		
		   if (processorId != null) {
               return processorId;
		   }
		   
		   OutputStream os = null;
           InputStream is = null;

           Runtime runtime = Runtime.getRuntime();
           Process process = null;
           try {
                   process = runtime.exec(new String[] { "wmic", "cpu", "get", "ProcessorId" });
           } catch (IOException e) {
                   throw new RuntimeException(e);
           }

           os = process.getOutputStream();
           is = process.getInputStream();

           try {
                   os.close();
           } catch (IOException e) {
                   throw new RuntimeException(e);
           }

           Scanner sc = new Scanner(is);
           try {
                   while (sc.hasNext()) {
                           String next = sc.next();
                           if ("ProcessorId".equals(next)) {
                        	   processorId = sc.next().trim();
                                   break;
                           }
                   }
           } finally {
                   try {
                           is.close();
                           sc.close();
                   } catch (IOException e) {}
           }

           if (processorId == null) {
        	   processorId = QueryHardware.UNKNOWN;
           }

           return processorId;
	}

	public String getDiskDriveSerialNumber() {
		
		 if (diskDriveSerialNumber != null) {
             return diskDriveSerialNumber;
		   }
		   
		   OutputStream os = null;
         InputStream is = null;

         Runtime runtime = Runtime.getRuntime();
         Process process = null;
         try {
                 process = runtime.exec(new String[] { "wmic", "diskdrive", "get", "SerialNumber" });
         } catch (IOException e) {
                 throw new RuntimeException(e);
         }

         os = process.getOutputStream();
         is = process.getInputStream();

         try {
                 os.close();
         } catch (IOException e) {
                 throw new RuntimeException(e);
         }

         Scanner sc = new Scanner(is);
         try {
                 while (sc.hasNext()) {
                         String next = sc.next();
                         if ("SerialNumber".equals(next)) {
                        	 diskDriveSerialNumber = sc.next().trim();
                                 break;
                         }
                 }
         } finally {
                 try {
                         is.close();
                         sc.close();
                 } catch (IOException e) {}
         }

         if (diskDriveSerialNumber == null) {
        	 diskDriveSerialNumber = QueryHardware.UNKNOWN;
         }

         return diskDriveSerialNumber;
	}

    public String getCSProductUUID() {
   
 		   if (csproductUUID != null) {
                return csproductUUID;
 		   }
 		   
 		   OutputStream os = null;
            InputStream is = null;

            Runtime runtime = Runtime.getRuntime();
            Process process = null;
            try {
                    process = runtime.exec(new String[] { "wmic", "csproduct", "get", "uuid" });
            } catch (IOException e) {
                    throw new RuntimeException(e);
            }

            os = process.getOutputStream();
            is = process.getInputStream();

            try {
                    os.close();
            } catch (IOException e) {
                    throw new RuntimeException(e);
            }

            Scanner sc = new Scanner(is);
            try {
                    while (sc.hasNext()) {
                            String next = sc.next();
                            if ("UUID".equals(next)) {
                                    csproductUUID = sc.next().trim();
                                    break;
                            }
                    }
            } finally {
                    try {
                            is.close();
                            sc.close();
                    } catch (IOException e) {}
            }

            if (csproductUUID == null) {
                    csproductUUID = QueryHardware.UNKNOWN;
            }

            return csproductUUID;
    }

}
