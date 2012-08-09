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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.Contract;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.TickType;
import com.jgoetsch.ib.TWSUtils;

/**
 * Logs responses received from TWS to various loggers at the DEBUG level.
 * This handler is automatically registered so you can get logging output just by
 * enabling DEBUG logging on com.jgoetsch.ib.handlers.MessageLogger or its
 * sublevels.
 * 
 * @author jgoetsch
 *
 */
public class MessageLogger extends BaseHandler {

	private Logger statusLog = LoggerFactory.getLogger(MessageLogger.class.getName() + ".status");
	private Logger errorLog = LoggerFactory.getLogger(MessageLogger.class.getName() + ".error");
	private Logger orderLog = LoggerFactory.getLogger(MessageLogger.class.getName() + ".order");
	private Logger execLog = LoggerFactory.getLogger(MessageLogger.class.getName() + ".execDetails");
	private Logger accountLog = LoggerFactory.getLogger(MessageLogger.class.getName() + ".account");
	private Logger marketDataLog = LoggerFactory.getLogger(MessageLogger.class.getName() + ".marketDataLog");

	@Override
	public int getStatus() {
		return STATUS_WORKING;
	}

	@Override
	public void nextValidId(int orderId) {
		statusLog.debug("Initial order id = " + orderId);
	}

	@Override
	public void tickPrice(int tickerId, int field, double price, int canAutoExecute) {
		marketDataLog.debug("tickPrice: id=" + tickerId + ", " + TickType.getField(field) + "=" + price);
	}

	@Override
	public void tickSize(int tickerId, int field, int size) {
		marketDataLog.debug("tickPrice: id=" + tickerId + ", " + TickType.getField(field) + "=" + size);
	}

	@Override
	public void tickSnapshotEnd(int reqId) {
		marketDataLog.debug("tickSnapshotEnd: id=" + reqId);
	}

	@Override
	public void tickString(int tickerId, int tickType, String value) {
		marketDataLog.debug("tickString: id=" + tickerId + ", " + TickType.getField(tickType) + "=" + value);
	}

	/*
	public void updateAccountValue(String key, String value, String currency, String accountName) {
		System.out.println(accountName + ": " + key + " = " + value + " " + currency);
	}
	*/
	public void execDetails(int orderId, Contract contract, Execution execution) {
		execLog.debug(TWSUtils.fromTWSExecution(execution) + " " + TWSUtils.fromTWSContract(contract));
	}

	public void error(Exception e) {
		errorLog.error("Exception occured", e);
	}

	public void error(String str) {
		errorLog.info(str);
	}

	public void error(int id, int errorCode, String errorMsg) {
		errorLog.debug("{id:" + id + ", code:" + errorCode + "} " + errorMsg);
	}

	public void connectionClosed() {
		statusLog.warn("Connection to TWS was lost!");
		
	}

	@Override
	public void updateAccountValue(String key, String value, String currency,
			String accountName) {
		if (accountLog.isDebugEnabled())
			accountLog.debug(key + " = " + value + " " + currency);
	}

	@Override
	public void updatePortfolio(Contract contract, int position,
			double marketPrice, double marketValue, double averageCost,
			double unrealizedPNL, double realizedPNL, String accountName)
	{
		if (accountLog.isDebugEnabled())
			accountLog.debug(position + " " + TWSUtils.fromTWSContract(contract) + " @ " + averageCost + " (" + unrealizedPNL + ")");
	}

	@Override
	public void accountDownloadEnd(String accountName) {
		accountLog.debug("accountDownloadEnd {}", accountName);
	}

	@Override
	public void openOrder(int orderId, Contract contract, Order order, OrderState orderState)
	{
		if (orderLog.isDebugEnabled())
			orderLog.debug("openOrder: id=" + orderId + ", contract=" + TWSUtils.fromTWSContract(contract) + ", order=" + TWSUtils.fromTWSOrder(order)
					+ ", orderState=" + orderState.m_status);
	}

	@Override
	public void orderStatus(int orderId, String status, int filled,
			int remaining, double avgFillPrice, int permId, int parentId,
			double lastFillPrice, int clientId, String whyHeld)
	{
		if (orderLog.isDebugEnabled())
			orderLog.debug("orderStatus: id=" + orderId + ", status=" + status + ", filled=" + filled + ", remaining=" + remaining
					+ ", avgFillPrice=" + avgFillPrice + ", permId=" + permId + ", parentId=" + parentId + ", lastFillPrice=" + lastFillPrice
					+ ", clientId=" + clientId + ", whyHeld=" + whyHeld);
	}

}
