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

import java.util.HashMap;
import java.util.Map;

import com.jgoetsch.tradeframework.Contract;
import com.jgoetsch.tradeframework.data.HistoricalDataSource;

public class HistoricalMarketDataSource extends SimulatedMarketDataSource {

	private final HistoricalDataSource historicalDataSource;
	private final Map<Contract, HistoricalMarketDataFeed> historicalFeeds = new HashMap<Contract, HistoricalMarketDataFeed>();
	private final int dataPeriod;
	private final int bufferLength;

	public HistoricalMarketDataSource(Map<Contract, ? extends SimulatedMarketDataFeed> marketDataFeedMap, HistoricalDataSource historicalDataSource, long startTimestamp) {
		super(marketDataFeedMap, startTimestamp);
		this.historicalDataSource = historicalDataSource;
		this.dataPeriod = HistoricalDataSource.PERIOD_15_SECONDS;
		this.bufferLength = 1680;
	}

	public HistoricalMarketDataSource(HistoricalDataSource historicalDataSource, long startTimestamp) {
		super((Map<Contract, SimulatedMarketDataFeed>)null, startTimestamp);
		this.historicalDataSource = historicalDataSource;
		this.dataPeriod = HistoricalDataSource.PERIOD_15_SECONDS;
		this.bufferLength = 1680;
	}

	public HistoricalMarketDataSource(HistoricalDataSource historicalDataSource, long startTimestamp, int dataPeriod, int bufferLength) {
		super((Map<Contract, SimulatedMarketDataFeed>)null, startTimestamp);
		this.historicalDataSource = historicalDataSource;
		this.dataPeriod = dataPeriod;
		this.bufferLength = bufferLength;
	}

	@Override
	public synchronized SimulatedMarketDataFeed getDataFeed(Contract contract) {
		SimulatedMarketDataFeed dataFeed = super.getDataFeed(contract);
		if (dataFeed != null)
			return dataFeed;
		else {
			HistoricalMarketDataFeed historicalFeed = historicalFeeds.get(contract);
			if (historicalFeed != null)
				return historicalFeed;
			else {
				historicalFeed = new HistoricalMarketDataFeed(contract, historicalDataSource, getDataPeriod(), getBufferLength());
				historicalFeeds.put(contract, historicalFeed);
				return historicalFeed;
			}
		}
	}

	public int getDataPeriod() {
		return dataPeriod;
	}

	public int getBufferLength() {
		return bufferLength;
	}

}
