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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import com.jgoetsch.tradeframework.Contract;

public class CalculatedAccountData implements AccountData {

	private Map<Contract, Position> positions;
	private BigDecimal cashBalance;
	private Instant timestamp;

	public CalculatedAccountData(Map<Contract, ? extends Position> positions, BigDecimal cashBalance, Instant timestamp) {
		this.positions = Collections.unmodifiableMap(positions);
		this.cashBalance = cashBalance;
		this.timestamp = timestamp;
	}
	
	public BigDecimal getCashBalance() {
		return cashBalance;
	}

	public BigDecimal getNetLiquidationValue() {
		return positions.values().stream().map(Position::getValue).reduce(cashBalance, BigDecimal::add);
	}

	public BigDecimal getValue(String valueType) {
		if("NetLiquidation".equalsIgnoreCase(valueType))
			return getNetLiquidationValue();
		else if ("CashBalance".equalsIgnoreCase(valueType))
			return getCashBalance();
		else
			return BigDecimal.ZERO;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public Map<Contract, Position> getPositions() {
		return positions;
	}

	public String getSummaryText() {
		NumberFormat df = new DecimalFormat("$#,###,##0;($#,###,##0)");
		NumberFormat price = new DecimalFormat("0.###");
		StringBuilder sb = new StringBuilder();

		sb.append("\n\nNet Liquidation Value\t\t").append(df.format(getNetLiquidationValue()));
		sb.append("\nLast Updated Time\t\t").append(timestamp);
		sb.append("\nOpen Positions:");
		for (Map.Entry<Contract, ? extends Position> posEntry : positions.entrySet()) {
			String contractTitle = posEntry.getKey().toString();
			sb.append("\n\t").append(posEntry.getValue().getQuantity()).append("\t").append(contractTitle);
			for (int i=0; i < 2 - contractTitle.length() / 8; i++)
				sb.append('\t');
			sb.append(price.format(posEntry.getValue().getAvgPrice()));
			sb.append("\t").append(price.format(posEntry.getValue().getMarketPrice()));
			sb.append("\t").append(df.format(posEntry.getValue().getUnrealizedProfitLoss()));
			sb.append("\t").append(df.format(posEntry.getValue().getRealizedProfitLoss()));
		}

		return sb.toString();
	}
}
