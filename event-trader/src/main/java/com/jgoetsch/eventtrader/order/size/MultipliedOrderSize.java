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

import java.text.NumberFormat;
import java.util.Map;

import com.jgoetsch.eventtrader.TradeSignal;

public abstract class MultipliedOrderSize implements OrderSize {

	private double multiplier = 1;

	public int getValue(TradeSignal trade, double price, Map<Object, Object> context) {
		return (int)(getBaseValue(trade, price, context) * multiplier);
	}

	protected abstract int getBaseValue(TradeSignal trade, double price, Map<Object, Object> context);

	@Override
	public String toString() {
		return getClass().getSimpleName() + (multiplier != 1 ? " * " + NumberFormat.getNumberInstance().format(multiplier) : "");
	}

	public void setMultiplier(double multiplier) {
		this.multiplier = multiplier;
	}

	public double getMultiplier() {
		return multiplier;
	}

}
