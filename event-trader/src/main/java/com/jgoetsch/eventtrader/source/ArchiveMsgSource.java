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
package com.jgoetsch.eventtrader.source;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.source.parser.MsgParseException;
import com.jgoetsch.tradeframework.marketdata.TimeAdvanceable;

/**
 * Reads messages as "timestamp author message..." formatted lines from an input stream
 * i.e. chat logs.
 * 
 * Being a non-realtime source, it will also advance the time in a simulated
 * trading environment if its timeAdvancer property is set.
 * 
 * @author jgoetsch
 *
 */
public class ArchiveMsgSource extends MsgSource {

	private Logger log = LoggerFactory.getLogger(ArchiveMsgSource.class);
	private TimeAdvanceable timeAdvancer;
	private final BufferedReader reader;
	private Msg nextMsg;

	public ArchiveMsgSource(InputStream inputStream) {
		this.reader = new BufferedReader(new InputStreamReader(inputStream));
	}

	public ArchiveMsgSource(File inputFile) throws FileNotFoundException {
		this.reader = new BufferedReader(new FileReader(inputFile));
	}

	public final void receiveMsgs() {
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				Msg msg = parseLine(line);
				newMsg(msg);
			}
		} catch (Exception e) {
			log.error("Exception reading from archive", e);
		}
	}

	private static final Pattern lineFmt = Pattern.compile("\\[(.*?)\\]\\s*(\\w*)\\s*(.*)");

	protected Msg parseLine(String line) throws MsgParseException {
		Matcher m = lineFmt.matcher(line);
		if (m.matches()) {
			DateTime time = parseMsgTime(m.group(1));
			return new Msg(time, m.group(2), m.group(3));
		}
		else
			throw new MsgParseException(line);

	}

	private static final DateTimeFormatter timestampFormat = DateTimeFormat.forPattern("E MMM dd HH:mm:ss y").withZone(DateTimeZone.forID("America/New_York"));

	protected DateTime parseMsgTime(String time) throws MsgParseException {
		try {
			return timestampFormat.parseDateTime(time.replaceAll("_", " ").replaceAll("[EPC][SD]T\\s+", ""));
		} catch (IllegalArgumentException e) {
			throw new MsgParseException("Invalid date format", e);
		}
	}

	public void setTimeAdvancer(TimeAdvanceable timeAdvancer) {
		this.timeAdvancer = timeAdvancer;
	}

	public TimeAdvanceable getTimeAdvancer() {
		return timeAdvancer;
	}
}
