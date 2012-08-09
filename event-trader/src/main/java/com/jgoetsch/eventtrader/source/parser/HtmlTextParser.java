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

import org.htmlparser.Node;
import org.htmlparser.Tag;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.ParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.eventtrader.source.MsgHandler;

public class HtmlTextParser implements MsgParser {

	private Logger log = LoggerFactory.getLogger(HtmlTextParser.class);
	private MsgParser innerParser = new BufferedMsgParser();

	public boolean parseContent(InputStream input, long length, String contentType, MsgHandler handler) throws IOException, MsgParseException {
		Lexer lexer;
		try {
			lexer = new Lexer(new Page(input, null));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}

		StringBuilder sb = new StringBuilder();
		try {
			Node node;
			int newLines = 0;
			while ((node = lexer.nextNode()) != null) {
				if (node instanceof Tag) {
					Tag tag = (Tag) node;
					if (newLines < 2 && (tag.getTagName().equalsIgnoreCase("br") || (tag.getTagName().equalsIgnoreCase("p") && tag.isEndTag()))) {
						sb.append("\n");
						newLines++;
					}
				}
				String content = node.toPlainTextString().replaceAll("&nbsp;", "").replaceAll("&quot;", "\"").trim();
				if (content.length() > 0) {
					sb.append(content);
					newLines = 0;
				}
			}
		} catch (ParserException e) {
			log.warn("Exception parsing html", e);
		}

		return getInnerParser().parseContent(new ByteArrayInputStream(sb.toString().getBytes()), sb.length(), "text/plain", handler);
	}

	public void setInnerParser(MsgParser innerParser) {
		this.innerParser = innerParser;
	}

	public MsgParser getInnerParser() {
		return innerParser;
	}

}
