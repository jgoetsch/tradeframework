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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.function.UnaryOperator;

import com.jgoetsch.tradeframework.rounding.TickRounding;

/**
 * Simulates market data for a security with randomly moving price
 * 
 * @author jgoetsch
 *
 */
public class RandomMarketDataFeed extends SimulatedMarketDataFeed {

	private double volatility = 0.05;
	private double trend = 0;
	private UnaryOperator<BigDecimal> rounding = TickRounding.DEFAULT_STOCK;
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
	public RandomMarketDataFeed(BigDecimal startPrice, double volatility, long interval) {
		super();
		this.setStartPrice(startPrice);
		this.setVolatility(volatility);
		this.setInterval(interval);
	}

	public void initialize() {
		if (getLastTick() == null)
			throw new IllegalStateException("Required property startPrice is not set");
	}

	private BigDecimal randomIncrement() {
		setTrend((getTrend() + Math.random() * Math.signum(Math.random() - 0.5 + getTrend())) / 2);
		return BigDecimal.valueOf(Math.pow(Math.random(), 2) * Math.signum(Math.random() - 0.5 + trend) * volatility);
	}

	@Override
	public void advanceTo(Instant timestamp) {
		((SimpleMarketData)getLastTick()).setTimestamp(timestamp);
		setLastTick(retrieveNextTick());
	}

	@Override
	protected MarketData retrieveNextTick() {
		MarketData last = getLastTick();
		if (last == null || last.getTimestamp() == null)
			throw new IllegalStateException(getClass().getSimpleName() + " does not know what time it is");

		SimpleMarketData mkd = new SimpleMarketData();
		mkd.setLast(rounding.apply(last.getLast().add(randomIncrement())));
		mkd.setTimestamp(last.getTimestamp().plusMillis(interval));
		mkd.setLastTimestamp(mkd.getTimestamp());
		mkd.setAsk(rounding.apply(mkd.getLast().add(randomIncrement().abs())));
		mkd.setAskSize(10);
		mkd.setBid(rounding.apply(mkd.getLast().subtract(randomIncrement().abs())));
		mkd.setBidSize(10);
		mkd.setClose(mkd.getLast());
		mkd.setHigh(mkd.getLast().max(last.getHigh()));
		mkd.setLow(mkd.getLast().min(last.getLow()));
		return mkd;
	}


	public void close() {
	}

	public void setStartPrice(BigDecimal startPrice) {
		SimpleMarketData mkd = new SimpleMarketData();
		mkd.setLast(startPrice);
		mkd.setHigh(startPrice);
		mkd.setLow(startPrice);
		setLastTick(mkd);
	}

	public BigDecimal getStartPrice() {
		return getLastTick().getLast();
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

	public UnaryOperator<BigDecimal> getRounding() {
		return rounding;
	}

	public void setRounding(UnaryOperator<BigDecimal> rounding) {
		this.rounding = rounding;
	}

}
