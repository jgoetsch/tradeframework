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
package com.jgoetsch.tradeframework.marketdata;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class SimpleMarketData implements MarketData, Serializable {

	private static final long serialVersionUID = 1L;

	private double bid;
	private int bidSize;
	private double ask;
	private int askSize;
	private double last;
	private int lastSize;
	private double high;
	private double low;
	private double close;
	private int volume;
	private long lastTimestamp;
	private long timestamp;

	public SimpleMarketData() {
	}
	public SimpleMarketData(double bid, double ask, double last) {
		this(bid, 0, ask, 0, last, 0);
	}
	public SimpleMarketData(double bid, int bidSize, double ask, int askSize, double last, int lastSize) {
		this.bid = bid;
		this.bidSize = bidSize;
		this.ask = ask;
		this.askSize = askSize;
		this.last = last;
		this.lastSize = lastSize;
	}

	private static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm:ss z").withZone(ZoneId.of("America/New_York"));

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(dateFormat.format(Instant.ofEpochMilli(getTimestamp())));
		DecimalFormat df = new DecimalFormat("0.#####");
		sb.append(": Last=").append(df.format(getLast()));
		sb.append(", Bid=").append(df.format(getBid()));
		sb.append(", Ask=").append(df.format(getAsk()));
		return sb.toString();
	}

	public void setBid(double bid) {
		this.bid = bid;
	}

	public double getBid() {
		return bid;
	}

	public void setBidSize(int bidSize) {
		this.bidSize = bidSize;
	}

	public int getBidSize() {
		return bidSize;
	}

	public void setAsk(double ask) {
		this.ask = ask;
	}

	public double getAsk() {
		return ask;
	}

	public void setAskSize(int askSize) {
		this.askSize = askSize;
	}

	public int getAskSize() {
		return askSize;
	}

	public void setLast(double last) {
		this.last = last;
	}

	public double getLast() {
		return last;
	}

	public void setLastSize(int lastSize) {
		this.lastSize = lastSize;
	}

	public int getLastSize() {
		return lastSize;
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

	public void setVolume(int volume) {
		this.volume = volume;
	}

	public int getVolume() {
		return volume;
	}

	public void setLastTimestamp(long lastTimestamp) {
		this.lastTimestamp = lastTimestamp;
	}

	public long getLastTimestamp() {
		return lastTimestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getTimestamp() {
		return timestamp;
	}

}
