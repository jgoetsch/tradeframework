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
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class SimpleMarketData implements MarketData, Serializable {

	private static final long serialVersionUID = 1L;

	private static class PriceWithSize {
		private BigDecimal price;
		private Integer size;

		public BigDecimal getPrice() {
			return price;
		}
		public void setPrice(BigDecimal price) {
			this.price = price;
		}
		public Integer getSize() {
			return size;
		}
		public void setSize(Integer size) {
			this.size = size;
		}

		@Override
		public String toString() {
			if (size == null)
				return String.valueOf(price);
			else {
				StringBuilder sb = new StringBuilder(String.valueOf(price));
				sb.append(" (").append(String.valueOf(size)).append(")");
				return sb.toString();
			}
		}
	}

	private final PriceWithSize bid;
	private final PriceWithSize ask;
	private final PriceWithSize last;
	private BigDecimal high;
	private BigDecimal low;
	private BigDecimal close;
	private Integer volume;
	private Instant lastTimestamp;
	private Instant timestamp;

	public SimpleMarketData() {
		bid = new PriceWithSize();
		ask = new PriceWithSize();
		last = new PriceWithSize();
	}

	public SimpleMarketData(BigDecimal bid, BigDecimal ask, BigDecimal last) {
		this();
		setBid(bid);
		setAsk(ask);
		setLast(last);
	}

	public SimpleMarketData(BigDecimal bid, Integer bidSize, BigDecimal ask, int askSize, BigDecimal last, Integer lastSize) {
		this(bid, ask, last);
		setBidSize(bidSize);
		setAskSize(askSize);
		setLastSize(lastSize);
	}

	public SimpleMarketData(MarketData source) {
		this(source.getBid(), source.getBidSize(), source.getAsk(), source.getAskSize(), source.getLast(), source.getLastSize());
		setHigh(source.getHigh());
		setLow(source.getLow());
		setClose(source.getClose());
		setVolume(source.getVolume());
		setLastTimestamp(source.getLastTimestamp());
		setTimestamp(source.getTimestamp());
	}

	private static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm:ss z").withZone(ZoneId.of("America/New_York"));

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(dateFormat.format(getTimestamp())).append(": ");
		sb.append("Bid=").append(getBid());
		sb.append(", Ask=").append(getAsk());
		sb.append(", Last=").append(getLast());
		return sb.toString();
	}

	public void setBid(BigDecimal bid) {
		this.bid.setPrice(bid);
	}

	public BigDecimal getBid() {
		return bid.getPrice();
	}

	public void setBidSize(Integer bidSize) {
		this.bid.setSize(bidSize);
	}

	public Integer getBidSize() {
		return bid.getSize();
	}

	public void setAsk(BigDecimal ask) {
		this.ask.setPrice(ask);
	}

	public BigDecimal getAsk() {
		return ask.getPrice();
	}

	public void setAskSize(Integer askSize) {
		this.ask.setSize(askSize);
	}

	public Integer getAskSize() {
		return ask.getSize();
	}

	public void setLast(BigDecimal last) {
		this.last.setPrice(last);
	}

	public BigDecimal getLast() {
		return last.getPrice();
	}

	public void setLastSize(Integer lastSize) {
		this.last.setSize(lastSize);
	}

	public Integer getLastSize() {
		return last.getSize();
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
