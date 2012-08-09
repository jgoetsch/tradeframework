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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.eventtrader.source.parser.MsgParser;

public class ApeStreamingMsgSource extends HttpStreamingMsgSource {

	private Logger log = LoggerFactory.getLogger(HttpStreamingMsgSource.class);
	private int challenge = 1;
	private String sessid;
	private List<Command> initialCommands;

	public static class Command {
		private String cmd;
		private Map<String,Object> params;

		public Command() {}
		public void setCmd(String cmd) {
			this.cmd = cmd;
		}
		public String getCmd() {
			return cmd;
		}
		public void setParams(Map<String,Object> params) {
			this.params = params;
		}
		public Map<String,Object> getParams() {
			return params;
		}
	}

	public void receiveMsgs(HttpClient client) {
		if (executeRequest(client, createCommandRequest("CONNECT", getRequestParameters()), new MsgParser() {
			public boolean parseContent(InputStream input, long length, String contentType, MsgHandler handler) {
				JSONArray responseJson = (JSONArray)JSONValue.parse(new BufferedReader(new InputStreamReader(input)));
				for (Object obj : responseJson) {
					if (((JSONObject)obj).get("raw").equals("LOGIN")) {
						JSONObject data = (JSONObject)((JSONObject)obj).get("data");
						sessid = (String)data.get("sessid");
					}
				}
				if (sessid != null)
					return true;
				else {
					log.error("Could not find sessid in server response: " + responseJson.toString());
					return false;
				}
			} })
		) {
			log.info("Connected to " + getUrl() + " with session id " + sessid);
			if (initialCommands != null) {
				for (Command command : initialCommands) {
					log.info("Issuing command " + command.getCmd() + " " + command.getParams());
					if (!executeRequest(client, createCommandRequest(command.getCmd(), command.getParams()), getMsgParser()))
						break;
				}
			}
			super.receiveMsgs(client);
		}
	}

	protected HttpUriRequest createRequest() {
		return createCommandRequest("CHECK", null);
	}

	protected HttpUriRequest createCommandRequest(String cmd, Map<String, Object> params) {
		Map<String,Object> cmdParams = new LinkedHashMap<String,Object>();
		cmdParams.put("cmd", cmd);
		cmdParams.put("chl", challenge++);
		if (params != null) {
			cmdParams.put("params", params);
		}
		if (sessid != null) {
			cmdParams.put("sessid", sessid);
		}
		List<Object> cmdJson = new LinkedList<Object>();
		cmdJson.add(cmdParams);

		HttpPost req = new HttpPost(getUrl());
		req.addHeader("X-Requested-With", "XMLHttpRequest");
		try {
			String jsonCmd = JSONValue.toJSONString(cmdJson);
			req.setEntity(new StringEntity(jsonCmd, "application/x-www-form-urlencoded", "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			log.error("Error encoding request entity", e);
		}
		return req;
	}

	private JSONArray executeRequest(HttpClient client, HttpUriRequest request) {
		try {
			HttpResponse rsp = client.execute(request);
			HttpEntity entity = rsp.getEntity();
			if (rsp.getStatusLine().getStatusCode() >= 400 || entity == null) {
				log.error("HTTP request to " + request.getURI() + " failed with status " + rsp.getStatusLine());
				return null;
			}
			else {
				return (JSONArray)JSONValue.parse(new BufferedReader(new InputStreamReader(entity.getContent())));
			}
		} catch (Exception e) {
			log.warn("Exception reading or parsing message source", e);
			return null;
		}
		finally {
			request.abort();
		}
	}

	protected boolean afterDisconnect() {
		return true;
	}

	public void setInitialCommands(List<Command> initialCommands) {
		this.initialCommands = initialCommands;
	}

	public List<Command> getInitialCommands() {
		return initialCommands;
	}
}
