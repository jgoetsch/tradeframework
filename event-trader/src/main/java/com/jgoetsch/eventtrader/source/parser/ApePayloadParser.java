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
package com.jgoetsch.eventtrader.source.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.eventtrader.source.MsgHandler;
import com.jgoetsch.eventtrader.source.parser.structured.StructuredMsgParser;

@Deprecated
public class ApePayloadParser implements MsgParser {
	private Logger log = LoggerFactory.getLogger(ApePayloadParser.class);
	private StructuredMsgParser payload;

	public boolean parseContent(InputStream input, long length, String contentType, MsgHandler handler) throws IOException, MsgParseException {
		JSONArray json = (JSONArray)JSONValue.parse(new BufferedReader(new InputStreamReader(input)));
		for (Object rawObj : json) {
			log.info("Received raw message " + rawObj.toString());
			JSONObject raw = (JSONObject)rawObj;
			String rawType = (String)raw.get("raw");
			JSONObject data = (JSONObject)raw.get("data");

			if (!payload.parseData(rawType, data, handler))
				return false;
		}
		return true;
	}

	public void setPayload(StructuredMsgParser payload) {
		this.payload = payload;
	}

	public StructuredMsgParser getPayload() {
		return payload;
	}
}
