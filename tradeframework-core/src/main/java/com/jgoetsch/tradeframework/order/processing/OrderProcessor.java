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
package com.jgoetsch.tradeframework.order.processing;

import com.jgoetsch.tradeframework.Execution;
import com.jgoetsch.tradeframework.marketdata.MarketData;

public abstract class OrderProcessor {
	private final int totalQuantity;
	private int quantityRemaining;

	public OrderProcessor(int quantity) {
		this.totalQuantity = quantity;
		this.quantityRemaining = quantity;
	}

	public boolean isBuying() {
		return totalQuantity > 0;
	}

	public boolean isSelling() {
		return totalQuantity < 0;
	}

	/**
	 * Returns the number of shares in the order that have not yet been filled.
	 * @return number of shares remaining to be filled
	 */
	public final int getQuantityRemaining() {
		return quantityRemaining;
	}

	@Override
	public String toString() {
		return (isBuying() ? "BUY " : "SELL ") + totalQuantity + ": " + quantityRemaining + " remaining";
	}

	/**
	 * 
	 * @param marketData market data to process order against
	 * @return an Execution if any part of the order was executed on this tick, otherwise null
	 */
	public final Execution process(MarketData marketData) {
		Execution exec = handleProcessing(marketData);
		if (exec != null)
			quantityRemaining -= exec.getQuantity();
		return exec;
	}

	/**
	 * Overridable method to handle processing of an open order for each market data tick.
	 * This will continue to be called on every market data tick for the contract
	 * until the order is completely filled (specifically <code>getQuantityRemaining</code>
	 * returns 0) or the order is canceled.
	 * 
	 * @param marketData market data to process order against
	 * @return an Execution if any part of the order was executed on this tick, otherwise null
	 */
	protected abstract Execution handleProcessing(MarketData marketData);

}

