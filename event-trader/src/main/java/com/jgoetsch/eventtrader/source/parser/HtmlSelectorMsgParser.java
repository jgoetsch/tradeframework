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

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.source.MsgHandler;

public class HtmlSelectorMsgParser implements MsgParser {

	private String selector;
	private Pattern charsetPattern = Pattern.compile("charset=([^;]+)");

	public HtmlSelectorMsgParser() {
	}

	public HtmlSelectorMsgParser(String selector) {
		this.selector = selector;
	}

	public boolean parseContent(InputStream input, long length, String contentType, MsgHandler handler) throws IOException {
		Matcher charset = charsetPattern.matcher(contentType);
		Document doc = Jsoup.parse(input, charset.find() ? charset.group(1) : null, "");
		Elements nodes = doc.select(selector);
		for (Element node : nodes) {
			Msg msg = createMsg(node);
			if (msg != null && !handler.newMsg(msg))
				return false;
		}
		return true;
	}

	protected Msg createMsg(Element node) {
		return new Msg(null, node.text());
	}

	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

}
