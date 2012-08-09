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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;

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
	private int curQuantity;
	private int totalQuantity;
	private long multiplier;
	private double marketPrice;
	private double avgEntryPrice = 0;
	private double avgExitPrice = 0;
	private double extPrice = 0;
	private Date entryDate;
	private Date exitDate;
	private double realizedPL = 0;
	private double commissions = 0;

	public DefaultCalculatedPosition(Contract contract) {
		this.contract = contract;
		this.multiplier = contract.getMultiplier();
	}

	public Contract getContract() {
		return contract;
	}

	public final int getQuantity() {
		return curQuantity;
	}
	
	public final double getMarketPrice() {
		return marketPrice;
	}
	
	public final long getMultiplier() {
		return multiplier;
	}

	public final double getAvgPrice() {
		return avgEntryPrice;
	}

	public final double getRealizedProfitLoss() {
		return realizedPL;
	}

	public final double getUnrealizedProfitLoss() {
		return getPotentialProfitLoss(getMarketPrice(), getQuantity());
	}

	public final double getValue() {
		return getMarketPrice() * getQuantity() * getMultiplier();
	}

	public double trade(Execution exec) {
		marketPrice = exec.getPrice();
		if (exec.getQuantity() == 0)
			throw new IllegalArgumentException("Execution cannot have quantity of 0");

		if (curQuantity == 0 || (exec.getQuantity() > 0) == (curQuantity > 0)) {
			avgEntryPrice = ((getAvgPrice() * curQuantity) + (exec.getPrice() * exec.getQuantity())) / (curQuantity + exec.getQuantity());
			if (entryDate == null)
				entryDate = exec.getDate();
		}
		else {
			if (Math.abs(exec.getQuantity()) > Math.abs(curQuantity))
				throw new UnsupportedOperationException("Liquidating more shares than current position size not supported: " + contract + " " + exec + ", position @" + curQuantity);
			realizedPL += getPotentialProfitLoss(exec.getPrice(), -exec.getQuantity()) - exec.getCommission();
			avgExitPrice = ((getExitPrice() * Math.abs(totalQuantity - curQuantity)) + (exec.getPrice() * Math.abs(exec.getQuantity()))) / (Math.abs(totalQuantity - curQuantity) + Math.abs(exec.getQuantity()));
			exitDate = exec.getDate();
			if (entryDate != null && exitDate != null && exitDate.before(entryDate))
				throw new IllegalStateException("Date of position exit trade cannot be before entry date.");
		}

		curQuantity += exec.getQuantity();
		if (Math.abs(curQuantity) > Math.abs(totalQuantity))
			totalQuantity = curQuantity;
		commissions += exec.getCommission();
		return exec.getPrice() * -exec.getQuantity() * multiplier - exec.getCommission();
	}

	protected double getPotentialProfitLoss(double price, int quantity) {
		return (price - getAvgPrice()) * quantity * getMultiplier();
	}

	public void setMarketPrice(MarketData marketData) {
		setMarketPrice(marketData.getLast());
	}
	
	protected final void setMarketPrice(double marketPrice) {
		this.marketPrice = marketPrice;
		if (getQuantity() != 0)
			extPrice = extPrice == 0 ? marketPrice : (getQuantity() > 0 ? Math.max(extPrice, marketPrice) : Math.min(extPrice, marketPrice));
	}
	
	@Override
	public String toString() {
		NumberFormat pf = NumberFormat.getNumberInstance();
		if (getQuantity() != 0)
			return getContract() + " " + getQuantity() + " shrs @" + avgEntryPrice + " avg, mkt @" + getMarketPrice() + ", mkt val = " + getValue();
		else
			return (getTransactionQuantity() > 0 ? "long " : "short ") + getTransactionQuantity()
				+ " shrs, entry @" + pf.format(getEntryPrice()) + ", exit @" + pf.format(getExitPrice())
				+ ", reached " + pf.format(getExtentPrice())
				+ ", " + new DecimalFormat("+0.00%;-0.00%").format(getPercentGain());
	}

	public double getEntryPrice() {
		return getAvgPrice();
	}

	public double getExitPrice() {
		return avgExitPrice;
	}

	public double getExtentPrice() {
		return extPrice;
	}

	public int getTransactionQuantity() {
		return totalQuantity;
	}

	public double getPercentGain() {
		return (getExitPrice() - getEntryPrice()) / getEntryPrice() * getTransactionQuantity() / Math.abs(getTransactionQuantity());
	}

	public double getMaxUnrealizedPercent() {
		return (getExtentPrice() - getEntryPrice()) / getEntryPrice() * getTransactionQuantity() / Math.abs(getTransactionQuantity());
	}

	public Date getEntryDate() {
		return entryDate;
	}

	public Date getExitDate() {
		return exitDate;
	}

	public double getCommisions() {
		return commissions;
	}

}
