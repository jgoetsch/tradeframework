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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
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
public class AccountDataHandler extends BaseHandler implements AccountData {

	private String accountCode;
	private Map<String, Object> accountValues;
	private Map<Contract, Position> positions;
	private String updatedTimestampValue;
	private long timestamp;
	private boolean isDone = false;

	public AccountDataHandler() {
		this.accountValues = new TreeMap<String, Object>();
		this.positions = new HashMap<Contract, Position>();
	}

	public AccountDataHandler(String accountCode) {
		this();
		this.accountCode = accountCode;
	}

	@Override
	public synchronized void updateAccountValue(String key, String value, String currency, String accountName) {
		if (accountCode == null || accountCode.equals(accountName)) {
			try {
				accountValues.put(key, Double.valueOf(value));
			} catch (NumberFormatException ex) {
				accountValues.put(key, value);
			}
		}
	}

	@Override
	public synchronized void updatePortfolio(com.ib.client.Contract contract, int position,
			double marketPrice, double marketValue, double averageCost,
			double unrealizedPNL, double realizedPNL, String accountName)
	{
		if (accountCode == null || accountCode.equals(accountName)) {
			positions.put(TWSUtils.fromTWSContract(contract), new PresetPosition(position, marketPrice, marketValue, averageCost, unrealizedPNL, realizedPNL));
		}
	}

	@Override
	public synchronized void updateAccountTime(String timeStamp) {
		updatedTimestampValue = timeStamp;
		timestamp = System.currentTimeMillis();
	}

	@Override
	public synchronized void accountDownloadEnd(String accountName) {
		if (accountCode == null || accountCode.equals(accountName)) {
			isDone = true;
			this.notifyAll();
		}
	}

	@Override
	public int getStatus() {
		if (isDone)
			return STATUS_SUCCESS;
		else
			return super.getStatus();
	}

	public double getCashBalance() {
		Double value = (Double)accountValues.get("CashBalance");
		return value == null ? 0 : value.doubleValue();
	}

	public double getNetLiquidationValue() {
		Double value = (Double)accountValues.get("NetLiquidation");
		return value == null ? 0 : value.doubleValue();
	}

	public Map<Contract, Position> getPositions() {
		return positions;
	}

	public double getValue(String valueType) {
		if (accountValues.containsKey(valueType))
			return (Double)accountValues.get(valueType);
		else
			return 0;
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
		sb.append("\nLast Updated Time\t\t").append(updatedTimestampValue).append("\t").append(DateFormat.getDateTimeInstance().format(new Date(timestamp)));
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

	public long getTimestamp() {
		return timestamp;
	}
}
