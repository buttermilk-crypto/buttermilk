package com.cryptoregistry.workbench;

public class ExceptionHolder{
	
	public Exception ex;

	public ExceptionHolder() {}
	
	public boolean hasException() {
		return ex != null;
	}
}
