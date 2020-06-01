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
package com.jgoetsch.eventtrader.order.price;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.function.Supplier;

import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.tradeframework.PropertyNotSetException;
import com.jgoetsch.tradeframework.data.DataUnavailableException;
import com.jgoetsch.tradeframework.marketdata.MarketData;

public class ConstrainedPrice implements OrderPrice {

	private Collection<? extends OrderPrice> prices;
	private boolean aggressive = false;

	public ConstrainedPrice() {
	}

	public ConstrainedPrice(Collection<? extends OrderPrice> prices) {
		if (prices == null || prices.isEmpty())
			throw new IllegalArgumentException("Must specify at least one OrderPrice");
		this.prices = prices;
	}

	public BigDecimal getValue(TradeSignal trade, Supplier<MarketData> marketData) throws DataUnavailableException {
		return prices.stream().map(p -> p.getValue(trade, marketData))
				.reduce(trade.isSell() == aggressive ? BigDecimal::min : BigDecimal::max).get();
	}

	public void initialize() {
		if (getPrices() == null || getPrices().size() == 0)
			throw new PropertyNotSetException("prices");
	}

	public void setAggressive(boolean aggressive) {
		this.aggressive = aggressive;
	}

	public boolean isAggressive() {
		return aggressive;
	}

	public void setPrices(Collection<OrderPrice> prices) {
		this.prices = prices;
	}

	public Collection<? extends OrderPrice> getPrices() {
		return prices;
	}

	@Override
	public String toString() {
		return prices.toString();
	}

}
