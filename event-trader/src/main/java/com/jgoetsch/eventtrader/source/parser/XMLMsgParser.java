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
import java.util.ConcurrentModificationException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.source.MsgHandler;

/**
 * Not thread safe.
 * 
 * @author jgoetsch
 *
 */
public class XMLMsgParser extends DefaultHandler implements MsgParser {

	private Logger log = LoggerFactory.getLogger(XMLMsgParser.class);
	private SAXParser parser;
	private MsgHandler msgHandler;

	private class StopProcessingSAXException extends SAXException {
		private static final long serialVersionUID = 1L;
	}

	public XMLMsgParser() throws ParserConfigurationException, SAXException {
		this.parser = SAXParserFactory.newInstance().newSAXParser();
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		log.info(new String(ch, start, length));
	}

	public boolean parseContent(InputStream input, long length, String contentType, MsgHandler handler) throws IOException, MsgParseException
	{
		if (msgHandler != null)
			throw new ConcurrentModificationException("XMLMsgParser instance should not be used concurrently");
		if (contentType != null && !contentType.toLowerCase().startsWith("text/xml") && !contentType.toLowerCase().startsWith("application/xml")) {
			byte contents[] = new byte[2048];
			input.read(contents);
			throw new MsgParseException("Attempt to parse non-XML content type " + contentType + " with XMLMsgParser:\n" + new String(contents));
		}

		try {
			msgHandler = handler;
			parser.parse(input, this);
		}
		catch (StopProcessingSAXException e) {
			return false;
		}
		catch (SAXException e) {
			throw new MsgParseException(e);
		}
		finally {
			msgHandler = null;
		}

		return true;
	}

	/**
	 * To be called by subclasses when a new Msg is parsed out of the input.
	 * 
	 * @param msg
	 * @throws StopProcessingSAXException
	 */
	protected void newMsg(Msg msg) throws StopProcessingSAXException {
		if (!msgHandler.newMsg(msg))
			throw new StopProcessingSAXException();
	}

}
