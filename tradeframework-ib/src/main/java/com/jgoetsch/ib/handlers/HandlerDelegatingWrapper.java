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

	public void historicalData(int reqId, Bar bar)
	{
		callHandlers("historicalData", reqId, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.historicalData(reqId, bar);
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

	public void orderStatus(final int orderId, final String status, final double filled,
			final double remaining, final double avgFillPrice, final int permId, final int parentId,
			final double lastFillPrice, final int clientId, final String whyHeld, double mktCapPrice)
	{
		callHandlers("orderStatus", orderId, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.orderStatus(orderId, status, filled, remaining, avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld, mktCapPrice);
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

	public void tickOptionComputation(final int tickerId, final int field, final double impliedVol,
			final double delta, final double optPrice, final double pvDividend,
			final double gamma, final double vega, final double theta, final double undPrice)
	{
		callHandlers("tickOptionComputation", tickerId, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.tickOptionComputation(tickerId, field, impliedVol, delta, optPrice, pvDividend, gamma, vega, theta, undPrice);
			}
		});
	}

	public void tickPrice(final int tickerId, final int field, final double price, final TickAttrib attrib)
	{
		callHandlers("tickPrice", tickerId, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.tickPrice(tickerId, field, price, attrib);
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
			final String marketMaker, final int operation, final int side, final double price, final int size,
			final boolean isSmartDepth)
	{
		callHandlers("updateMktDepthL2", tickerId, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.updateMktDepthL2(tickerId, position, marketMaker, operation, side, price, size, isSmartDepth);
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

	public void deltaNeutralValidation(final int reqId, final DeltaNeutralContract deltaNeutralContract) {
		callHandlers("deltaNeutralValidation", reqId, new HandlerCallback() {
			public void callHandler(EWrapper handler) {
				handler.deltaNeutralValidation(reqId, deltaNeutralContract);
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

	@Override
	public void updatePortfolio(Contract contract, double position, double marketPrice, double marketValue,
			double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {
		callHandlers("updatePortfolio", -1, h -> h.updatePortfolio(contract, position, marketPrice,
				marketValue, averageCost, unrealizedPNL, realizedPNL, accountName));
	}

	@Override
	public void marketDataType(int reqId, int marketDataType) {
		callHandlers("marketDataType", reqId, h -> h.marketDataType(reqId, marketDataType));
	}

	@Override
	public void commissionReport(CommissionReport commissionReport) {
		callHandlers("commissionReport", -1, h -> h.commissionReport(commissionReport));
	}

	@Override
	public void position(String account, Contract contract, double pos, double avgCost) {
		callHandlers("position", -1, h -> h.position(account, contract, pos, avgCost));
	}

	@Override
	public void positionEnd() {
		callHandlers("positionEnd", -1, h -> h.positionEnd());
	}

	@Override
	public void accountSummary(int reqId, String account, String tag, String value, String currency) {
		callHandlers("accountSummary", reqId, h -> h.accountSummary(reqId, account, tag, value, currency));
	}

	@Override
	public void accountSummaryEnd(int reqId) {
		callHandlers("accountSummaryEnd", reqId, h -> h.accountSummaryEnd(reqId));
	}

	@Override
	public void verifyMessageAPI(String apiData) {
		callHandlers("verifyMessageAPI", -1, h -> h.verifyMessageAPI(apiData));
	}

	@Override
	public void verifyCompleted(boolean isSuccessful, String errorText) {
		callHandlers("verifyCompleted", -1, h -> h.verifyCompleted(isSuccessful, errorText));
	}

	@Override
	public void verifyAndAuthMessageAPI(String apiData, String xyzChallenge) {
		callHandlers("verifyAndAuthMessageAPI", -1, h -> h.verifyAndAuthMessageAPI(apiData, xyzChallenge));
	}

	@Override
	public void verifyAndAuthCompleted(boolean isSuccessful, String errorText) {
		callHandlers("verifyAndAuthCompleted", -1, h -> h.verifyAndAuthCompleted(isSuccessful, errorText));
	}

	@Override
	public void displayGroupList(int reqId, String groups) {
		callHandlers("displayGroupList", reqId, h -> h.displayGroupList(reqId, groups));
	}

	@Override
	public void displayGroupUpdated(int reqId, String contractInfo) {
		callHandlers("displayGroupUpdated", reqId, h -> h.displayGroupUpdated(reqId, contractInfo));
	}

	@Override
	public void connectAck() {
		callHandlers("connectAck", -1, h -> h.connectAck());
	}

	@Override
	public void positionMulti(int reqId, String account, String modelCode, Contract contract, double pos,
			double avgCost) {
		callHandlers("positionMulti", reqId, h -> h.positionMulti(reqId, account, modelCode, contract, pos, avgCost));
	}

	@Override
	public void positionMultiEnd(int reqId) {
		callHandlers("positionMultiEnd", reqId, h -> h.positionMultiEnd(reqId));
	}

	@Override
	public void accountUpdateMulti(int reqId, String account, String modelCode, String key, String value,
			String currency) {
		callHandlers("accountUpdateMulti", reqId, h -> h.accountUpdateMulti(reqId, account, modelCode, key, value, currency));
	}

	@Override
	public void accountUpdateMultiEnd(int reqId) {
		callHandlers("accountUpdateMultiEnd", reqId, h -> h.accountUpdateMultiEnd(reqId));
	}

	@Override
	public void securityDefinitionOptionalParameter(int reqId, String exchange, int underlyingConId,
			String tradingClass, String multiplier, Set<String> expirations, Set<Double> strikes) {
		callHandlers("securityDefinitionOptionalParameter", reqId, h ->h.securityDefinitionOptionalParameter(reqId,
				exchange, underlyingConId, tradingClass, multiplier, expirations, strikes));
	}

	@Override
	public void securityDefinitionOptionalParameterEnd(int reqId) {
		callHandlers("securityDefinitionOptionalParameterEnd", reqId, h -> h.securityDefinitionOptionalParameterEnd(reqId));
	}

	@Override
	public void softDollarTiers(int reqId, SoftDollarTier[] tiers) {
		callHandlers("softDollarTiers", reqId, h -> h.softDollarTiers(reqId, tiers));
	}

	@Override
	public void familyCodes(FamilyCode[] familyCodes) {
		callHandlers("familyCodes", -1, h -> h.familyCodes(familyCodes));
	}

	@Override
	public void symbolSamples(int reqId, ContractDescription[] contractDescriptions) {
		callHandlers("symbolSamples", reqId, h -> h.symbolSamples(reqId, contractDescriptions));
	}

	@Override
	public void historicalDataEnd(int reqId, String startDateStr, String endDateStr) {
		callHandlers("historicalDataEnd", reqId, h -> h.historicalDataEnd(reqId, startDateStr, endDateStr));
	}

	@Override
	public void mktDepthExchanges(DepthMktDataDescription[] depthMktDataDescriptions) {
		callHandlers("mktDepthExchanges", -1, h -> h.mktDepthExchanges(depthMktDataDescriptions));
	}

	@Override
	public void tickNews(int tickerId, long timeStamp, String providerCode, String articleId, String headline,
			String extraData) {
		callHandlers("tickNews", tickerId, h -> h.tickNews(tickerId, timeStamp, providerCode, articleId, headline, extraData));
	}

	@Override
	public void smartComponents(int reqId, Map<Integer, Entry<String, Character>> theMap) {
		callHandlers("smartComponents", reqId, h -> h.smartComponents(reqId, theMap));
	}

	@Override
	public void tickReqParams(int tickerId, double minTick, String bboExchange, int snapshotPermissions) {
		callHandlers("tickReqParams", tickerId, h -> h.tickReqParams(tickerId, minTick, bboExchange, snapshotPermissions));
	}

	@Override
	public void newsProviders(NewsProvider[] newsProviders) {
		callHandlers("newsProviders", -1, h -> h.newsProviders(newsProviders));
	}

	@Override
	public void newsArticle(int requestId, int articleType, String articleText) {
		callHandlers("newsArticle", requestId, h -> h.newsArticle(requestId, articleType, articleText));
	}

	@Override
	public void historicalNews(int requestId, String time, String providerCode, String articleId, String headline) {
		callHandlers("historicalNews", requestId, h -> h.historicalNews(requestId, time, providerCode, articleId, headline));
	}

	@Override
	public void historicalNewsEnd(int requestId, boolean hasMore) {
		callHandlers("historicalNewsEnd", requestId, h -> h.historicalNewsEnd(requestId, hasMore));
	}

	@Override
	public void headTimestamp(int reqId, String headTimestamp) {
		callHandlers("headTimestamp", reqId, h -> h.headTimestamp(reqId, headTimestamp));
	}

	@Override
	public void histogramData(int reqId, List<HistogramEntry> items) {
		callHandlers("histogramData", reqId, h -> h.histogramData(reqId, items));
	}

	@Override
	public void historicalDataUpdate(int reqId, Bar bar) {
		callHandlers("historicalDataUpdate", reqId, h -> h.historicalDataUpdate(reqId, bar));
	}

	@Override
	public void rerouteMktDataReq(int reqId, int conId, String exchange) {
		callHandlers("rerouteMktDataReq", reqId, h -> h.rerouteMktDataReq(reqId, conId, exchange));
	}

	@Override
	public void rerouteMktDepthReq(int reqId, int conId, String exchange) {
		callHandlers("rerouteMktDepthReq", reqId, h -> h.rerouteMktDepthReq(reqId, conId, exchange));
	}

	@Override
	public void marketRule(int marketRuleId, PriceIncrement[] priceIncrements) {
		callHandlers("marketRule", marketRuleId, h -> h.marketRule(marketRuleId, priceIncrements));
	}

	@Override
	public void pnl(int reqId, double dailyPnL, double unrealizedPnL, double realizedPnL) {
		callHandlers("pnl", reqId, h -> h.pnl(reqId, dailyPnL, unrealizedPnL, realizedPnL));
	}

	@Override
	public void pnlSingle(int reqId, int pos, double dailyPnL, double unrealizedPnL, double realizedPnL, double value) {
		callHandlers("pnlSingle", reqId, h -> h.pnlSingle(reqId, pos, dailyPnL, unrealizedPnL, realizedPnL, value));
	}

	@Override
	public void historicalTicks(int reqId, List<HistoricalTick> ticks, boolean done) {
		callHandlers("historicalTicks", reqId, h -> h.historicalTicks(reqId, ticks, done));
	}

	@Override
	public void historicalTicksBidAsk(int reqId, List<HistoricalTickBidAsk> ticks, boolean done) {
		callHandlers("historicalTicksBidAsk", reqId, h -> h.historicalTicksBidAsk(reqId, ticks, done));
	}

	@Override
	public void historicalTicksLast(int reqId, List<HistoricalTickLast> ticks, boolean done) {
		callHandlers("historicalTicksLast", reqId, h -> h.historicalTicksLast(reqId, ticks, done));
	}

	@Override
	public void tickByTickAllLast(int reqId, int tickType, long time, double price, int size,
			TickAttribLast tickAttribLast, String exchange, String specialConditions) {
		callHandlers("tickByTickAllLast", reqId, h -> h.tickByTickAllLast(reqId, tickType, time, price,
				size, tickAttribLast, exchange, specialConditions));
	}

	@Override
	public void tickByTickBidAsk(int reqId, long time, double bidPrice, double askPrice, int bidSize, int askSize,
			TickAttribBidAsk tickAttribBidAsk) {
		callHandlers("tickByTickBidAsk", reqId, h -> h.tickByTickBidAsk(reqId, time, bidPrice, askPrice,
				bidSize, askSize, tickAttribBidAsk));
	}

	@Override
	public void tickByTickMidPoint(int reqId, long time, double midPoint) {
		callHandlers("tickByTickMidPoint", reqId, h -> h.tickByTickMidPoint(reqId, time, midPoint));
	}

	@Override
	public void orderBound(long orderId, int apiClientId, int apiOrderId) {
		callHandlers("orderBound", -1, h -> h.orderBound(orderId, apiClientId, apiOrderId));
	}

	@Override
	public void completedOrder(Contract contract, Order order, OrderState orderState) {
		callHandlers("completedOrder", -1, h -> h.completedOrder(contract, order, orderState));
	}

	@Override
	public void completedOrdersEnd() {
		callHandlers("completedOrdersEnd", -1, h -> h.completedOrdersEnd());
	}

}
