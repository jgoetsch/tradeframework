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
package com.jgoetsch.eventtrader.order.price;

import java.math.BigDecimal;
import java.text.NumberFormat;

import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.tradeframework.data.DataUnavailableException;
import com.jgoetsch.tradeframework.marketdata.MarketData;

/**
 * Abstract PriceCalculation whose resulting price can be offset by a predetermined amount.
 * 
 * @author jgoetsch
 *
 */
public abstract class OffsetOrderPrice implements OrderPrice {
	private double offset = 0.0;
	private boolean isPercentage = false;
	private TickRounding tickRounding = TickRounding.DEFAULT_STOCK;

	public final double getValue(TradeSignal trade, MarketData marketData) throws DataUnavailableException {
		double base = getBaseValue(trade, marketData);
		if (base == 0)
			throw new DataUnavailableException("Market data for " + getClass().getSimpleName() + " not available");

		BigDecimal baseDecimal = BigDecimal.valueOf(base);
		BigDecimal offs = getOffset(trade.getType().isSell(), baseDecimal);
		return tickRounding.roundToTick(baseDecimal.add(offs), trade.getType().isSell()).doubleValue();
	}

	/**
	 * Override to provide the base price to use from the MarketData before
	 * the offset is applied.
	 * 
	 * @param trade
	 * @param marketData
	 * @return base price extracted from the marketData
	 */
	protected abstract Double getBaseValue(TradeSignal trade, MarketData marketData) throws DataUnavailableException;

	/**
	 * Returns the actual offset amount to be added to the base price. By
	 * default the fixed offset is added to the price if buying, or subtracted
	 * if selling. Can be overridden in order to dynamically modify the offset
	 * based on trade or market data.
	 * 
	 * @param trade
	 * @param marketData
	 * @return offset to add to base price
	 */
	protected BigDecimal getOffset(boolean isSell, BigDecimal basePrice) {
		BigDecimal offs = BigDecimal.valueOf(getOffset());
		if (isSell)
			offs = offs.negate();
		if (isPercentage())
			offs = offs.multiply(basePrice);
		return offs;
	}

	@Override
	public String toString() {
		NumberFormat nf = isPercentage ? NumberFormat.getPercentInstance() : NumberFormat.getNumberInstance();
		return getClass().getSimpleName() + (offset >= 0 ? " + " : " - ") + nf.format(Math.abs(offset));
	}

	public final void setOffset(double offset) {
		this.offset = offset;
	}

	public final double getOffset() {
		return offset;
	}

	public final void setPercentage(boolean isPercentage) {
		this.isPercentage = isPercentage;
	}

	public final boolean isPercentage() {
		return isPercentage;
	}

	public TickRounding getTickRounding() {
		return tickRounding;
	}

	public void setTickRounding(TickRounding tickRounding) {
		this.tickRounding = tickRounding;
	}

}
