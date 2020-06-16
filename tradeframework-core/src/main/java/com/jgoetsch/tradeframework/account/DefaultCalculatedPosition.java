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
package com.jgoetsch.tradeframework.account;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;

import com.jgoetsch.tradeframework.Contract;
import com.jgoetsch.tradeframework.Execution;
import com.jgoetsch.tradeframework.marketdata.MarketData;

/**
 * Position that uses average entry price for P/L calculations
 * 
 * @author jgoetsch
 *
 */
public class DefaultCalculatedPosition implements MutablePosition, ClosedPosition {

	private Contract contract;
	private BigDecimal curQuantity;
	private BigDecimal totalQuantity;
	private BigDecimal multiplier;
	private BigDecimal marketPrice;
	private BigDecimal avgEntryPrice = BigDecimal.ZERO;
	private BigDecimal avgExitPrice = BigDecimal.ZERO;
	private BigDecimal extPrice;
	private Instant entryDate;
	private Instant exitDate;
	private BigDecimal realizedPL = BigDecimal.ZERO;
	private BigDecimal commissions = BigDecimal.ZERO;

	public DefaultCalculatedPosition(Contract contract) {
		this.contract = contract;
		this.multiplier = BigDecimal.valueOf(contract.getMultiplier() != null ? contract.getMultiplier() : 1);
	}

	public Contract getContract() {
		return contract;
	}

	public final BigDecimal getQuantity() {
		return curQuantity;
	}
	
	public final BigDecimal getMarketPrice() {
		return marketPrice;
	}
	
	public final BigDecimal getMultiplier() {
		return multiplier;
	}

	public final BigDecimal getAvgPrice() {
		return avgEntryPrice;
	}

	public final BigDecimal getRealizedProfitLoss() {
		return realizedPL;
	}

	public final BigDecimal getUnrealizedProfitLoss() {
		return getPotentialProfitLoss(getMarketPrice(), getQuantity());
	}

	public final BigDecimal getValue() {
		return getMarketPrice().multiply(getQuantity()).multiply(getMultiplier());
	}

	public boolean exists() {
		return getQuantity().signum() != 0;
	}

	public BigDecimal trade(Execution exec) {
		marketPrice = exec.getPrice();
		if (exec.getQuantity().signum() == 0)
			throw new IllegalArgumentException("Execution cannot have quantity of 0");

		if (curQuantity.signum() == 0 || exec.getQuantity().signum() == curQuantity.signum()) {
			avgEntryPrice = avgEntryPrice.multiply(curQuantity).add(exec.getPrice().multiply(exec.getQuantity())).divide(curQuantity.add(exec.getQuantity()), RoundingMode.HALF_UP);
			if (entryDate == null)
				entryDate = exec.getDate();
		}
		else {
			if (exec.getQuantity().abs().compareTo(curQuantity.abs()) > 0)
				throw new UnsupportedOperationException("Liquidating more shares than current position size not supported: " + contract + " " + exec + ", position @" + curQuantity);
			realizedPL = realizedPL.add(getPotentialProfitLoss(exec.getPrice(), exec.getQuantity().negate()).subtract(exec.getCommission()));
			BigDecimal exitQty = totalQuantity.subtract(curQuantity);
			avgExitPrice = avgExitPrice.multiply(exitQty).add(exec.getPrice().multiply(exec.getQuantity()))
					.divide(exitQty.add(exec.getQuantity()));
			exitDate = exec.getDate();
			if (entryDate != null && exitDate != null && exitDate.isBefore(entryDate))
				throw new IllegalStateException("Date of position exit trade cannot be before entry date.");
		}

		curQuantity = curQuantity.add(exec.getQuantity());
		if (curQuantity.abs().compareTo(totalQuantity.abs()) > 0)
			totalQuantity = curQuantity;
		commissions = commissions.add(exec.getCommission());
		return exec.getPrice().multiply(exec.getQuantity().negate()).multiply(multiplier).subtract(exec.getCommission());
	}

	protected BigDecimal getPotentialProfitLoss(BigDecimal price, BigDecimal quantity) {
		return price.subtract(getAvgPrice()).multiply(quantity).multiply(getMultiplier());
	}

	public void setMarketPrice(MarketData marketData) {
		setMarketPrice(marketData.getLast());
	}
	
	protected final void setMarketPrice(BigDecimal marketPrice) {
		this.marketPrice = marketPrice;
		if (exists())
			extPrice = extPrice == null ? marketPrice : (getQuantity().signum() > 0 ? extPrice.max(marketPrice) : extPrice.min(marketPrice));
	}
	
	@Override
	public String toString() {
		NumberFormat pf = NumberFormat.getNumberInstance();
		if (getQuantity().signum() != 0)
			return getContract() + " " + getQuantity() + " shrs @" + avgEntryPrice + " avg, mkt @" + getMarketPrice() + ", mkt val = " + getValue();
		else
			return (getTransactionQuantity().signum() > 0 ? "long " : "short ") + getTransactionQuantity()
				+ " shrs, entry @" + pf.format(getEntryPrice()) + ", exit @" + pf.format(getExitPrice())
				+ ", reached " + pf.format(getExtentPrice())
				+ ", " + new DecimalFormat("+0.00%;-0.00%").format(getPercentGain());
	}

	public BigDecimal getEntryPrice() {
		return getAvgPrice();
	}

	public BigDecimal getExitPrice() {
		return avgExitPrice;
	}

	public BigDecimal getExtentPrice() {
		return extPrice;
	}

	public BigDecimal getTransactionQuantity() {
		return totalQuantity;
	}

	public BigDecimal getPercentGain() {
		BigDecimal pct = getExitPrice().subtract(getEntryPrice()).divide(getEntryPrice(), 3, RoundingMode.HALF_UP);
		return pct.multiply(BigDecimal.valueOf(totalQuantity.signum()));
	}

	public BigDecimal getMaxUnrealizedPercent() {
		BigDecimal pct = getExtentPrice().subtract(getEntryPrice()).divide(getEntryPrice(), 3, RoundingMode.HALF_UP);
		return pct.multiply(BigDecimal.valueOf(totalQuantity.signum()));
	}

	public Instant getEntryDate() {
		return entryDate;
	}

	public Instant getExitDate() {
		return exitDate;
	}

	public BigDecimal getCommisions() {
		return commissions;
	}

}
