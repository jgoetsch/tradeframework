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
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class SimpleMarketData implements MarketData, Serializable {

	private static final long serialVersionUID = 1L;

	private BigDecimal bid;
	private Integer bidSize;
	private BigDecimal ask;
	private Integer askSize;
	private BigDecimal last;
	private Integer lastSize;
	private BigDecimal high;
	private BigDecimal low;
	private BigDecimal close;
	private Integer volume;
	private Instant lastTimestamp;
	private Instant timestamp;

	public SimpleMarketData() {
	}

	public SimpleMarketData(BigDecimal bid, BigDecimal ask, BigDecimal last) {
		this(bid, 0, ask, 0, last, 0);
	}

	public SimpleMarketData(BigDecimal bid, Integer bidSize, BigDecimal ask, int askSize, BigDecimal last, Integer lastSize) {
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
		sb.append(dateFormat.format(getTimestamp()));
		DecimalFormat df = new DecimalFormat("0.#####");
		sb.append(": Last=").append(df.format(getLast()));
		sb.append(", Bid=").append(df.format(getBid()));
		sb.append(", Ask=").append(df.format(getAsk()));
		return sb.toString();
	}

	public void setBid(BigDecimal bid) {
		this.bid = bid;
	}

	public BigDecimal getBid() {
		return bid;
	}

	public void setBidSize(Integer bidSize) {
		this.bidSize = bidSize;
	}

	public Integer getBidSize() {
		return bidSize;
	}

	public void setAsk(BigDecimal ask) {
		this.ask = ask;
	}

	public BigDecimal getAsk() {
		return ask;
	}

	public void setAskSize(Integer askSize) {
		this.askSize = askSize;
	}

	public Integer getAskSize() {
		return askSize;
	}

	public void setLast(BigDecimal last) {
		this.last = last;
	}

	public BigDecimal getLast() {
		return last;
	}

	public void setLastSize(Integer lastSize) {
		this.lastSize = lastSize;
	}

	public Integer getLastSize() {
		return lastSize;
	}

	public void setHigh(BigDecimal high) {
		this.high = high;
	}

	public BigDecimal getHigh() {
		return high;
	}

	public void setLow(BigDecimal low) {
		this.low = low;
	}

	public BigDecimal getLow() {
		return low;
	}

	public void setClose(BigDecimal close) {
		this.close = close;
	}

	public BigDecimal getClose() {
		return close;
	}

	public void setVolume(Integer volume) {
		this.volume = volume;
	}

	public Integer getVolume() {
		return volume;
	}

	public void setLastTimestamp(Instant lastTimestamp) {
		this.lastTimestamp = lastTimestamp;
	}

	public Instant getLastTimestamp() {
		return lastTimestamp;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

}
