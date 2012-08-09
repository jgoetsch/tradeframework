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

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.source.MsgHandler;

public class NodeFilterMsgParser implements MsgParser {

	private NodeFilter nodeFilter;

	public NodeFilterMsgParser() {
	}

	public NodeFilterMsgParser(NodeFilter nodeFilter) {
		this.setNodeFilter(nodeFilter);
	}

	public boolean parseContent(InputStream input, long length, String contentType, MsgHandler handler) throws MsgParseException {
		Lexer lexer;
		try {
			lexer = new Lexer(new Page(input, new Page().getCharset(contentType)));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
		Parser parser = new Parser(lexer);

		try {
			NodeList nodes = parser.extractAllNodesThatMatch(getNodeFilter());
			for (int i=0; i < nodes.size(); i++) {
				if (!handler.newMsg(createMsg(nodes.elementAt(i))))
					return false;
			}
		} catch (ParserException e) {
			throw new MsgParseException(e);
		}
		return true;
	}

	protected Msg createMsg(Node node) {
		return new Msg(null, node.toPlainTextString());
	}

	public void setNodeFilter(NodeFilter nodeFilter) {
		this.nodeFilter = nodeFilter;
	}

	public NodeFilter getNodeFilter() {
		return nodeFilter;
	}

}
