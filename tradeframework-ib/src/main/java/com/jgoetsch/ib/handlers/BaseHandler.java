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

/**
 * Base implementation of the EWrapper interface with empty handlers for every event
 * and common handling of error and closed connection conditions.
 * Extending classes need override only the events they want to handle.
 * 
 * @author jgoetsch
 *
 */
public class BaseHandler implements EWrapper {

	private boolean closedConnection;
	private int errorCode;
	private String errorMsg;
	
	public static final int STATUS_WORKING = 0;
	public static final int STATUS_SUCCESS = 1;
	public static final int STATUS_FAILED = 2;

	protected final static int MAX_WAIT_COUNT = 2; // 5 secs
	protected final static int WAIT_TIME = 2500;

	public int getErrorCode() {
		return errorCode;
	}
	
	public String getErrorMsg() {
		return errorMsg;
	}
	
	public boolean isClosedConnection() {
		return closedConnection;
	}

	/**
	 * Should be overridden to return success when the conditions that the handler must
	 * wait for to be considered complete are satisfied. The default implementation returns
	 * failed status on closed connection or error or working otherwise.
	 * 
	 * @return STATUS_WORKING, STATUS_SUCCESS, or STATUS_FAILED
	 */
	public int getStatus() {
		if (isClosedConnection())
			return STATUS_FAILED;
		else
			return STATUS_WORKING;
	}

	/**
	 * Puts the current thread into a wait state until the handler reports
	 * either success or failure or a timeout is reached.
	 * 
	 * @return true if handler reported success, false if failed or timed out
	 */
	public final boolean block() {
		int waitCount = 0;
		while (getStatus() == BaseHandler.STATUS_WORKING && (++waitCount) <= MAX_WAIT_COUNT) {
			try {
				this.wait(WAIT_TIME);
			} catch (InterruptedException e) {}
		}
		return (getStatus() == STATUS_SUCCESS);
	}


	/**
	 * Handle connection closed event. Mark closed and wake up threads waiting on this handler.
	 */
	public synchronized void connectionClosed() {
		this.closedConnection = true;
		this.notifyAll();
	}

	/**
	 * Handle error event. Record error and wake up threads waiting on this handler.
	 */
	public synchronized void error(int id, int errorCode, String errorMsg) {
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
		this.notifyAll();
	}

	public void error(Exception e) {
	}

	public void error(String str) {
	}
	
	/*
	 * empty EWrapper event method implementations to be overridden
	 */

	public void bondContractDetails(int reqId, ContractDetails contractDetails) {
	}

	public void contractDetails(int reqId, ContractDetails contractDetails) {
	}

	public void contractDetailsEnd(int reqId) {
	}

	public void currentTime(long time) {
	}

	public void execDetails(int orderId, Contract contract, Execution execution) {
	}

	public void fundamentalData(int reqId, String data) {
	}

	public void historicalData(int reqId, String date, double open,
			double high, double low, double close, int volume, int count,
			double WAP, boolean hasGaps) {
	}

	public void managedAccounts(String accountsList) {
	}

	public void nextValidId(int orderId) {
	}

	public void openOrder(int orderId, Contract contract, Order order,
			OrderState orderState) {
	}

	public void orderStatus(int orderId, String status, int filled,
			int remaining, double avgFillPrice, int permId, int parentId,
			double lastFillPrice, int clientId, String whyHeld) {
	}

	public void realtimeBar(int reqId, long time, double open, double high,
			double low, double close, long volume, double wap, int count) {
	}

	public void receiveFA(int faDataType, String xml) {
	}

	public void scannerData(int reqId, int rank,
			ContractDetails contractDetails, String distance, String benchmark,
			String projection, String legsStr) {
	}

	public void scannerDataEnd(int reqId) {
	}

	public void scannerParameters(String xml) {
	}

	public void tickEFP(int tickerId, int tickType, double basisPoints,
			String formattedBasisPoints, double impliedFuture, int holdDays,
			String futureExpiry, double dividendImpact, double dividendsToExpiry) {
	}

	public void tickGeneric(int tickerId, int tickType, double value) {
	}

	public void tickOptionComputation(int tickerId, int field,
			double impliedVol, double delta, double modelPrice,
			double pvDividend) {
	}

	public void tickPrice(int tickerId, int field, double price,
			int canAutoExecute) {
	}

	public void tickSize(int tickerId, int field, int size) {
	}

	public void tickString(int tickerId, int tickType, String value) {
	}

	public void updateAccountTime(String timeStamp) {
	}

	public void updateAccountValue(String key, String value, String currency,
			String accountName){
	}

	public void updateMktDepth(int tickerId, int position, int operation,
			int side, double price, int size) {
	}

	public void updateMktDepthL2(int tickerId, int position,
			String marketMaker, int operation, int side, double price, int size) {
	}

	public void updateNewsBulletin(int msgId, int msgType, String message,
			String origExchange) {
	}

	public void updatePortfolio(Contract contract, int position,
			double marketPrice, double marketValue, double averageCost,
			double unrealizedPNL, double realizedPNL, String accountName) {
	}

	public void accountDownloadEnd(String accountName) {
	}

	public void deltaNeutralValidation(int reqId, UnderComp underComp) {
	}

	public void execDetailsEnd(int reqId) {
	}

	public void openOrderEnd() {
	}

	public void tickSnapshotEnd(int reqId) {
	}

}
