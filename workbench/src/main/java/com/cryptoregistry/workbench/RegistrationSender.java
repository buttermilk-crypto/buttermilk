/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2015 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.workbench;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.http.HttpVersion;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;

import com.fasterxml.jackson.databind.ObjectMapper;

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
	boolean success;
	
	public RegistrationSender(Properties props) {
		this.props = props;
	}
	
	public void request(String registrationJSON) {
		try {
			  URIBuilder builder = new URIBuilder();
			   builder.setScheme(props.get("registration.reg.scheme"))
		        .setHost(props.get("registration.reg.hostname"))
		        .setPath(props.get("registration.reg.path"))
		        .setPort(props.intValue("registration.reg.port"));
			   String url = builder.build().toString();
				Response response = Request.Post(url)
				        .useExpectContinue()
				        .version(HttpVersion.HTTP_1_1)
				        .bodyString(registrationJSON, ContentType.APPLICATION_JSON)
				        .execute();

				responseBody = response.returnContent().asString(StandardCharsets.UTF_8);
				ObjectMapper mapper = new ObjectMapper();
				@SuppressWarnings("rawtypes")
				Map map = (Map) mapper.readValue(responseBody, Map.class);
				if(map.containsKey("error")){
					success = false;
				}else{
					success = true;
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	public String getResponseBody() {
		return responseBody;
	}

	public boolean isSuccess() {
		return success;
	}
	
	
}
