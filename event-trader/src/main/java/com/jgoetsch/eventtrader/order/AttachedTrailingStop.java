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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.tradeframework.InvalidContractException;
import com.jgoetsch.tradeframework.Order;
import com.jgoetsch.tradeframework.data.DataUnavailableException;
import com.jgoetsch.tradeframework.marketdata.MarketData;
import com.jgoetsch.tradeframework.order.OrderException;

@Deprecated
public class AttachedTrailingStop extends MarketOrderExecutor {

	private Logger log = LoggerFactory.getLogger(AttachedTrailingStop.class);
	private MarketOrderExecutor baseExecutor;
	private double trailPercent;

	public final boolean handleProcessing(TradeSignal trade, MarketData marketData) throws OrderException, IOException {
		Order baseOrder = new Order();
		try {
			baseExecutor.prepareOrder(baseOrder, trade, marketData);
		} catch (DataUnavailableException du) {
			log.warn("Could not processs order because: " + du.getMessage());
			return false;
		}

		if (baseOrder != null) {
			Order order = createAttachedOrder(trade, marketData, baseOrder);
			try {
				getTradingService().placeOrder(trade.getContract(), baseOrder);
				getTradingService().placeOrder(trade.getContract(), order);
				return true;
			} catch (InvalidContractException e) {
				log.warn("Invalid contract", e);
			}
		}
		return false;
	}

	protected Order createAttachedOrder(TradeSignal trade, MarketData marketData, Order baseOrder) throws OrderException, IOException
	{
		double price = baseOrder.getLimitPrice();
		if (price <= 0)
			price = marketData.getLast();

		double trailAmt = price * trailPercent + .01;
		return Order.trailingStopOrder(-baseOrder.getQuantity(),
				trade.isBuy() ? price - trailAmt : price + trailAmt, trailAmt);
	}

	protected final Order createOrder(TradeSignal trade, MarketData marketData) {
		throw new UnsupportedOperationException();
	}

	public void setBaseExecutor(MarketOrderExecutor baseExecutor) {
		this.baseExecutor = baseExecutor;
	}

	public MarketOrderExecutor getBaseExecutor() {
		return baseExecutor;
	}
}
