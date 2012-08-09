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


/**
 * Simulates market data for a security with randomly moving price
 * 
 * @author jgoetsch
 *
 */
public class RandomMarketDataFeed extends SimulatedMarketDataFeed {

	private double incrementUnit = 0.01;
	private double volatility = 0.05;
	private double trend = 0;
	private long interval = 1000;

	public RandomMarketDataFeed() {
	}

	/**
	 * Constructs a new RandomMarketData object.
	 * 
	 * @param startPrice	the price to start at
	 * @param incrementUnit	the minimum unit of price change
	 * @param volatility	the maximum change in price between ticks
	 * @param length		number of ticks to generate
	 */
	public RandomMarketDataFeed(double startPrice, double incrementUnit, double volatility, long interval) {
		super();
		this.setStartPrice(startPrice);
		this.setIncrementUnit(incrementUnit);
		this.setVolatility(volatility);
		this.setTrend(0);
	}

	public void initialize() {
		if (getLastTick() == null)
			throw new IllegalStateException("Required property startPrice is not set");
	}

	private double randomIncrement() {
		setTrend((getTrend() + Math.random() * Math.signum(Math.random() - 0.5 + getTrend())) / 2);
		double inc = Math.pow(Math.random(), 2) * Math.signum(Math.random() - 0.5 + trend) * volatility;
		double norm = incrementUnit <= 1.0 ? (1 / incrementUnit) : incrementUnit;
		return Math.round(inc * norm) / norm;
	}

	@Override
	public void advanceTo(long timestamp) {
		((SimpleMarketData)getLastTick()).setTimestamp(timestamp);
		setLastTick(retrieveNextTick());
	}

	@Override
	protected MarketData retrieveNextTick() {
		MarketData last = getLastTick();
		if (last == null || last.getTimestamp() == 0)
			throw new IllegalStateException(getClass().getSimpleName() + " does not know what time it is");

		SimpleMarketData mkd = new SimpleMarketData();
		mkd.setLast(last.getLast() + randomIncrement());
		mkd.setTimestamp(last.getTimestamp() + interval);
		mkd.setLastTimestamp(mkd.getTimestamp());
		mkd.setAsk(mkd.getLast() + Math.abs(randomIncrement()));
		mkd.setAskSize(10);
		mkd.setBid(mkd.getLast() - Math.abs(randomIncrement()));
		mkd.setBidSize(10);
		mkd.setClose(mkd.getLast());
		mkd.setHigh(Math.max(last.getHigh(), mkd.getLast()));
		mkd.setLow(Math.min(last.getLow(), mkd.getLast()));
		return mkd;
	}


	public void close() {
	}

	public void setStartPrice(double startPrice) {
		SimpleMarketData mkd = new SimpleMarketData();
		mkd.setLast(startPrice);
		mkd.setHigh(startPrice);
		mkd.setLow(startPrice);
		setLastTick(mkd);
	}

	public double getStartPrice() {
		return getLastTick() != null ? getLastTick().getLast() : 0.0;
	}

	public void setIncrementUnit(double incrementUnit) {
		this.incrementUnit = incrementUnit;
	}

	public double getIncrementUnit() {
		return incrementUnit;
	}

	public void setVolatility(double volatility) {
		this.volatility = volatility;
	}

	public double getVolatility() {
		return volatility;
	}

	public void setTrend(double trend) {
		this.trend = trend;
	}

	public double getTrend() {
		return trend;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public long getInterval() {
		return interval;
	}

}
