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
package com.jgoetsch.eventtrader.order;

import java.util.function.Supplier;

import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.eventtrader.order.price.OrderPrice;
import com.jgoetsch.tradeframework.Order;
import com.jgoetsch.tradeframework.PropertyNotSetException;
import com.jgoetsch.tradeframework.data.DataUnavailableException;
import com.jgoetsch.tradeframework.marketdata.MarketData;
import com.jgoetsch.tradeframework.order.OrderException;

/**
 * Places a limit order with the limit offset from the bid/ask by the
 * specified amount or percentage.
 * 
 * @author jgoetsch
 *
 */
public class LimitOrderExecutor extends MarketOrderExecutor {

	private OrderPrice limitPrice;
	private boolean allowOutsideRth;

	@Override
	protected void prepareOrder(Order order, TradeSignal trade, Supplier<MarketData> marketData) throws OrderException, DataUnavailableException
	{
		order.setType(Order.TYPE_LIMIT);
		order.setLimitPrice(limitPrice.getValue(trade, marketData).doubleValue());
		order.setAllowOutsideRth(allowOutsideRth);
	}

	public void initialize() {
		if (getLimitPrice() == null)
			throw new PropertyNotSetException("limitPrice");
	}

	@Override
	protected double getIntendedPrice(TradeSignal trade, Supplier<MarketData> marketData) throws DataUnavailableException {
		return limitPrice.getValue(trade, marketData).doubleValue();
	}

	public void setLimitPrice(OrderPrice limitPrice) {
		this.limitPrice = limitPrice;
	}

	public OrderPrice getLimitPrice() {
		return limitPrice;
	}

	public boolean isAllowOutsideRth() {
		return allowOutsideRth;
	}

	public void setAllowOutsideRth(boolean allowOutsideRth) {
		this.allowOutsideRth = allowOutsideRth;
	}

}
