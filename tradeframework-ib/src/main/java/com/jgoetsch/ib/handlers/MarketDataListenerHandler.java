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

import java.util.HashSet;
import java.util.Set;

import com.ib.client.TickAttrib;
import com.jgoetsch.tradeframework.Contract;
import com.jgoetsch.tradeframework.marketdata.MarketDataListener;

public class MarketDataListenerHandler extends MarketDataHandler {

	private Contract contract;
	private Set<MarketDataListener> listeners = new HashSet<MarketDataListener>();
	
	public MarketDataListenerHandler(int tickerId, Contract contract) {
		super(tickerId);
		this.contract = contract;
	}

	public final boolean addListener(MarketDataListener listener) {
		return listeners.add(listener);
	}
	
	public final boolean removeListener(MarketDataListener listener) {
		return listeners.remove(listener);
	}

	public final boolean hasListeners() {
		return !listeners.isEmpty();
	}

	@Override
	protected synchronized void onTickPrice(int field, double price, TickAttrib attrib) {
		super.onTickPrice(field, price, attrib);
		if (getStatus() == STATUS_SUCCESS) {
			for (MarketDataListener listener : listeners)
				listener.tick(contract, this);
		}
	}

	@Override
	protected synchronized void onTickSize(int field, int size) {
		super.onTickSize(field, size);
		if (getStatus() == STATUS_SUCCESS)
			for (MarketDataListener listener : listeners)
				listener.tick(contract, this);
	}

	@Override
	protected synchronized void onTickString(int tickType, String value) {
		super.onTickString(tickType, value);
		if (getStatus() == STATUS_SUCCESS)
			for (MarketDataListener listener : listeners)
				listener.tick(contract, this);
	}

}
