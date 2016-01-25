/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2015 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.workbench;

import org.apache.http.HttpVersion;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;

import asia.redact.bracket.properties.Properties;

/**
 * Call the RegistrationAction interface on the server and post the registration JSON
 * 
 * @author Dave
 *
 */

public class RegistrationSender {

	Properties props;
	CloseableHttpClient httpclient;
	String responseBody;
	
	public RegistrationSender(Properties props) {
		this.props = props;
	}
	
	public String request(String registrationJSON) {
		try {
			  URIBuilder builder = new URIBuilder();
			   builder.setScheme(props.get("registration.reg.scheme"))
		        .setHost(props.get("registration.reg.hostname"))
		        .setPath(props.get("registration.reg.path"))
		        .setPort(props.intValue("registration.reg.port"));
			   String url = builder.build().toString();
				byte [] res = Request.Post(url)
				        .useExpectContinue()
				        .version(HttpVersion.HTTP_1_1)
				        .bodyString(registrationJSON, ContentType.APPLICATION_JSON)
				        .execute().returnContent().asBytes();
				return new String(res, "UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
			}
		return null;
	}
}
