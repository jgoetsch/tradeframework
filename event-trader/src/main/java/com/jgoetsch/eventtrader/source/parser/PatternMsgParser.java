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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.source.MsgHandler;

public class PatternMsgParser implements BufferedMsgParser {

	private Pattern pattern;
	private String sourceFormat;
	private String messageFormat;

	public PatternMsgParser() {
	}

	public PatternMsgParser(Pattern pattern, String messageFormat) {
		this.pattern = pattern;
		this.messageFormat = messageFormat;
		this.sourceFormat = null;
	}

	public PatternMsgParser(Pattern pattern, String sourceFormat, String messageFormat) {
		this.pattern = pattern;
		this.messageFormat = messageFormat;
		this.sourceFormat = sourceFormat;
	}

	@Override
	public boolean parseContent(String content, String contentType, MsgHandler handler) {
		Matcher m = pattern.matcher(content);
		while (m.find()) {
			Msg msg = new Msg(sourceFormat != null ? formatFromGroups(m, sourceFormat) : null, formatFromGroups(m, messageFormat));
			if (!handler.newMsg(msg))
				return false;
		}
		return true;
	}

	private static final Pattern p = Pattern.compile("\\$(\\d+)");
	private String formatFromGroups(Matcher inputMatch, String format) {
		Matcher m = p.matcher(format);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, inputMatch.group(Integer.parseInt(m.group(1))));
		}
		m.appendTail(sb);
		return sb.toString();
	}

	public Pattern getPattern() {
		return pattern;
	}

	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}

	public String getSourceFormat() {
		return sourceFormat;
	}

	public void setSourceFormat(String sourceFormat) {
		this.sourceFormat = sourceFormat;
	}

	public String getMessageFormat() {
		return messageFormat;
	}

	public void setMessageFormat(String messageFormat) {
		this.messageFormat = messageFormat;
	}
}
