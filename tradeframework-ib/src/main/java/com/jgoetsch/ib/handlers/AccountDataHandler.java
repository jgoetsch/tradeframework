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
package com.jgoetsch.ib.handlers;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.jgoetsch.ib.TWSUtils;
import com.jgoetsch.tradeframework.Contract;
import com.jgoetsch.tradeframework.account.AccountData;
import com.jgoetsch.tradeframework.account.Position;
import com.jgoetsch.tradeframework.account.PresetPosition;

/**
 * Handler that will wait for the specified account value to be returned and
 * store it.
 * 
 * @author jgoetsch
 *
 */
public class AccountDataHandler extends BaseHandler<AccountData> implements AccountData {

	private String accountCode;
	private Map<String, Object> accountValues = new TreeMap<String, Object>();
	private Map<Contract, Position> positions = new HashMap<Contract, Position>();
	private String updatedTimestampValue;
	private Instant timestamp;

	public AccountDataHandler(String accountCode) {
		this.accountCode = accountCode;
	}

	public AccountDataHandler(String accountCode, HandlerManager manager) {
		super(manager);
		this.accountCode = accountCode;
	}

	@Override
	public void updateAccountValue(String key, String value, String currency, String accountName) {
		if (accountCode == null || accountCode.equals(accountName)) {
			try {
				accountValues.put(key, new BigDecimal(value));
			} catch (NumberFormatException ex) {
				accountValues.put(key, value);
			}
		}
	}

	@Override
	public void updatePortfolio(com.ib.client.Contract contract, double position,
			double marketPrice, double marketValue, double averageCost,
			double unrealizedPNL, double realizedPNL, String accountName)
	{
		if (accountCode == null || accountCode.equals(accountName)) {
			positions.put(TWSUtils.fromTWSContract(contract),
					new PresetPosition(BigDecimal.valueOf(position), BigDecimal.valueOf(marketPrice),
							BigDecimal.valueOf(marketValue), BigDecimal.valueOf(averageCost),
							BigDecimal.valueOf(unrealizedPNL), BigDecimal.valueOf(realizedPNL)));
		}
	}

	@Override
	public void updateAccountTime(String timeStamp) {
		updatedTimestampValue = timeStamp;
		timestamp = Instant.now();
	}

	@Override
	public void accountDownloadEnd(String accountName) {
		if (accountCode == null || accountCode.equals(accountName)) {
			getCompletableFuture().complete(this);
		}
	}

	public BigDecimal getCashBalance() {
		return (BigDecimal)accountValues.get("CashBalance");
	}

	public BigDecimal getNetLiquidationValue() {
		return (BigDecimal)accountValues.get("NetLiquidation");
	}

	public Map<Contract, Position> getPositions() {
		return positions;
	}

	public BigDecimal getValue(String valueType) {
		return (BigDecimal)accountValues.get(valueType);
	}

	@Override
	public String toString() {
		return "[AccountValueHandler net liq value = " + getNetLiquidationValue() + "]";
	}

	public String getSummaryText() {
		NumberFormat df = new DecimalFormat("$#,###,##0;($#,###,##0)");
		NumberFormat price = new DecimalFormat("0.###");
		StringBuilder sb = new StringBuilder();

		//sb.append("\nProfit(Loss)\t\t").append(df.format(accountValues.get("PNL")));
		//sb.append("\t").append(pcnt.format(accountValues.get("PNL") / (getNetLiquidationValue() - accountValues.get("PNL"))));
		for (Map.Entry<String, Object> e: accountValues.entrySet()) {
			sb.append("\n").append(e.getKey());
			for (int i=0; i < 4 - e.getKey().length() / 8; i++)
				sb.append('\t');
			sb.append(e.getValue());
		}

		sb.append("\n\nNet Liquidation Value\t\t").append(df.format(getNetLiquidationValue()));
		sb.append("\nLast Updated Time\t\t").append(updatedTimestampValue).append("\t").append(timestamp);
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

	public Instant getTimestamp() {
		return timestamp;
	}
}
