/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith. All Rights Reserved.
 *
 */
package com.cryptoregistry.workbench;

public class ExceptionHolder{
	
	public Exception ex;

	public ExceptionHolder() {}
	
	public boolean hasException() {
		return ex != null;
	}
}
