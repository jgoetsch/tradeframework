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

public class RoundedOrderSize implements OrderSize {

	private OrderSize size;
	private int roundToSize = 1;

	public RoundedOrderSize() {
	}

	public RoundedOrderSize(OrderSize size, int roundToSize) {
		this.size = size;
		this.roundToSize = roundToSize;
	}

	public int getValue(TradeSignal trade, double price, Map<Object, Object> context) {
		return (size.getValue(trade, price, context) / roundToSize) * roundToSize;
	}

	@Override
	public String toString() {
		return "[" + size + " round to " + roundToSize + "]";
	}

	public void setSize(OrderSize size) {
		this.size = size;
	}

	public OrderSize getSize() {
		return size;
	}

	public void setRoundToSize(int roundToSize) {
		this.roundToSize = roundToSize;
	}

	public int getRoundToSize() {
		return roundToSize;
	}

}
