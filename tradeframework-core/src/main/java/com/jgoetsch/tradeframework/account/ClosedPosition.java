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

import java.util.Date;

import com.jgoetsch.tradeframework.Contract;

/**
 * Interface that provides information about a round trip transaction, that is
 * matching entry and exit trades of a single contract.
 * 
 * @author jgoetsch
 * 
 */
public interface ClosedPosition {

	public Contract getContract();

	/**
	 * Returns the aggregate (generally volume weighted average) entry price of
	 * this transaction.
	 * 
	 * @return Entry price of the trade.
	 */
	public double getEntryPrice();

	/**
	 * Returns the aggregate (generally volume weighted average) exit price of
	 * this transaction. If no exit trade has yet been executed on this
	 * position, this method returns <code>0</code>.
	 * 
	 * @return Exit price of the trade.
	 */
	public double getExitPrice();

	/**
	 * Returns the price that the contract reached at which the unrealized gain
	 * of the position was at its maximum, i.e. the maximum price for long
	 * positions or the minimum price for short positions.
	 * 
	 * @return
	 */
	public double getExtentPrice();

	/**
	 * Returns the date of the first entry trade in this transaction.
	 * 
	 * @return Entry date of this transaction
	 */
	public Date getEntryDate();

	/**
	 * Returns the date of the last exit trade in this transaction.
	 * 
	 * @return Exit date of this transaction
	 */
	public Date getExitDate();

	/**
	 * Returns the largest position size that was accumulated as part of this
	 * transaction.
	 * 
	 * @return The maximum absolute value of the position size of the contract
	 *         during this transaction.
	 */
	public int getTransactionQuantity();

	/**
	 * Returns the total realized profit or loss amount of this transaction. If
	 * no exit trade has yet been executed on this position, this method returns
	 * <code>0</code>.
	 * 
	 * @return Realized profit or loss amount of this transaction.
	 */
	public double getRealizedProfitLoss();

	public double getCommisions();

	public double getPercentGain();
}
