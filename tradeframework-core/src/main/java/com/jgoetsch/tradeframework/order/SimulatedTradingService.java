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
package com.jgoetsch.tradeframework.order;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.jgoetsch.tradeframework.Contract;
import com.jgoetsch.tradeframework.Order;
import com.jgoetsch.tradeframework.Execution;
import com.jgoetsch.tradeframework.InvalidContractException;
import com.jgoetsch.tradeframework.marketdata.MarketData;
import com.jgoetsch.tradeframework.marketdata.MarketDataListener;
import com.jgoetsch.tradeframework.marketdata.MarketDataSource;
import com.jgoetsch.tradeframework.order.commissions.CommissionStructure;
import com.jgoetsch.tradeframework.order.processing.LimitOrderProcessor;
import com.jgoetsch.tradeframework.order.processing.MarketOrderProcessor;
import com.jgoetsch.tradeframework.order.processing.OrderProcessor;
import com.jgoetsch.tradeframework.order.processing.StopLimitOrderProcessor;
import com.jgoetsch.tradeframework.order.processing.StopOrderProcessor;
import com.jgoetsch.tradeframework.order.processing.TrailingLimitOrderProcessor;
import com.jgoetsch.tradeframework.order.processing.TrailingStopOrderProcessor;

public class SimulatedTradingService implements TradingService, MarketDataListener {

	private MarketDataSource marketDataSource;
	private final Set<ExecutionListener> executionListeners = new HashSet<ExecutionListener>();

	//private final List<Execution> executions = new LinkedList<Execution>();
	private final Map<Contract, Collection<OrderProcessor>> openOrders = new HashMap<Contract, Collection<OrderProcessor>>();

	private CommissionStructure commissions;

	public SimulatedTradingService() {
	}

	public SimulatedTradingService(MarketDataSource marketDataSource) {
		this.marketDataSource = marketDataSource;
		this.commissions = null;
	}

	public SimulatedTradingService(MarketDataSource marketDataSource, CommissionStructure commissions) {
		this.marketDataSource = marketDataSource;
		this.commissions = commissions;
	}

	public synchronized void tick(Contract contract, MarketData marketData) {
		Collection<OrderProcessor> contractOrders = openOrders.get(contract);
		if (contractOrders != null) {
			Iterator<OrderProcessor> openOrderIter = contractOrders.iterator();
			while (openOrderIter.hasNext()) {
				OrderProcessor openOrder = openOrderIter.next();
				Execution execution = openOrder.process(marketData);
				if (execution != null) {
					if (commissions != null)
						execution.setCommission(commissions.getCommissions(contract, execution));
					notifyExecution(contract, execution);
					if (openOrder.getQuantityRemaining() == 0) {
						openOrderIter.remove();
					}
				}

			}
			if (contractOrders.isEmpty()) {
				openOrders.remove(contract);
				try {
					marketDataSource.cancelMarketData(contract, this);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected OrderProcessor createOpenOrder(Contract contract, Order order) {

		if (Order.TYPE_MARKET.equalsIgnoreCase(order.getType()))
			return new MarketOrderProcessor(order.getQuantity());

		else if (Order.TYPE_LIMIT.equalsIgnoreCase(order.getType()))
			return new LimitOrderProcessor(order.getQuantity(), order.getLimitPrice());

		else if (Order.TYPE_STOP.equalsIgnoreCase(order.getType()))
			return new StopOrderProcessor(order.getQuantity(), order.getAuxPrice());

		else if (Order.TYPE_STOPLIMIT.equalsIgnoreCase(order.getType()))
			return new StopLimitOrderProcessor(order.getQuantity(), order.getAuxPrice(), order.getLimitPrice());

		else if (Order.TYPE_TRAIL.equalsIgnoreCase(order.getType()))
			return new TrailingStopOrderProcessor(order.getQuantity(), order.getTrailStopPrice(), order.getAuxPrice(), new StopOrderProcessor.EODTriggerMethod());

		else if (Order.TYPE_TRAILLIMIT.equalsIgnoreCase(order.getType()))
			return new TrailingLimitOrderProcessor(order.getQuantity(), order.getTrailStopPrice(), order.getAuxPrice(), order.getLimitPrice());

		else
			throw new UnsupportedOperationException("Unsupported order type: " + order.getType());
	}

	public void placeOrder(Contract contract, Order order) throws IOException, InvalidContractException, OrderException {
		OrderProcessor openOrder = createOpenOrder(contract, order);
		synchronized (this) {
			Collection<OrderProcessor> contractOrders = openOrders.get(contract);
			if (contractOrders == null) {
				contractOrders = new LinkedList<OrderProcessor>();
				openOrders.put(contract, contractOrders);
			}
			contractOrders.add(openOrder);
		}

		try {
			marketDataSource.subscribeMarketData(contract, this);
		}
		catch (InvalidContractException e) {
			cancelOrders(contract);
			throw e;
		}
	}

	public synchronized void cancelOrders(Contract contract) {
		openOrders.remove(contract);
	}

	public final synchronized void subscribeExecutions(ExecutionListener listener) {
		executionListeners.add(listener);
	}

	public final void cancelExecutionSubscription(ExecutionListener listener) {
		executionListeners.remove(listener);
	}

	/**
	 * Notify all execution listeners of an execution
	 * @param contract
	 * @param execution
	 */
	protected final void notifyExecution(Contract contract, Execution execution) {
		//executions.add(execution);
		for (ExecutionListener listener : executionListeners)
			listener.handleExecution(contract, execution);
	}

	public void close() {
	}

	@Override
	public String toString() {
		return "SimulatedTradingService:" + openOrders;
	}

	public MarketDataSource getMarketDataSource() {
		return marketDataSource;
	}

	public void setMarketDataSource(MarketDataSource marketDataSource) {
		this.marketDataSource = marketDataSource;
	}

	public CommissionStructure getCommissions() {
		return commissions;
	}

	public void setCommissions(CommissionStructure commissions) {
		this.commissions = commissions;
	}

}
