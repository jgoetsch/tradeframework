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
import java.util.Collection;

public final class TransactionStatistics {

	int nTransactions;
	int nLong, nShort;
	int nWins, nLosses;
	int totalShares;
	double winPercent;
	double lossPercent;
	double minPl;
	double maxPl;
	double maxGain;
	double maxDrawdown;
	BigDecimal totalPlAmount;
	BigDecimal commissions;

	public TransactionStatistics(Collection<ClosedPosition> transactions) {
		nTransactions = transactions.size();
		for (ClosedPosition tr : transactions) {
			if (tr.getTransactionQuantity().signum() > 0)
				nLong++;
			else if (tr.getTransactionQuantity().signum() < 0)
				nShort++;
			if (tr.getRealizedProfitLoss().signum() > 0) {
				nWins++;
				winPercent += tr.getPercentGain().doubleValue();
			}
			else if (tr.getRealizedProfitLoss().signum() < 0) {
				nLosses++;
				lossPercent += tr.getPercentGain().doubleValue();
			}
			maxPl = Math.max(maxPl, getTotalPlPercent());
			minPl = Math.min(minPl, getTotalPlPercent());
			maxGain = Math.max(maxGain, getTotalPlPercent() - minPl);
			maxDrawdown = Math.max(maxDrawdown, maxPl - getTotalPlPercent());
			totalShares += tr.getTransactionQuantity().abs().intValue();
			totalPlAmount = totalPlAmount.add(tr.getRealizedProfitLoss());
			commissions = commissions.add(tr.getCommisions());
		}
	}

	public double getWinningPercent() {
		return (double)nWins / (nWins + nLosses);
	}

	public double getTotalPlPercent() {
		return winPercent + lossPercent;
	}

	public double getAvgWinPercent() {
		return winPercent / nWins;
	}

	public double getAvgLossPercent() {
		return lossPercent / nLosses;
	}

	public double getMaxGain() {
		return maxGain;
	}

	public double getMaxDrawdown() {
		return maxDrawdown;
	}

	public int getLongCount() {
		return nLong;
	}

	public int getShortCount() {
		return nShort;
	}

	public int getAvgNumberShares() {
		return totalShares / nTransactions;
	}

	public BigDecimal getProfitBeforeCommissions() {
		return totalPlAmount;
	}

	public BigDecimal getCommssions() {
		return commissions;
	}
}
