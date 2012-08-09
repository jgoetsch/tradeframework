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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WordPressLoginFilter implements HttpActionFilter {

	Logger log = LoggerFactory.getLogger(WordPressLoginFilter.class);
	private String loginUrl;

	public WordPressLoginFilter() {
	}
	public WordPressLoginFilter(String loginUrl) {
		this.loginUrl = loginUrl;
	}

	public void doAction(HttpClient client, AbstractHttpMsgSource httpMsgSource) {
		try {
			if (getLoginUrl() == null || getLoginUrl().length() == 0)
				throw new IllegalStateException("Required property \"loginUrl\" not set");
			else {
				HttpPost post = new HttpPost(getLoginUrl());
				List <NameValuePair> nvps = new ArrayList <NameValuePair>();
				nvps.add(new BasicNameValuePair("log", httpMsgSource.getUsername()));
				nvps.add(new BasicNameValuePair("pwd", httpMsgSource.getPassword()));
				nvps.add(new BasicNameValuePair("redirect_to", "wp-admin/"));
				nvps.add(new BasicNameValuePair("testcookie", "1"));
				post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
				HttpParams postParams = new BasicHttpParams();
				postParams.setBooleanParameter("http.protocol.handle-redirects", false);
				post.setParams(postParams);

				HttpResponse rsp = client.execute(post);
				HttpEntity entity = rsp.getEntity();
				if (entity != null)
					entity.consumeContent(); // release connection gracefully
	
				if (rsp.getStatusLine().getStatusCode() >= 400)
					throw new IOException("Login POST request failed with error code " + rsp.getStatusLine());
				else if (rsp.getStatusLine().getStatusCode() != 302)
					throw new IOException("Login failed, redirect response not received (probably incorrect username/password)");
				else {
					log.debug("Successfully logged into " + getLoginUrl() + " as user " + httpMsgSource.getUsername());
				}
			}
		}
		catch (IOException e) {
			log.error("Wordpress site login failed", e);
		}

	}

	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}

	public String getLoginUrl() {
		return loginUrl;
	}

}
