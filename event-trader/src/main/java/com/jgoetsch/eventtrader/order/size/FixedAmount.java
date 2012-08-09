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
package com.jgoetsch.eventtrader.order.size;

import java.util.Map;

import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.tradeframework.PropertyNotSetException;

/**
 * OrderSize evaluator for a fixed dollar amount based on the estimated
 * trade price.
 * 
 * @author jgoetsch
 * 
 */
public class FixedAmount implements OrderSize {

	private double amount;

	public FixedAmount() {
	}

	public FixedAmount(double amount) {
		this.amount = amount;
	}

	public int getValue(TradeSignal trade, double price, Map<Object, Object> context) {
		return (int)(getAmount() / price);
	}

	public void initialize() {
		if (amount == 0)
			throw new PropertyNotSetException("amount");
	}

	@Override
	public String toString() {
		return "FixedAmount: $" + amount;
	}

	public final void setAmount(double amount) {
		this.amount = amount;
	}

	public final double getAmount() {
		return amount;
	}

}
