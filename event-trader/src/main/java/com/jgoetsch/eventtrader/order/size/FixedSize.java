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

import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.eventtrader.processor.ProcessorContext;
import com.jgoetsch.tradeframework.PropertyNotSetException;

public class FixedSize implements OrderSize {

	private int size;

	public FixedSize() {
	}

	public FixedSize(int size) {
		this.size = size;
	}

	public int getValue(TradeSignal trade, BigDecimal price, ProcessorContext context) {
		return getSize();
	}

	public void initialize() {
		if (size == 0)
			throw new PropertyNotSetException("size");
	}

	@Override
	public String toString() {
		return String.valueOf(size);
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}

}
