/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2014 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.util;


import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**<pre>
 * Used for ISO 8601 formatting, which is our standard for date-time values. Reworked as per 
 * http://stackoverflow.com/questions/2201925/converting-iso-8601-compliant-string-to-java-util-date
 *
 * </pre>
 * @author Dave
 *
 */
public class TimeUtil {
	
	private static Lock lock = new ReentrantLock();

	public static String format(Date date) {
		 lock.lock(); 
		  try {
			  Calendar c = GregorianCalendar.getInstance();
			  c.setTime(date);
			  return javax.xml.bind.DatatypeConverter.printDateTime(c);
		 } finally {
		     lock.unlock();
		 }
	}
	
	
	public static final Date getISO8601FormatDate(String in) {
		 lock.lock(); 
	     try {
	    	 Calendar cal = javax.xml.bind.DatatypeConverter.parseDateTime(in);
	    	 return cal.getTime();
	     } finally {
	       lock.unlock();
	     }
	}
	
	public static final String now() {
		 lock.lock(); 
	     try {
	    	return format(new Date());
	     } finally {
	       lock.unlock();
	     }
	}

}
