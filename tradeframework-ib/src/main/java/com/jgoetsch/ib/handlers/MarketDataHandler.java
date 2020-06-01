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

import java.util.concurrent.CompletableFuture;

import com.ib.client.TickAttrib;
import com.ib.client.TickType;
import com.jgoetsch.ib.TWSException;
import com.jgoetsch.tradeframework.marketdata.MarketData;

public class MarketDataHandler extends BaseIdHandler implements MarketData {

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

	private final CompletableFuture<MarketData> future = new CompletableFuture<MarketData>();

	public MarketDataHandler(int tickerId) {
		super(tickerId);
	}

	public CompletableFuture<MarketData> getCompletableFuture() {
		return future;
	}

	@Override
	protected synchronized void onTickPrice(int field, double price, TickAttrib attrib) {
		switch (TickType.get(field)) {
			case BID:
				setBid(price); break;
			case ASK:
				setAsk(price); break;
			case LAST:
				setLast(price); break;
			case HIGH:
				setHigh(price); break;
			case LOW:
				setLow(price); break;
			case CLOSE:
				setClose(price); break;
			default:
				break;
		}
		timestamp = System.currentTimeMillis();
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
		timestamp = System.currentTimeMillis();
	}

	@Override
	protected synchronized void onTickString(int tickType, String value) {
		switch (TickType.get(tickType)) {
			case LAST_TIMESTAMP:
				setLastTimestamp(Long.parseLong(value)); break;
			default:
				break;
		}
		timestamp = System.currentTimeMillis();
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

	private void setBid(double bid) {
		this.bid = bid;
	}
	public double getBid() {
		return bid;
	}
	private void setBidSize(int bidSize) {
		this.bidSize = bidSize;
	}
	public int getBidSize() {
		return bidSize;
	}
	private void setAsk(double ask) {
		this.ask = ask;
	}
	public double getAsk() {
		return ask;
	}
	private void setAskSize(int askSize) {
		this.askSize = askSize;
	}
	public int getAskSize() {
		return askSize;
	}
	private void setLast(double last) {
		this.last = last;
	}
	public double getLast() {
		return last;
	}
	private void setLastSize(int lastSize) {
		this.lastSize = lastSize;
	}
	public int getLastSize() {
		return lastSize;
	}
	private void setHigh(double high) {
		this.high = high;
	}
	public double getHigh() {
		return high;
	}
	private void setLow(double low) {
		this.low = low;
	}
	public double getLow() {
		return low;
	}
	private void setVolume(int volume) {
		this.volume = volume;
	}
	public int getVolume() {
		return volume;
	}
	private void setClose(double close) {
		this.close = close;
	}
	public double getClose() {
		return close;
	}
	private void setLastTimestamp(long lastTimestamp) {
		this.lastTimestamp = lastTimestamp;
	}
	public long getLastTimestamp() {
		return lastTimestamp;
	}

	public long getTimestamp() {
		return timestamp;
	}
}
