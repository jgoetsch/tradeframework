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

import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.EWrapper;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.UnderComp;

public abstract class HandlerDelegatingWrapper implements EWrapper {

	protected interface HandlerCallback {
		void callHandler(EWrapper handler);
	}
	
	protected abstract void callHandlers(String eventName, int objId, HandlerCallback callback);

	/*
	 * EWrapper interface methods
	 * Delegate calls to the appropriate handler(s)
	 */
	
	public void bondContractDetails(final int reqId, final ContractDetails contractDetails) {
		callHandlers("bondContractDetails", reqId, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.bondContractDetails(reqId, contractDetails);
			}
		});
	}

	public void contractDetails(final int reqId, final ContractDetails contractDetails) {
		callHandlers("contractDetails", reqId, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.contractDetails(reqId, contractDetails);
			}
		});
	}

	public void contractDetailsEnd(final int reqId) {
		callHandlers("contractDetailsEnd", reqId, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.contractDetailsEnd(reqId);
			}
		});
	}

	public void currentTime(final long time) {
		callHandlers("currentTime", -1, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.currentTime(time);
			}
		});
	}

	public void execDetails(final int orderId, final Contract contract, final Execution execution) {
		callHandlers("execDetails", orderId, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.execDetails(orderId, contract, execution);
			}
		});
	}

	public void fundamentalData(final int reqId, final String data) {
		callHandlers("fundamentalData", reqId, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.fundamentalData(reqId, data);
			}
		});
	}

	public void historicalData(final int reqId, final String date, final double open,
			final double high, final double low, final double close, final int volume, final int count,
			final double WAP, final boolean hasGaps)
	{
		callHandlers("historicalData", reqId, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.historicalData(reqId, date, open, high, low, close, volume, count, WAP, hasGaps);
			}
		});
	}

	public void managedAccounts(final String accountsList) {
		callHandlers("managedAccounts", -1, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.managedAccounts(accountsList);
			}
		});
	}

	public void nextValidId(final int orderId) {
		callHandlers("nextValidId", -1, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.nextValidId(orderId);
			}
		});
	}

	public void openOrder(final int orderId, final Contract contract, final Order order, final OrderState orderState)
	{
		callHandlers("openOrder", orderId, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.openOrder(orderId, contract, order, orderState);
			}
		});
	}

	public void orderStatus(final int orderId, final String status, final int filled,
			final int remaining, final double avgFillPrice, final int permId, final int parentId,
			final double lastFillPrice, final int clientId, final String whyHeld)
	{
		callHandlers("orderStatus", orderId, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.orderStatus(orderId, status, filled, remaining, avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld);
			}
		});
	}

	public void realtimeBar(final int reqId, final long time, final double open, final double high,
			final double low, final double close, final long volume, final double wap, final int count)
	{
		callHandlers("realtimeBar", reqId, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.realtimeBar(reqId, time, open, high, low, close, volume, wap, count);
			}
		});
	}

	public void receiveFA(final int faDataType, final String xml) {
		callHandlers("receiveFA", -1, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.receiveFA(faDataType, xml);
			}
		});
	}

	public void scannerData(final int reqId, final int rank,
			final ContractDetails contractDetails, final String distance, final String benchmark,
			final String projection, final String legsStr)
	{
		callHandlers("scannerData", reqId, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.scannerData(reqId, rank, contractDetails, distance, benchmark, projection, legsStr);
			}
		});
	}

	public void scannerDataEnd(final int reqId) {
		callHandlers("scannerDataEnd", reqId, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.scannerDataEnd(reqId);
			}
		});
	}

	public void scannerParameters(final String xml) {
		callHandlers("scannerParameters", -1, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.scannerParameters(xml);
			}
		});
	}

	public void tickEFP(final int tickerId, final int tickType, final double basisPoints,
			final String formattedBasisPoints, final double impliedFuture, final int holdDays,
			final String futureExpiry, final double dividendImpact, final double dividendsToExpiry)
	{
		callHandlers("tickEFP", tickerId, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.tickEFP(tickerId, tickType, basisPoints, formattedBasisPoints, impliedFuture, holdDays, futureExpiry, dividendImpact, dividendsToExpiry);
			}
		});
	}

	public void tickGeneric(final int tickerId, final int tickType, final double value) {
		callHandlers("tickGeneric", tickerId, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.tickGeneric(tickerId, tickType, value);
			}
		});
	}

	public void tickOptionComputation(final int tickerId, final int field,
			final double impliedVol, final double delta, final double modelPrice,
			final double pvDividend)
	{
		callHandlers("tickOptionComputation", tickerId, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.tickOptionComputation(tickerId, field, impliedVol, delta, modelPrice, pvDividend);
			}
		});
	}

	public void tickPrice(final int tickerId, final int field, final double price, final int canAutoExecute)
	{
		callHandlers("tickPrice", tickerId, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.tickPrice(tickerId, field, price, canAutoExecute);
			}
		});
	}

	public void tickSize(final int tickerId, final int field, final int size) {
		callHandlers("tickSize", tickerId, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.tickSize(tickerId, field, size);
			}
		});
	}

	public void tickString(final int tickerId, final int tickType, final String value) {
		callHandlers("tickString", tickerId, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.tickString(tickerId, tickType, value);
			}
		});
	}

	public void updateAccountTime(final String timeStamp) {
		callHandlers("updateAccountTime", -1, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.updateAccountTime(timeStamp);
			}
		});
	}

	public void updateAccountValue(final String key, final String value, final String currency,
			final String accountName)
	{
		callHandlers("updateAccountValue", -1, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.updateAccountValue(key, value, currency, accountName);
			}
		});
	}

	public void updateMktDepth(final int tickerId, final int position, final int operation,
			final int side, final double price, final int size)
	{
		callHandlers("updateMktDepth", tickerId, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.updateMktDepth(tickerId, position, operation, side, price, size);
			}
		});
	}

	public void updateMktDepthL2(final int tickerId, final int position,
			final String marketMaker, final int operation, final int side, final double price, final int size)
	{
		callHandlers("updateMktDepthL2", tickerId, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.updateMktDepthL2(tickerId, position, marketMaker, operation, side, price, size);
			}
		});
	}

	public void updateNewsBulletin(final int msgId, final int msgType, final String message, final String origExchange)
	{
		callHandlers("updateNewsBulletin", -1, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.updateNewsBulletin(msgId, msgType, message, origExchange);
			}
		});
	}

	public void updatePortfolio(final Contract contract, final int position,
			final double marketPrice, final double marketValue, final double averageCost,
			final double unrealizedPNL, final double realizedPNL, final String accountName)
	{
		callHandlers("updatePortfolio", -1, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.updatePortfolio(contract, position, marketPrice, marketValue, averageCost, unrealizedPNL, realizedPNL, accountName);
			}
		});
	}

	/**
	 * Connection closed event gets sent to all registered handlers
	 */
	public void connectionClosed() {
		callHandlers("connectionClosed", -1, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.connectionClosed();
			}
		});
	}

	public void error(final int id, final int errorCode, final String errorMsg) {
		callHandlers("error", id, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.error(id, errorCode, errorMsg);
			}
		});
		/*for (EWrapper handler : handlerManager.getHandlers("error"))
			handler.error(id, errorCode, errorMsg);
		if (errorCode >= 501 && errorCode <= 503)
			for (EWrapper handler : handlerManager.getHandlers("nextValidId"))
				handler.error(id, errorCode, errorMsg);*/
	}

	public void error(final Exception e) {
		callHandlers("error", -1, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.error(e);
			}
		});
	}

	public void error(final String str) {
		callHandlers("error", -1, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.error(str);
			}
		});
	}

	public void accountDownloadEnd(final String accountName) {
		callHandlers("accountDownloadEnd", -1, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.accountDownloadEnd(accountName);
			}
		});
	}

	public void deltaNeutralValidation(final int reqId, final UnderComp underComp) {
		callHandlers("deltaNeutralValidation", reqId, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.deltaNeutralValidation(reqId, underComp);
			}
		});
	}

	public void execDetailsEnd(final int reqId) {
		callHandlers("execDetailsEnd", reqId, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.execDetailsEnd(reqId);
			}
		});
	}

	public void openOrderEnd() {
		callHandlers("openOrderEnd", -1, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.openOrderEnd();
			}
		});
	}

	public void tickSnapshotEnd(final int reqId) {
		callHandlers("tickSnapshotEnd", reqId, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.tickSnapshotEnd(reqId);
			}
		});
	}

}
