/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2015 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.app;

import org.apache.http.HttpVersion;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;

import asia.redact.bracket.properties.Properties;

/**
 * 
 * 
 * @author Dave
 *
 */

public class AddKeyClient {

	Properties props;
	String sessionReq;
	CloseableHttpClient httpclient;

	/**
	 * keyPath will be a directory where our confidential key (and possibly
	 * password file) are located).
	 * 
	 * @param props
	 * @param keyPath
	 */
	public AddKeyClient(Properties props, String req) {
		this.sessionReq = req;
	}

	public String request() {
		try {
			  URIBuilder builder = new URIBuilder();
			   builder.setScheme(props.get("registration.add.scheme"))
		        .setHost(props.get("registration.add.hostname"))
		        .setPath(props.get("registration.add.path"))
		        .setPort(props.intValue("registration.add.port"));
			   String url = builder.build().toString();
				byte [] res = Request.Post(url)
				        .useExpectContinue()
				        .version(HttpVersion.HTTP_1_1)
				        .bodyString(sessionReq, ContentType.APPLICATION_JSON)
				        .execute().returnContent().asBytes();
				return new String(res, "UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
			}
		return null;
	}


}
