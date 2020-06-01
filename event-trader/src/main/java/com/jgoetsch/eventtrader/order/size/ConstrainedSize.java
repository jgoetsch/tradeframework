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

import java.util.Collection;

import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.eventtrader.processor.ProcessorContext;
import com.jgoetsch.tradeframework.PropertyNotSetException;

public class ConstrainedSize implements OrderSize {

	private Collection<OrderSize> sizes;
	private boolean aggressive = false;

	public ConstrainedSize() {
	}

	public ConstrainedSize(Collection<OrderSize> sizes) {
		this.sizes = sizes;
	}

	public int getValue(TradeSignal trade, double price, ProcessorContext context) {
		int size = -1;
		for (OrderSize calc : getSizes()) {
			int calcSize = calc.getValue(trade, price, context);
			if (size == -1)
				size = calcSize;
			else
				size = aggressive ? Math.max(size, calcSize) : Math.min(size, calcSize);
		}
		return size;
	}

	public void initialize() {
		if (getSizes() == null || getSizes().size() == 0)
			throw new PropertyNotSetException("sizes");
	}

	@Override
	public String toString() {
		return sizes.toString();
	}

	public void setSizes(Collection<OrderSize> sizes) {
		this.sizes = sizes;
	}

	public Collection<OrderSize> getSizes() {
		return sizes;
	}

}
