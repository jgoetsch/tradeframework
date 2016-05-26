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

import java.util.List;
import java.util.Map;

import org.json.simple.JSONValue;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.source.MsgHandler;

public class TwitterJSONMsgParser extends LineBufferedMsgParser {

	@SuppressWarnings("rawtypes")
	@Override
	public boolean parseContent(String content, String contentType, MsgHandler handler) throws MsgParseException {
		if (contentType == null || !contentType.split(";")[0].trim().equalsIgnoreCase("application/json"))
			throw new MsgParseException("Attempt to parse non-JSON content type " + contentType + " with JSONMsgParser");

		Object json = JSONValue.parse(content);
		if (json instanceof Map)
			parseStatus((Map)json, handler);
		else if (json instanceof List) {
			for (Object status : (List)json) {
				if (status instanceof Map)
					parseStatus((Map)status, handler);
			}
		}
		return true;
	}

	@SuppressWarnings("rawtypes")
	private void parseStatus(Map json, MsgHandler handler) {
		String txt = (String)json.get("text");
		Map user = ((Map)json.get("user"));
		if (txt != null && user != null) {
			Msg msg = new Msg((String)user.get("screen_name"), txt);
			msg.setImageUrl((String)user.get("profile_image_url"));
			handler.newMsg(msg);
		}
	}
}
