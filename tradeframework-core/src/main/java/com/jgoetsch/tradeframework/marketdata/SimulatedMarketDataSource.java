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

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.tradeframework.Contract;
import com.jgoetsch.tradeframework.InvalidContractException;

public class SimulatedMarketDataSource implements MarketDataSource, TimeAdvanceable {

	@SuppressWarnings("unused")
	private Logger log = LoggerFactory.getLogger(SimulatedMarketDataSource.class);

	private SimulatedMarketDataFeed singleMarketDataFeed;
	private Map<Contract, SimulatedMarketDataFeed> marketDataFeedMap;
	private Instant curTimestamp;

	private Map<Contract, Set<MarketDataListener>> listenerMap = new HashMap<Contract, Set<MarketDataListener>>();

	public SimulatedMarketDataSource() {
		curTimestamp = Instant.now();
	}
	
	public SimulatedMarketDataSource(Map<Contract, ? extends SimulatedMarketDataFeed> marketDataFeedMap, Instant startTimestamp) {
		if (marketDataFeedMap != null)
			this.setMarketDataFeedMap(Collections.unmodifiableMap(marketDataFeedMap));
		this.setStartTimestamp(startTimestamp);
	}

	public SimulatedMarketDataSource(SimulatedMarketDataFeed marketDataFeed, Instant startTimestamp) {
		this.setMarketDataFeed(marketDataFeed);
		this.setStartTimestamp(startTimestamp);
	}

	public SimulatedMarketDataSource(SimulatedMarketDataFeed marketDataFeed) throws IOException {
		this.setMarketDataFeed(marketDataFeed);
		try {
			this.setStartTimestamp(marketDataFeed.nextTick().getLastTimestamp());
		} catch (InvalidContractException e) {
			throw new RuntimeException(e);
		}
	}

	public SimulatedMarketDataFeed getDataFeed(Contract contract) {
		return getMarketDataFeedMap() != null ? getMarketDataFeedMap().get(contract) : getMarketDataFeed();
	}

	protected final Instant getCurTimestamp() {
		return getStartTimestamp();
	}

	public final void subscribeMarketData(Contract contract, MarketDataListener marketDataListener) throws IOException, InvalidContractException {
		SimulatedMarketDataFeed dataFeed = getDataFeed(contract);
		if (dataFeed == null)
			throw new SimulatedDataNotAvailableException(contract);
		else {
			synchronized (this) {
				Set<MarketDataListener> listeners = listenerMap.get(contract);
				if (listeners == null) {
					dataFeed.advanceTo(getStartTimestamp());
					listeners = new HashSet<MarketDataListener>(3);
					listenerMap.put(contract, listeners);
				}
				listeners.add(marketDataListener);
			}
			if (dataFeed.getLastTick() != null)
				marketDataListener.tick(contract, dataFeed.getLastTick());
		}
	}

	public synchronized final void cancelMarketData(Contract contract, MarketDataListener marketDataListener) {
		Set<MarketDataListener> listeners = listenerMap.get(contract);
		if (listeners != null) {
			listeners.remove(marketDataListener);
			if (listeners.isEmpty())
				listenerMap.remove(contract);
		}
	}

	public MarketData getDataSnapshot(Contract contract) throws IOException, InvalidContractException {
		return getMktDataSnapshot(contract).join();
	}

	public final CompletableFuture<MarketData> getMktDataSnapshot(Contract contract) {
		SimulatedMarketDataFeed dataFeed = getDataFeed(contract);
		if (dataFeed == null)
			return CompletableFuture.failedFuture(new SimulatedDataNotAvailableException(contract));
		else {
			synchronized (this) {
				if (!listenerMap.containsKey(contract)) {
					try {
						dataFeed.advanceTo(getStartTimestamp());
					} catch (IOException | InvalidContractException ex) {
						return CompletableFuture.failedFuture(ex);
					}
				}
				return CompletableFuture.completedFuture(dataFeed.getLastTick());
			}
		}
	}

	public synchronized final boolean receiveDataAndWait(long millis) throws IOException {
		long chunkLength = 15000;
		TreeMap<MarketData, Contract> chunkTicks = new TreeMap<MarketData, Contract>(new MarketDataTimestampComparator());
		setStartTimestamp(getStartTimestamp().plusMillis(millis));

		for (Instant newTimestamp = getStartTimestamp().plusMillis(chunkLength - millis); !newTimestamp.isAfter(getStartTimestamp()); newTimestamp.plusMillis(chunkLength)) {
			chunkTicks.clear();
			boolean hasMoreData = false;
			for (Contract contract : listenerMap.keySet()) {
				SimulatedMarketDataFeed dataFeed = getDataFeed(contract);
				List<MarketData> ticks;
				try {
					ticks = dataFeed.getTicksUpTo(newTimestamp.isBefore(getStartTimestamp()) ? newTimestamp : getStartTimestamp());
					if (ticks != null) {
						hasMoreData = true;
						for (MarketData tick : ticks)
							chunkTicks.put(tick, contract);
					}
				} catch (InvalidContractException e) {
					throw new RuntimeException(e);
				}
			}
			if (!hasMoreData)
				return false;

			for (Map.Entry<MarketData, Contract> tickEntry : chunkTicks.entrySet()) {
				Set<MarketDataListener> listeners = listenerMap.get(tickEntry.getValue());
				if (listeners != null) {
					for (MarketDataListener listener : new HashSet<MarketDataListener>(listeners)) {
						listener.tick(tickEntry.getValue(), tickEntry.getKey());
					}
				}
			}
		}
		return true;
	}

	public void close() throws IOException {
		if (getMarketDataFeed() != null)
			getMarketDataFeed().close();
		if (getMarketDataFeedMap() != null) {
			for (SimulatedMarketDataFeed dataFeed : getMarketDataFeedMap().values())
				dataFeed.close();
		}
	}

	public void setMarketDataFeed(SimulatedMarketDataFeed singleMarketDataFeed) {
		this.singleMarketDataFeed = singleMarketDataFeed;
	}

	public SimulatedMarketDataFeed getMarketDataFeed() {
		return singleMarketDataFeed;
	}

	public void setMarketDataFeedMap(Map<Contract, SimulatedMarketDataFeed> marketDataFeedMap) {
		this.marketDataFeedMap = marketDataFeedMap;
	}

	public Map<Contract, SimulatedMarketDataFeed> getMarketDataFeedMap() {
		return marketDataFeedMap;
	}

	public void setStartTimestamp(Instant startTimestamp) {
		this.curTimestamp = startTimestamp;
	}

	public Instant getStartTimestamp() {
		return curTimestamp;
	}
}
