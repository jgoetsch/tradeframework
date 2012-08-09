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
import java.io.UnsupportedEncodingException;

import org.htmlparser.Parser;
import org.htmlparser.beans.StringBean;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.ParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.eventtrader.source.MsgHandler;

public class HtmlBeanParser implements MsgParser {

	private Logger log = LoggerFactory.getLogger(HtmlBeanParser.class);
	private MsgParser innerParser = new BufferedMsgParser();

	public boolean parseContent(InputStream input, long length, String contentType, MsgHandler handler) throws IOException, MsgParseException {
		Lexer lexer;
		try {
			lexer = new Lexer(new Page(input, null));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
		Parser parser = new Parser(lexer);
		StringBean sb = new StringBean();
		try {
			parser.visitAllNodesWith(sb);
		} catch (ParserException e) {
			log.error("Failed to parse html", e);
		}

		return getInnerParser().parseContent(new ByteArrayInputStream(sb.getStrings().getBytes()), sb.getStrings().length(), "text/plain", handler);
	}

	public void setInnerParser(MsgParser innerParser) {
		this.innerParser = innerParser;
	}

	public MsgParser getInnerParser() {
		return innerParser;
	}

}
