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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.tradeframework.Contract;
import com.jgoetsch.tradeframework.Execution;
import com.jgoetsch.tradeframework.marketdata.MarketData;
import com.jgoetsch.tradeframework.marketdata.MarketDataListener;
import com.jgoetsch.tradeframework.marketdata.MarketDataSource;
import com.jgoetsch.tradeframework.order.ExecutionListener;
import com.jgoetsch.tradeframework.order.TradingService;

public class SimulatedAccount implements AccountDataSource, AccountData, ExecutionListener, MarketDataListener {

	private final Set<AccountDataListener> listeners = new HashSet<AccountDataListener>();
	private final MarketDataSource marketDataSource;

	private final Map<Contract, MutablePosition> positions = new HashMap<Contract, MutablePosition>();
	private List<ClosedPosition> completedTransactions = new LinkedList<ClosedPosition>();
	private BigDecimal cashBalance;
	private Instant timestamp;

	private Logger transactionLog = LoggerFactory.getLogger(ClosedPosition.class);
	private Logger executionLog = LoggerFactory.getLogger(Execution.class);

	public SimulatedAccount(BigDecimal initialBalance, TradingService orderExecutionSource) {
		this.cashBalance = initialBalance;
		this.marketDataSource = null;
		if (orderExecutionSource != null)
			orderExecutionSource.subscribeExecutions(this);
	}

	public SimulatedAccount(BigDecimal initialBalance, TradingService orderExecutionSource, MarketDataSource marketDataSource) {
		this.cashBalance = initialBalance;
		this.marketDataSource = marketDataSource;
		if (orderExecutionSource != null)
			orderExecutionSource.subscribeExecutions(this);
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

	public Map<Contract, ? extends Position> getPositions() {
		return Collections.unmodifiableMap(positions);
	}

	public String getSummaryText() {
		NumberFormat df = new DecimalFormat("$#,###,##0;($#,###,##0)");
		NumberFormat price = new DecimalFormat("0.###");
		StringBuilder sb = new StringBuilder();

		sb.append("\n\nNet Liquidation Value\t\t").append(df.format(getNetLiquidationValue()));
		sb.append("\nLast Updated Time\t\t").append(timestamp);
		if (!positions.isEmpty()) {
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
		}
		return sb.toString();
	}

	public CompletableFuture<AccountData> getAccountDataSnapshot() {
		return CompletableFuture.completedFuture(this);
	}

	public void subscribeAccountData(AccountDataListener listener) {
		listeners.add(listener);
	}

	public void cancelAccountDataSubscription(AccountDataListener listener) {
		listeners.remove(listener);
	}

	public CompletableFuture<BigDecimal> getAccountValue(String valueType) {
		return CompletableFuture.completedFuture(getValue(valueType));
	}

	protected void update() {
		for (AccountDataListener listener : listeners) {
			listener.updateAccountData(this);
		}
	}

	protected MutablePosition createPosition(Contract contract) {
		return new DefaultCalculatedPosition(contract);
	}

	public Map<Contract, ? extends Position> getPositionMap() {
		return positions;
	}

	public List<ClosedPosition> getCompletedTransactions() {
		return Collections.unmodifiableList(completedTransactions);
	}

	public synchronized void handleExecution(Contract contract, Execution execution) {
		MutablePosition position = positions.get(contract);
		if (position != null && position.exists() && position.getQuantity().compareTo(execution.getQuantity().negate()) != 0
				&& position.getQuantity().add(execution.getQuantity()).signum() != position.getQuantity().signum())
		{
			Execution remainExec = new Execution(execution);
			remainExec.setQuantity(position.getQuantity().negate());
			handleSplitExecution(contract, remainExec);
			Execution newExec = new Execution(execution);
			newExec.setQuantity(execution.getQuantity().subtract(remainExec.getQuantity()));
			handleSplitExecution(contract, newExec);
		}
		else
			handleSplitExecution(contract, execution);
	}

	private static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").withZone(ZoneId.of("America/New_York"));

	private void handleSplitExecution(Contract contract, Execution execution) {
		MutablePosition position = positions.get(contract);
		if (position == null) {
			position = createPosition(contract);
			positions.put(contract, position);
		}
		cashBalance = cashBalance.add(position.trade(execution));
		update();
		if (executionLog.isInfoEnabled())
			executionLog.info(dateFormat.format(execution.getDate()) + ": " + contract + " " + execution + ", @" + position.getQuantity());

		if (position.getQuantity().signum() == 0) {
			onPositionClosed(contract, (ClosedPosition) position);
			positions.remove(contract);
		}

		if (marketDataSource != null) {
			try {
				if (position.getQuantity().signum() != 0)
					marketDataSource.subscribeMarketData(contract, this);
				else
					marketDataSource.cancelMarketData(contract, this);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	protected void onPositionClosed(Contract contract, ClosedPosition pos) {
		if (transactionLog.isInfoEnabled()) {
			transactionLog.info(String.format("%s\t%s\t%-5s\t%5d\t%8.5f\t%8.5f\t%5.2f\t%+6.2f%%\t%8.2f\t%7.2f\t%10.2f",
					dateFormat.format(pos.getEntryDate()), dateFormat.format(pos.getExitDate()),
					contract, pos.getTransactionQuantity(), pos.getEntryPrice(), pos.getExitPrice(), pos.getExtentPrice(),
					pos.getPercentGain().movePointRight(2), pos.getRealizedProfitLoss(), pos.getCommisions().negate(), getNetLiquidationValue()));
		}
		completedTransactions.add((ClosedPosition) pos);
	}

	public void tick(Contract contract, MarketData data) {
		MutablePosition position = positions.get(contract);
		if (position != null) {
			position.setMarketPrice(data);
			timestamp = data.getTimestamp();
			update();
		}
	}

	@Override
	public String toString() {
		return getClass().getName() + ": " + new DecimalFormat("$#,###,##0;($#,###,##0)").format(getNetLiquidationValue());
	}

}
