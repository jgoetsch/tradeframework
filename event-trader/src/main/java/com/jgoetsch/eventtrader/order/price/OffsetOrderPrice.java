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
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private BigDecimal offset = BigDecimal.ZERO;
	private boolean isPercentage = false;
	private UnaryOperator<BigDecimal> buyTickRounding = TickRounding.DEFAULT_STOCK_BUY;
	private UnaryOperator<BigDecimal> sellTickRounding = TickRounding.DEFAULT_STOCK_SELL;
	Logger log = LoggerFactory.getLogger(this.getClass());

	public final BigDecimal getValue(TradeSignal trade, Supplier<MarketData> marketData) throws DataUnavailableException {
		BigDecimal base = getBaseValue(trade, marketData);
		if (base != null) {
			BigDecimal calculated =
					(trade.isSell() ? sellTickRounding : buyTickRounding)
					.apply(base.add(getActualOffset(trade.isSell(), base)));
			log.trace("[{}] calculated price {} from base {}", toString(), calculated, base);
			return calculated;
		}
		else
			throw new DataUnavailableException("Required data for " + getClass().getSimpleName() + " not available");
	}

	/**
	 * Override to provide the base price to use from the MarketData before
	 * the offset is applied.
	 * 
	 * @param trade
	 * @param marketData
	 * @return base price extracted from the marketData
	 */
	protected abstract BigDecimal getBaseValue(TradeSignal trade, Supplier<MarketData> marketData);

	protected final BigDecimal fromDouble(double value) throws DataUnavailableException {
		return value != 0 ? BigDecimal.valueOf(value) : null;
	}

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
	protected BigDecimal getActualOffset(boolean isSell, BigDecimal basePrice) {
		BigDecimal offs = offset;
		if (isSell)
			offs = offs.negate();
		if (isPercentage())
			offs = offs.multiply(basePrice);
		return offs;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append(offset.signum() < 0 ? "-" : "+");
		if (isPercentage)
			builder.append(offset.abs().movePointRight(2)).append("%");
		else
			builder.append(offset.abs());
		return builder.toString();
	}

	public final void setOffset(BigDecimal offset) {
		this.offset = offset;
	}

	public final BigDecimal getOffset() {
		return offset;
	}

	public final void setPercentage(boolean isPercentage) {
		this.isPercentage = isPercentage;
	}

	public final boolean isPercentage() {
		return isPercentage;
	}

	public UnaryOperator<BigDecimal> getBuyTickRounding() {
		return buyTickRounding;
	}

	public void setBuyTickRounding(UnaryOperator<BigDecimal> tickRounding) {
		this.buyTickRounding = tickRounding;
	}

	public UnaryOperator<BigDecimal> getSellTickRounding() {
		return sellTickRounding;
	}

	public void setSellTickRounding(UnaryOperator<BigDecimal> tickRounding) {
		this.sellTickRounding = tickRounding;
	}

}
