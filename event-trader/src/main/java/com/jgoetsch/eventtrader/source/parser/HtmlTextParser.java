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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.jgoetsch.eventtrader.source.MsgHandler;

public class HtmlTextParser implements MsgParser {

	private MsgParser innerParser = new FullBufferedMsgParser();
	private Pattern charsetPattern = Pattern.compile("charset=([^;]+)");

	public HtmlTextParser() {
	}

	public HtmlTextParser(MsgParser innerParser) {
		this.innerParser = innerParser;
	}

	public boolean parseContent(InputStream input, long length, String contentType, MsgHandler handler) throws IOException, MsgParseException {
		Matcher charset = charsetPattern.matcher(contentType);
		Document doc = Jsoup.parse(input, charset.find() ? charset.group(1) : null, "");
		String text = doc.text();
		return innerParser.parseContent(new ByteArrayInputStream(text.getBytes()), text.length(), "text/plain", handler);
	}

	public void setInnerParser(MsgParser innerParser) {
		this.innerParser = innerParser;
	}

	public MsgParser getInnerParser() {
		return innerParser;
	}

}
