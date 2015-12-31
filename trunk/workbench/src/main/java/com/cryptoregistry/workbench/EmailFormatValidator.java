package com.cryptoregistry.workbench;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * From http://examples.javacodegeeks.com/core-java/util/regex/matcher/validate-email-address-with-java-regular-expression-example/
 * 
 *
 */
public class EmailFormatValidator {

	private Pattern pattern;
	private Matcher matcher;

	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"+
	"[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	public EmailFormatValidator() {
		pattern = Pattern.compile(EMAIL_PATTERN);
	}

	public boolean validate(final String email) {

		matcher = pattern.matcher(email);
		return matcher.matches();

	}
}