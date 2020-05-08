/*
 * Copyright (c) 2012 Jeremy Goetsch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jgoetsch.eventtrader.source;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractHttpMsgSource extends UrlBasedMsgSource {

	private Logger log = LoggerFactory.getLogger(getClass());
	private String username;
	private String password;
	private CloseableHttpClient httpClient;
	private String method = "GET";

	protected HttpUriRequest createRequest() {
		List<NameValuePair> params = null;
		if (getRequestParameters() != null) {
			params = new ArrayList<NameValuePair>();
			for (Map.Entry<String, Object> p : getRequestParameters().entrySet())
				params.add(new BasicNameValuePair(p.getKey(), (String)p.getValue()));
		}

		HttpRequestBase req;
		if ("POST".equalsIgnoreCase(getMethod())) {
			req = new HttpPost(getUrl());
			if (params != null) {
				try {
					UrlEncodedFormEntity reqEntity = new UrlEncodedFormEntity(params, "UTF-8");
					((HttpPost)req).setEntity(reqEntity);
				} catch (UnsupportedEncodingException e) {
					log.error("Error encoding request entity", e);
				}
			}
		}
		else if (params != null) {
			req = new HttpGet(getUrl() + (getUrl().contains("?") ? "&" : "?") + URLEncodedUtils.format(params, "UTF-8"));
		}
		else {
			req = new HttpGet(getUrl());
		}

		return req;
	}

	@Override
	protected final void receiveMsgs() {
		if (httpClient == null) {
			HttpClientBuilder builder = HttpClients.custom();
	
			if (getUsername() != null) {
		        CredentialsProvider credsProvider = new BasicCredentialsProvider();
		        credsProvider.setCredentials(
		        		AuthScope.ANY,
		        		new UsernamePasswordCredentials(getUsername(), getPassword()));
		        builder.setDefaultCredentialsProvider(credsProvider);
			}
			
			httpClient = builder.build();
		}

		receiveMsgs(httpClient);
	}

	protected abstract void receiveMsgs(CloseableHttpClient client);

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getMethod() {
		return method;
	}

	public CloseableHttpClient getHttpClient() {
		return httpClient;
	}

	public void setHttpClient(CloseableHttpClient httpClient) {
		this.httpClient = httpClient;
	}

}
