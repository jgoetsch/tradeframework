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
package com.jgoetsch.eventtrader.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.source.MsgHandler;
import com.jgoetsch.eventtrader.source.parser.HtmlTextParser;
import com.jgoetsch.eventtrader.source.parser.PlainTextMsgParser;

public class HtmlTextParserTest {

	private HtmlTextParser msgParser = new HtmlTextParser(new PlainTextMsgParser());
	private String content = "<html><body><p>First line</p>Second line<br>third <b>line</b> <h1>More stuff</h1></body></html>";

	@Test
	public void testText() throws Exception {
		final List<Msg> msgs = new ArrayList<Msg>();
		msgParser.parseContent(new ByteArrayInputStream(content.getBytes()), content.length(), "text/html", new MsgHandler() {
			public boolean newMsg(Msg msg) {
				msgs.add(msg);
				System.out.println(msg.getMessage());
				return true;
			}
		});
		assertEquals("First lineSecond line third line More stuff", msgs.get(0).getMessage());
	}
}
