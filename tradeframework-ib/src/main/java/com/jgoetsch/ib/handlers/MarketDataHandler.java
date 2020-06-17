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

import com.ib.client.TickAttrib;
import com.ib.client.TickType;
import com.jgoetsch.tradeframework.BrokerResponseException;
import com.jgoetsch.tradeframework.marketdata.MarketData;
import com.jgoetsch.tradeframework.marketdata.SimpleMarketData;

public class MarketDataHandler extends BaseIdHandler<MarketData> {

	private SimpleMarketData data = new SimpleMarketData();

	public MarketDataHandler(int tickerId) {
		super(tickerId);
	}

	public MarketDataHandler(int tickerId, HandlerManager manager) {
		super(tickerId, manager);
	}

	@Override
	protected void onTickPrice(int field, double price, TickAttrib attrib) {
		BigDecimal decimal = BigDecimal.valueOf(price);
		switch (TickType.get(field)) {
			case BID:
				data.setBid(decimal); break;
			case ASK:
				data.setAsk(decimal); break;
			case LAST:
				data.setLast(decimal); break;
			case HIGH:
				data.setHigh(decimal); break;
			case LOW:
				data.setLow(decimal); break;
			case CLOSE:
				data.setClose(decimal); break;
			default:
		}
		data.setTimestamp(Instant.now());
	}

	@Override
	protected void onTickSize(int field, int size) {
		switch (TickType.get(field)) {
			case BID_SIZE:
				data.setBidSize(size); break;
			case ASK_SIZE:
				data.setAskSize(size); break;
			case LAST_SIZE:
				data.setLastSize(size); break;
			case VOLUME:
				data.setVolume(size); break;
			default:
		}
		data.setTimestamp(Instant.now());
	}

	@Override
	protected void onTickString(int tickType, String value) {
		switch (TickType.get(tickType)) {
			case LAST_TIMESTAMP:
				data.setLastTimestamp(Instant.ofEpochMilli(Long.parseLong(value))); break;
			default:
		}
		data.setTimestamp(Instant.now());
	}

	@Override
	protected void onTickSnapshotEnd() {
		getCompletableFuture().complete(getMarketData());
	}
	@Override
	protected void onError(int errorCode, String errorMsg) {
		getCompletableFuture().completeExceptionally(new BrokerResponseException(errorCode, errorMsg));
	}

	protected final MarketData getMarketData() {
		return new SimpleMarketData(data);
	}

	@Override
	public String toString() {
		return data.toString();
	}

}
