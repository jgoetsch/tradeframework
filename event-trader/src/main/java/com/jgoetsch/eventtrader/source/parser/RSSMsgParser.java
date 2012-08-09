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

import javax.xml.parsers.ParserConfigurationException;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.jgoetsch.eventtrader.Msg;

public final class RSSMsgParser extends XMLMsgParser {

	private String curField;
	private StringBuilder msgDate;
	private StringBuilder msgSourceName;
	private StringBuilder msgContent;

	private String itemElement = "item";
	private String dateElement = "pubDate";
	private String sourceNameElement = "dc:creator";
	private String messageElement = "title";
	private DateTimeFormatter dateFormat = DateTimeFormat.forPattern("EEE, d MMM yy HH:mm:ss Z");

	public RSSMsgParser() throws ParserConfigurationException, SAXException {
		super();
	}

	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes)
	throws SAXException
	{
		if (name.equalsIgnoreCase(itemElement)) {
			if (msgDate != null)
				throw new SAXException("Malformed RSS: nested item tag encountered");
			else {
				msgDate = new StringBuilder();
				msgSourceName = new StringBuilder();
				msgContent = new StringBuilder();
			}
		}
		else if (msgDate != null && curField == null) {
			curField = name;
		}
	}

	@Override
	public void endElement(String uri, String localName, String name)
	throws SAXException
	{
		if (name.equalsIgnoreCase(itemElement)) {
			if (msgDate == null)
				throw new SAXException("Malformed RSS");
			else {
				if (dateFormat != null && msgDate.length() > 0)
					newMsg(new Msg(dateFormat.parseDateTime(msgDate.toString()), msgSourceName.toString(), msgContent.toString()));
				else
					newMsg(new Msg(msgSourceName.toString(), msgContent.toString()));
				msgDate = null;
				msgSourceName = null;
				msgContent = null;
			}
		}
		else if (name.equalsIgnoreCase(curField)) {
			curField = null;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (curField != null) {
			if (curField.equalsIgnoreCase(getSourceNameElement()))
				msgSourceName.append(ch, start, length);
			else if (curField.equalsIgnoreCase(getMessageElement()))
				msgContent.append(ch, start, length);
			else if (curField.equalsIgnoreCase(getDateElement()))
				msgDate.append(ch, start, length);
		}
	}

	public void setMessageElement(String messageElement) {
		this.messageElement = messageElement;
	}

	public String getMessageElement() {
		return messageElement;
	}

	public void setSourceNameElement(String sourceNameElement) {
		this.sourceNameElement = sourceNameElement;
	}

	public String getSourceNameElement() {
		return sourceNameElement;
	}

	public void setDateElement(String dateElement) {
		this.dateElement = dateElement;
	}

	public String getDateElement() {
		return dateElement;
	}
}
