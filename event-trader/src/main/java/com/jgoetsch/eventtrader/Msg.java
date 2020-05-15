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
package com.jgoetsch.eventtrader;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.jgoetsch.eventtrader.source.parser.mapper.MsgMappable;

/**
 * Basic object representing a textual message of some sort. Msg objects are
 * extracted from any source by a
 * {@link com.jgoetsch.eventtrader.source.MsgSource MsgSource} and passed to one
 * or more {@link com.jgoetsch.eventtrader.processor.Processor Processor}
 * objects in order to perform actions based on the message contents.
 * 
 * @author jgoetsch
 * 
 */

@JsonTypeInfo(use = Id.CLASS)
public class Msg implements Serializable, MsgMappable {

	private static final long serialVersionUID = 1L;

	private Instant date;
	private String sourceName;
	private String sourceType;
	private String message;
	private String targetUrl;
	private String imageUrl;

	public Msg() {
	}

	public Msg(Msg other) {
		if (other != null) {
			this.date = other.date;
			this.sourceName = other.sourceName;
			this.message = other.message;
			this.targetUrl = other.targetUrl;
			this.imageUrl = other.imageUrl;
		}
	}

	public Msg(String sourceName, String message) {
		this.date = Instant.now();
		this.sourceName = sourceName;
		this.message = message;
	}

	public Msg(Instant date, String sourceName, String message) {
		this.date = date;
		this.sourceName = sourceName;
		this.message = message;
	}

	private static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm:ss z");
	private static final int MAX_MSG_DISPLAY_LENGTH = 1024;
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (getDate() != null)
			sb.append(dateFormat.format(getDate().atZone(ZoneId.systemDefault()))).append(" - ");
		if (getSourceName() != null)
			sb.append(getSourceName()).append(": ");
		if (getMessage() != null && getMessage().length() > MAX_MSG_DISPLAY_LENGTH) {
			int end = getMessage().lastIndexOf(" ", MAX_MSG_DISPLAY_LENGTH);
			if (end == -1)
				end = MAX_MSG_DISPLAY_LENGTH;
			sb.append(getMessage().substring(0, Math.min(getMessage().length(), end)));
			sb.append("...");
		}
		else
			sb.append(getMessage());
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getMessage() == null) ? 0 : getMessage().hashCode());
		result = prime * result
				+ ((getSourceName() == null) ? 0 : getSourceName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Msg other = (Msg) obj;
		if (getMessage() == null) {
			if (other.getMessage() != null)
				return false;
		} else if (!getMessage().equals(other.getMessage()))
			return false;
		if (getSourceName() == null) {
			if (other.getSourceName() != null)
				return false;
		} else if (!getSourceName().equals(other.getSourceName()))
			return false;
		return true;
	}

	@Override
	public Msg toMsg() {
		return this;
	}

	public void setDate(Instant date) {
		this.date = date;
	}

	public Instant getDate() {
		return date;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public String getSourceName() {
		return sourceName;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}

	public String getTargetUrl() {
		return targetUrl;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public String getSourceType() {
		return sourceType;
	}

}
