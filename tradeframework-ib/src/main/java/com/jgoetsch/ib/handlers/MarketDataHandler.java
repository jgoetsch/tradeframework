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
package com.jgoetsch.ib.handlers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import com.ib.client.TickAttrib;
import com.ib.client.TickType;
import com.jgoetsch.ib.TWSException;
import com.jgoetsch.tradeframework.marketdata.MarketData;

public class MarketDataHandler extends BaseIdHandler implements MarketData {

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

	private final CompletableFuture<MarketData> future = new CompletableFuture<MarketData>();

	public MarketDataHandler(int tickerId) {
		super(tickerId);
	}

	public CompletableFuture<MarketData> getCompletableFuture() {
		return future;
	}

	@Override
	protected synchronized void onTickPrice(int field, double price, TickAttrib attrib) {
		BigDecimal decimal = BigDecimal.valueOf(price);
		switch (TickType.get(field)) {
			case BID:
				setBid(decimal); break;
			case ASK:
				setAsk(decimal); break;
			case LAST:
				setLast(decimal); break;
			case HIGH:
				setHigh(decimal); break;
			case LOW:
				setLow(decimal); break;
			case CLOSE:
				setClose(decimal); break;
			default:
				break;
		}
		timestamp = Instant.now();
	}

	@Override
	protected synchronized void onTickSize(int field, int size) {
		switch (TickType.get(field)) {
			case BID_SIZE:
				setBidSize(size); break;
			case ASK_SIZE:
				setAskSize(size); break;
			case LAST_SIZE:
				setLastSize(size); break;
			case VOLUME:
				setVolume(size); break;
			default:
				break;
		}
		timestamp = Instant.now();
	}

	@Override
	protected synchronized void onTickString(int tickType, String value) {
		switch (TickType.get(tickType)) {
			case LAST_TIMESTAMP:
				setLastTimestamp(Instant.ofEpochMilli(Long.parseLong(value))); break;
			default:
				break;
		}
		timestamp = Instant.now();
	}

	@Override
	protected void onTickSnapshotEnd() {
		future.complete(this);
	}
	@Override
	protected void onError(int errorCode, String errorMsg) {
		future.completeExceptionally(new TWSException(errorCode, errorMsg));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(").append(getBidSize()).append(") ");
		sb.append(getBid()).append(" / ").append(getAsk());
		sb.append(" (").append(getAskSize()).append(") L:");
		sb.append(getLast());
		return sb.toString();
	}

	private void setBid(BigDecimal bid) {
		this.bid = bid;
	}
	public BigDecimal getBid() {
		return bid;
	}
	private void setBidSize(Integer bidSize) {
		this.bidSize = bidSize;
	}
	public Integer getBidSize() {
		return bidSize;
	}
	private void setAsk(BigDecimal ask) {
		this.ask = ask;
	}
	public BigDecimal getAsk() {
		return ask;
	}
	private void setAskSize(Integer askSize) {
		this.askSize = askSize;
	}
	public Integer getAskSize() {
		return askSize;
	}
	private void setLast(BigDecimal last) {
		this.last = last;
	}
	public BigDecimal getLast() {
		return last;
	}
	private void setLastSize(Integer lastSize) {
		this.lastSize = lastSize;
	}
	public Integer getLastSize() {
		return lastSize;
	}
	private void setHigh(BigDecimal high) {
		this.high = high;
	}
	public BigDecimal getHigh() {
		return high;
	}
	private void setLow(BigDecimal low) {
		this.low = low;
	}
	public BigDecimal getLow() {
		return low;
	}
	private void setVolume(Integer volume) {
		this.volume = volume;
	}
	public Integer getVolume() {
		return volume;
	}
	private void setClose(BigDecimal close) {
		this.close = close;
	}
	public BigDecimal getClose() {
		return close;
	}
	private void setLastTimestamp(Instant lastTimestamp) {
		this.lastTimestamp = lastTimestamp;
	}
	public Instant getLastTimestamp() {
		return lastTimestamp;
	}

	public Instant getTimestamp() {
		return timestamp;
	}
}
