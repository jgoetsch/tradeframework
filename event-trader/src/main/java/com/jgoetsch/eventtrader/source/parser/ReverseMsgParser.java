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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.eventtrader.source.MsgHandler;

public class ReverseMsgParser implements BufferedMsgParser {

	Logger log = LoggerFactory.getLogger(ReverseMsgParser.class);
	private BufferedMsgParser bufferedMsgParser;

	public ReverseMsgParser(BufferedMsgParser bufferedMsgParser) {
		this.bufferedMsgParser = bufferedMsgParser;
	}

	@Override
	public boolean parseContent(String content, String contentType, MsgHandler handler) throws MsgParseException {
		StringBuilder buffer = new StringBuilder(content.length());
		int prev = content.length();
		for (int i = prev; i > 0; prev = i, i = content.lastIndexOf('\n', prev - 1)) {
			buffer.append(content.substring(i, prev));
			if (!bufferedMsgParser.parseContent(content.substring(i, prev), contentType, handler))
				return false;
		}
		return true;
	}

}
