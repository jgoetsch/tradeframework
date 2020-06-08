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

import java.math.BigDecimal;

import com.jgoetsch.tradeframework.marketdata.MarketData;

public class TrailingLimitOrderProcessor extends TrailingStopOrderProcessor {

	private BigDecimal limitOffset;

	public TrailingLimitOrderProcessor(BigDecimal quantity, BigDecimal stopPrice, BigDecimal trailingAmount, BigDecimal limitPrice) {
		super(quantity, stopPrice, trailingAmount);
		this.limitOffset = stopPrice.subtract(limitPrice);
	}

	public TrailingLimitOrderProcessor(BigDecimal quantity, BigDecimal stopPrice, BigDecimal trailingAmount, BigDecimal limitPrice, TriggerMethod triggerMethod) {
		super(quantity, stopPrice, trailingAmount, triggerMethod);
		this.limitOffset = stopPrice.subtract(limitPrice);
	}

	@Override
	protected OrderProcessor onTriggered(MarketData marketData) {
		return new LimitOrderProcessor(getQuantityRemaining(), getStopPrice().subtract(getLimitOffset()));
	}

	protected void setLimitOffset(BigDecimal limitOffset) {
		this.limitOffset = limitOffset;
	}

	protected BigDecimal getLimitOffset() {
		return limitOffset;
	}
	
}
