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
package com.jgoetsch.tradeframework;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * OHLC implementation with settable fields
 * 
 * @author jgoetsch
 *
 */
public class SimpleOHLC implements OHLC, Serializable {

	private static final long serialVersionUID = 2L;

	private Date date;
	private double open;
	private double high;
	private double low;
	private double close;
	private long volume;

	public SimpleOHLC() {
	}

	public SimpleOHLC(OHLC ohlc) {
		this.date = ohlc.getDate();
		this.open = ohlc.getOpen();
		this.high = ohlc.getHigh();
		this.low = ohlc.getLow();
		this.close = ohlc.getClose();
		this.volume = ohlc.getVolume();
	}

	public void setOpen(double open) {
		this.open = open;
	}

	public double getOpen() {
		return open;
	}

	public void setHigh(double high) {
		this.high = high;
	}

	public double getHigh() {
		return high;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public double getLow() {
		return low;
	}

	public void setClose(double close) {
		this.close = close;
	}

	public double getClose() {
		return close;
	}

	public void setVolume(long volume) {
		this.volume = volume;
	}

	public long getVolume() {
		return volume;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date getDate() {
		return date;
	}

	public Date getEnd() {
		return null;
	}
	
	private static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm:ss z").withZone(ZoneId.of("America/New_York"));
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (getDate() != null) {
			sb.append(dateFormat.format(getDate().toInstant())).append(' ');
		}
		sb.append("O=").append(getOpen());
		sb.append(" H=").append(getHigh());
		sb.append(" L=").append(getLow());
		sb.append(" C=").append(getClose());
		return sb.toString();
	}

}
