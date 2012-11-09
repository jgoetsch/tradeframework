package com.jgoetsch.tradeframework.marketdata;

import java.io.IOException;

import com.jgoetsch.tradeframework.Contract;

/**
 * A trivial mocked market data source that produces the supplied single tick
 * of market data.
 * 
 * @author jgoetsch
 *
 */
public class ConstantMarketDataSource implements MarketDataSource {

	private MarketData marketData;

	public ConstantMarketDataSource() {
	}

	public ConstantMarketDataSource(MarketData marketData) {
		this.marketData = marketData;
	}

	public MarketData getDataSnapshot(Contract contract) {
		return getMktDataSnapshot(contract);
	}

	public MarketData getMktDataSnapshot(Contract contract) {
		return marketData;
	}

	public void subscribeMarketData(Contract contract, MarketDataListener marketDataListener) {
		marketDataListener.tick(contract, marketData);
	}

	public void cancelMarketData(Contract contract, MarketDataListener marketDataListener) {
	}

	public void close() throws IOException {
	}

	public MarketData getMarketData() {
		return marketData;
	}

	public void setMarketData(MarketData marketData) {
		this.marketData = marketData;
	}

}
