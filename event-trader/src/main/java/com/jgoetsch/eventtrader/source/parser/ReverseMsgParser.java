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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.eventtrader.source.MsgHandler;

public class ReverseMsgParser extends BufferedMsgParser {

	Logger log = LoggerFactory.getLogger(ReverseMsgParser.class);
	private MsgParser msgParser;

	public ReverseMsgParser() { }
	public ReverseMsgParser(MsgParser msgParser) {
		this.msgParser = msgParser;
	}

	@Override
	protected boolean parseContent(String content, String contentType, MsgHandler handler) throws MsgParseException {
		StringBuilder buffer = new StringBuilder(content.length());
		int prev = content.length();
		for (int i = prev; i > 0; prev = i, i = content.lastIndexOf('\n', prev - 1)) {
			buffer.append(content.substring(i, prev));
		}
		String reverse = buffer.toString();
		try {
			InputStream in = new ByteArrayInputStream(reverse.getBytes());
			return msgParser.parseContent(in, reverse.length(), contentType, handler);
		} catch (IOException e) {
			throw new MsgParseException(e);
		}
	}

	public void setMsgParser(MsgParser msgParser) {
		this.msgParser = msgParser;
	}

	public MsgParser getMsgParser() {
		return msgParser;
	}

}
