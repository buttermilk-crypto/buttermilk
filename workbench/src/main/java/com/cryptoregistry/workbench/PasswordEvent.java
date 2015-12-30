package com.cryptoregistry.workbench;

import java.util.EventObject;

public class PasswordEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	
	private char [] passwordValue;

	public PasswordEvent(Object source) {
		super(source);
	}

	public char[] getPasswordValue() {
		return passwordValue;
	}

	public void setPasswordValue(char[] passwordValue) {
		this.passwordValue = passwordValue;
	}

}
