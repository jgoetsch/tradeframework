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

import java.math.BigDecimal;
import java.util.function.UnaryOperator;

import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.eventtrader.order.price.TickRounding;
import com.jgoetsch.eventtrader.processor.ProcessorContext;

public class RoundedOrderSize implements OrderSize {

	private OrderSize size;
	private UnaryOperator<BigDecimal> tickRounding = TickRounding.ROUND_TO_ONE;

	public RoundedOrderSize() {
	}

	public RoundedOrderSize(OrderSize size, UnaryOperator<BigDecimal> tickRounding) {
		this.size = size;
		this.tickRounding = tickRounding;
	}

	public int getValue(TradeSignal trade, double price, ProcessorContext context) {
		return tickRounding.apply(BigDecimal.valueOf(size.getValue(trade, price, context))).intValue();
	}

	@Override
	public String toString() {
		return size.toString() + " (" + tickRounding + ")";
	}

	public void setSize(OrderSize size) {
		this.size = size;
	}

	public OrderSize getSize() {
		return size;
	}

	public void setTickRounding(UnaryOperator<BigDecimal> tickRounding) {
		this.tickRounding = tickRounding;
	}

	public UnaryOperator<BigDecimal> getTickRounding() {
		return tickRounding;
	}

}
