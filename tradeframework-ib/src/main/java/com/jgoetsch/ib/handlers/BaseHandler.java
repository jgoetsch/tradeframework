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

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.ib.client.Bar;
import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.ContractDescription;
import com.ib.client.ContractDetails;
import com.ib.client.DeltaNeutralContract;
import com.ib.client.DepthMktDataDescription;
import com.ib.client.EWrapper;
import com.ib.client.Execution;
import com.ib.client.FamilyCode;
import com.ib.client.HistogramEntry;
import com.ib.client.HistoricalTick;
import com.ib.client.HistoricalTickBidAsk;
import com.ib.client.HistoricalTickLast;
import com.ib.client.NewsProvider;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.PriceIncrement;
import com.ib.client.SoftDollarTier;
import com.ib.client.TickAttrib;
import com.ib.client.TickAttribBidAsk;
import com.ib.client.TickAttribLast;

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

	public final static int WAIT_TIME = 2500;

	public synchronized int getErrorCode() {
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
		long timeout;
		Instant until = Instant.now().plusMillis(WAIT_TIME);
		while (getStatus() == BaseHandler.STATUS_WORKING && (timeout = Duration.between(Instant.now(), until).toMillis()) > 0) {
			try {
				this.wait(timeout);
			} catch (InterruptedException e) {
				return false;
			}
		}
		return (getStatus() == STATUS_SUCCESS);
	}


	/**
	 * Handle connection closed event. Mark closed and wake up threads waiting on this handler.
	 */
	@Override
	public synchronized void connectionClosed() {
		this.closedConnection = true;
		this.notifyAll();
	}

	/**
	 * Handle error event. Record error and wake up threads waiting on this handler.
	 */
	@Override
	public synchronized void error(int id, int errorCode, String errorMsg) {
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
		this.notifyAll();
	}

	@Override
	public void error(Exception e) {
	}

	@Override
	public void error(String str) {
	}
	
	/*
	 * empty EWrapper event method implementations to be overridden
	 */

	@Override
	public void bondContractDetails(int reqId, ContractDetails contractDetails) {
	}

	@Override
	public void contractDetails(int reqId, ContractDetails contractDetails) {
	}

	@Override
	public void contractDetailsEnd(int reqId) {
	}

	@Override
	public void currentTime(long time) {
	}

	@Override
	public void execDetails(int orderId, Contract contract, Execution execution) {
	}

	@Override
	public void fundamentalData(int reqId, String data) {
	}

	@Override
	public void managedAccounts(String accountsList) {
	}

	@Override
	public void nextValidId(int orderId) {
	}

	@Override
	public void openOrder(int orderId, Contract contract, Order order,
			OrderState orderState) {
	}

	@Override
	public void realtimeBar(int reqId, long time, double open, double high,
			double low, double close, long volume, double wap, int count) {
	}

	@Override
	public void receiveFA(int faDataType, String xml) {
	}

	@Override
	public void scannerData(int reqId, int rank,
			ContractDetails contractDetails, String distance, String benchmark,
			String projection, String legsStr) {
	}

	@Override
	public void scannerDataEnd(int reqId) {
	}

	@Override
	public void scannerParameters(String xml) {
	}

	@Override
	public void tickEFP(int tickerId, int tickType, double basisPoints,
			String formattedBasisPoints, double impliedFuture, int holdDays,
			String futureExpiry, double dividendImpact, double dividendsToExpiry) {
	}

	@Override
	public void tickGeneric(int tickerId, int tickType, double value) {
	}

	@Override
	public void tickSize(int tickerId, int field, int size) {
	}

	@Override
	public void tickString(int tickerId, int tickType, String value) {
	}

	@Override
	public void updateAccountTime(String timeStamp) {
	}

	@Override
	public void updateAccountValue(String key, String value, String currency,
			String accountName){
	}

	@Override
	public void updateMktDepth(int tickerId, int position, int operation,
			int side, double price, int size) {
	}

	@Override
	public void updateNewsBulletin(int msgId, int msgType, String message,
			String origExchange) {
	}

	@Override
	public void accountDownloadEnd(String accountName) {
	}

	@Override
	public void execDetailsEnd(int reqId) {
	}

	@Override
	public void openOrderEnd() {
	}

	@Override
	public void tickSnapshotEnd(int reqId) {
	}

	@Override
	public void tickPrice(int tickerId, int field, double price, TickAttrib attrib) {
	}

	@Override
	public void tickOptionComputation(int tickerId, int field, double impliedVol, double delta, double optPrice,
			double pvDividend, double gamma, double vega, double theta, double undPrice) {
	}

	@Override
	public void orderStatus(int orderId, String status, double filled, double remaining, double avgFillPrice,
			int permId, int parentId, double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {
	}

	@Override
	public void updatePortfolio(Contract contract, double position, double marketPrice, double marketValue,
			double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {
	}

	@Override
	public void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation, int side, double price,
			int size, boolean isSmartDepth) {
	}

	@Override
	public void historicalData(int reqId, Bar bar) {
	}

	@Override
	public void deltaNeutralValidation(int reqId, DeltaNeutralContract deltaNeutralContract) {
	}

	@Override
	public void marketDataType(int reqId, int marketDataType) {
	}

	@Override
	public void commissionReport(CommissionReport commissionReport) {		
	}

	@Override
	public void position(String account, Contract contract, double pos, double avgCost) {
	}

	@Override
	public void positionEnd() {
	}

	@Override
	public void accountSummary(int reqId, String account, String tag, String value, String currency) {		
	}

	@Override
	public void accountSummaryEnd(int reqId) {		
	}

	@Override
	public void verifyMessageAPI(String apiData) {
	}

	@Override
	public void verifyCompleted(boolean isSuccessful, String errorText) {
	}

	@Override
	public void verifyAndAuthMessageAPI(String apiData, String xyzChallenge) {
	}

	@Override
	public void verifyAndAuthCompleted(boolean isSuccessful, String errorText) {		
	}

	@Override
	public void displayGroupList(int reqId, String groups) {		
	}

	@Override
	public void displayGroupUpdated(int reqId, String contractInfo) {		
	}

	@Override
	public void connectAck() {		
	}

	@Override
	public void positionMulti(int reqId, String account, String modelCode, Contract contract, double pos,
			double avgCost) {		
	}

	@Override
	public void positionMultiEnd(int reqId) {		
	}

	@Override
	public void accountUpdateMulti(int reqId, String account, String modelCode, String key, String value,
			String currency) {		
	}

	@Override
	public void accountUpdateMultiEnd(int reqId) {		
	}

	@Override
	public void securityDefinitionOptionalParameter(int reqId, String exchange, int underlyingConId,
			String tradingClass, String multiplier, Set<String> expirations, Set<Double> strikes) {		
	}

	@Override
	public void securityDefinitionOptionalParameterEnd(int reqId) {		
	}

	@Override
	public void softDollarTiers(int reqId, SoftDollarTier[] tiers) {		
	}

	@Override
	public void familyCodes(FamilyCode[] familyCodes) {		
	}

	@Override
	public void symbolSamples(int reqId, ContractDescription[] contractDescriptions) {		
	}

	@Override
	public void historicalDataEnd(int reqId, String startDateStr, String endDateStr) {		
	}

	@Override
	public void mktDepthExchanges(DepthMktDataDescription[] depthMktDataDescriptions) {		
	}

	@Override
	public void tickNews(int tickerId, long timeStamp, String providerCode, String articleId, String headline,
			String extraData) {
	}

	@Override
	public void smartComponents(int reqId, Map<Integer, Entry<String, Character>> theMap) {		
	}

	@Override
	public void tickReqParams(int tickerId, double minTick, String bboExchange, int snapshotPermissions) {		
	}

	@Override
	public void newsProviders(NewsProvider[] newsProviders) {
	}

	@Override
	public void newsArticle(int requestId, int articleType, String articleText) {		
	}

	@Override
	public void historicalNews(int requestId, String time, String providerCode, String articleId, String headline) {		
	}

	@Override
	public void historicalNewsEnd(int requestId, boolean hasMore) {		
	}

	@Override
	public void headTimestamp(int reqId, String headTimestamp) {		
	}

	@Override
	public void histogramData(int reqId, List<HistogramEntry> items) {		
	}

	@Override
	public void historicalDataUpdate(int reqId, Bar bar) {
	}

	@Override
	public void rerouteMktDataReq(int reqId, int conId, String exchange) {
	}

	@Override
	public void rerouteMktDepthReq(int reqId, int conId, String exchange) {
	}

	@Override
	public void marketRule(int marketRuleId, PriceIncrement[] priceIncrements) {
	}

	@Override
	public void pnl(int reqId, double dailyPnL, double unrealizedPnL, double realizedPnL) {
	}

	@Override
	public void pnlSingle(int reqId, int pos, double dailyPnL, double unrealizedPnL, double realizedPnL, double value) {
	}

	@Override
	public void historicalTicks(int reqId, List<HistoricalTick> ticks, boolean done) {
	}

	@Override
	public void historicalTicksBidAsk(int reqId, List<HistoricalTickBidAsk> ticks, boolean done) {
	}

	@Override
	public void historicalTicksLast(int reqId, List<HistoricalTickLast> ticks, boolean done) {
	}

	@Override
	public void tickByTickAllLast(int reqId, int tickType, long time, double price, int size,
			TickAttribLast tickAttribLast, String exchange, String specialConditions) {
	}

	@Override
	public void tickByTickBidAsk(int reqId, long time, double bidPrice, double askPrice, int bidSize, int askSize,
			TickAttribBidAsk tickAttribBidAsk) {
	}

	@Override
	public void tickByTickMidPoint(int reqId, long time, double midPoint) {
	}

	@Override
	public void orderBound(long orderId, int apiClientId, int apiOrderId) {
	}

	@Override
	public void completedOrder(Contract contract, Order order, OrderState orderState) {
	}

	@Override
	public void completedOrdersEnd() {
	}

}
