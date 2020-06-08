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
import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.eventtrader.processor.Processor;
import com.jgoetsch.eventtrader.processor.ProcessorContext;
import com.jgoetsch.tradeframework.InvalidContractException;
import com.jgoetsch.tradeframework.Order;
import com.jgoetsch.tradeframework.StandardOrder;
import com.jgoetsch.tradeframework.order.OrderException;
import com.jgoetsch.tradeframework.order.TradingService;

public class BasicOrderExecutor implements Processor<TradeSignal> {

	private Logger log = LoggerFactory.getLogger(BasicOrderExecutor.class);
	private TradingService tradingService;

	public BasicOrderExecutor() {
	}

	public BasicOrderExecutor(TradingService tradingService) {
		this.tradingService = tradingService;
	}

	public void process(TradeSignal trade, ProcessorContext context) throws OrderException, IOException {
		StandardOrder order = createOrder(trade);
		if (getTradingService() != null) {
			if (order.getQuantity().signum() == 0)
				throw new IllegalArgumentException("Number of shares not known");
			log.info("Placing order to " + order);
			try {
				getTradingService().placeOrder(trade.getContract(), order);
			}
			catch (InvalidContractException e) {
				log.warn(e.getMessage());
			}
		}
		else
			log.info("No trading service connected, order " + trade.getContract() + " " + order + " not placed");
	}

	protected StandardOrder createOrder(TradeSignal trade) {
		return Order.marketOrder(BigDecimal.valueOf(trade.getType().isBuy() ? trade.getNumShares() : -trade.getNumShares()));
	}

	public TradingService getTradingService() {
		return tradingService;
	}

	public void setTradingService(TradingService tradingService) {
		this.tradingService = tradingService;
	}

}
