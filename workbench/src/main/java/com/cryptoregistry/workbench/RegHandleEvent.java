/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith. All Rights Reserved.
 *
 */
package com.cryptoregistry.workbench;

import java.util.EventObject;

public class RegHandleEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	
	String regHandle;
	
	public RegHandleEvent(Object source) {
		super(source);
	}

	public String getRegHandle() {
		return regHandle;
	}

	public void setRegHandle(String regHandle) {
		this.regHandle = regHandle;
	}

}
