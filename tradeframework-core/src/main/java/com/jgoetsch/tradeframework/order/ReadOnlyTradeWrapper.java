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
package com.jgoetsch.tradeframework.order;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import com.jgoetsch.tradeframework.InvalidContractException;
import com.jgoetsch.tradeframework.Order;

/**
 * TradingService decorator that provides a read only view that will
 * not allow any live orders to be transmitted through an underlying
 * TradingService. This can be useful as a protective mechanism for
 * programs that do not need to place orders or for testing purposes.
 * 
 * @author jgoetsch
 *
 */
public final class ReadOnlyTradeWrapper implements TradingService {

	private TradingService tradingService;
	private boolean bPlaceUntransmittedOrder;

	/**
	 * Constructs a read only view onto an underlying TradingService object.
	 * 
	 * @param tradingService	The underlying TradingService to decorate.
	 */
	public ReadOnlyTradeWrapper(TradingService tradingService) {
		this.tradingService = tradingService;
		this.bPlaceUntransmittedOrder = false;
	}

	/**
	 * Constructs a read only view onto an underlying TradingService object
	 * with the option of entering orders in an untransmitted state.
	 * 
	 * @param tradingService			The underlying TradingService to decorate.
	 * @param bPlaceUntransmittedOrder	if true, orders will be entered into TWS but not
	 * 									transmitted, if false, orders will be ignored
	 * 									completely.
	 */
	public ReadOnlyTradeWrapper(TradingService tradingService, boolean bPlaceUntransmittedOrder) {
		this.tradingService = tradingService;
		this.bPlaceUntransmittedOrder = bPlaceUntransmittedOrder;
	}

	@Override
	public CompletableFuture<Order> previewOrder(Order order) throws InvalidContractException, OrderException, IOException {
		return tradingService.previewOrder(order);
	}

	public CompletableFuture<Order> placeOrder(Order order) throws InvalidContractException, OrderException, IOException {
		if (bPlaceUntransmittedOrder) {
			return tradingService.previewOrder(order);
		}
		else
			return CompletableFuture.completedFuture(null);
	}

	public void subscribeExecutions(ExecutionListener listener) {
		tradingService.subscribeExecutions(listener);
	}

	public void cancelExecutionSubscription(ExecutionListener listener) {
		tradingService.cancelExecutionSubscription(listener);
	}

	public void close() throws IOException {
		tradingService.close();
	}

}
