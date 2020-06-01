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
package com.jgoetsch.ib;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.EClientSocket;
import com.ib.client.EJavaSignal;
import com.ib.client.EReader;
import com.ib.client.EReaderSignal;
import com.ib.client.EWrapper;
import com.jgoetsch.ib.handlers.AccountDataHandler;
import com.jgoetsch.ib.handlers.AccountDataListenerHandler;
import com.jgoetsch.ib.handlers.ContractDetailsHandler;
import com.jgoetsch.ib.handlers.HandlerManager;
import com.jgoetsch.ib.handlers.HistoricalDataHandler;
import com.jgoetsch.ib.handlers.MarketDataHandler;
import com.jgoetsch.ib.handlers.MarketDataListenerHandler;
import com.jgoetsch.ib.handlers.MessageLogger;
import com.jgoetsch.ib.handlers.NextValidIdHandler;
import com.jgoetsch.ib.handlers.SimpleHandlerDelegatingWrapper;
import com.jgoetsch.tradeframework.Contract;
import com.jgoetsch.tradeframework.ContractDetails;
import com.jgoetsch.tradeframework.InvalidContractException;
import com.jgoetsch.tradeframework.NotConnectedException;
import com.jgoetsch.tradeframework.OHLC;
import com.jgoetsch.tradeframework.Order;
import com.jgoetsch.tradeframework.account.AccountData;
import com.jgoetsch.tradeframework.account.AccountDataListener;
import com.jgoetsch.tradeframework.account.AccountDataSource;
import com.jgoetsch.tradeframework.account.MultiAccountDataSource;
import com.jgoetsch.tradeframework.data.ContractDetailsSource;
import com.jgoetsch.tradeframework.data.DataUnavailableException;
import com.jgoetsch.tradeframework.data.HistoricalDataSource;
import com.jgoetsch.tradeframework.marketdata.MarketData;
import com.jgoetsch.tradeframework.marketdata.MarketDataListener;
import com.jgoetsch.tradeframework.marketdata.MarketDataSource;
import com.jgoetsch.tradeframework.order.ExecutionListener;
import com.jgoetsch.tradeframework.order.OrderException;
import com.jgoetsch.tradeframework.order.TradingService;

public class TWSService implements TradingService, AccountDataSource, MultiAccountDataSource, MarketDataSource, HistoricalDataSource, ContractDetailsSource {

	private Logger log = LoggerFactory.getLogger(TWSService.class);
	protected final HandlerManager handlerManager;
	protected final EClientSocket eClientSocket;
	protected final Map<Contract, MarketDataListenerHandler> marketDataSubscriptions = new HashMap<Contract, MarketDataListenerHandler>();
	protected final Map<String, AccountDataListenerHandler> accountDataSubscriptions = new HashMap<String, AccountDataListenerHandler>();
	private int curRequestId = -1;

	private String host = "localhost";
	private int port = 7496;
	private int clientId = 1;

	private final EReaderSignal readerSignal;

	public TWSService() {
		handlerManager = new SimpleHandlerDelegatingWrapper();
		handlerManager.addHandler(new MessageLogger());
		readerSignal = new EJavaSignal();
		eClientSocket = new EClientSocket((EWrapper)handlerManager, readerSignal);
	}

	/**
	 * Constructor for mock testing
	 */
	public TWSService(EClientSocket clientSocket, HandlerManager handlerManager) {
		this.eClientSocket = clientSocket;
		this.handlerManager = handlerManager;
		this.readerSignal = null;
	}

	/**
	 * Construct and connect to specified host/port/clientid
	 */
	public TWSService(String host, int port, int clientid) throws NotConnectedException {
		this();
		if (!connect(host, port, clientid))
			throw new NotConnectedException();
	}

	public boolean connect() {
		NextValidIdHandler h = new NextValidIdHandler();
		handlerManager.addHandler(h);
		eClientSocket.eConnect(host, port, clientId);

		// start reader and processor threads
		if (readerSignal != null) {
			final EReader reader = new EReader(eClientSocket, readerSignal);
			reader.setName("tws-reader");
			reader.start();
			new Thread(() -> {
			    while (eClientSocket.isConnected()) {
			    	readerSignal.waitForSignal();
			        try {
			            reader.processMsgs();
			        } catch (Exception e) {
			            log.error("Exception thrown from EReader processing messages", e);
			        }
			    }
			}, "tws-msg-proc").start();
		}

		boolean success;
		synchronized (h) {
			success = h.block();
			curRequestId = h.getId();
		}
		handlerManager.removeHandler(h);
		if (success)
			log.info("Connected to TWS at {}:{} clientid:{}, initial order id is {}", host, port, clientId, curRequestId);
		else
			log.warn("Failed to connect to TWS at {}:{} clientid:{}", host, port, clientId);
		return success;
	}

	public boolean connect(String host, int port, int clientId) {
		this.host = host;
		this.port = port;
		this.clientId = clientId;
		return connect();
	}

	public void close() {
		log.info("Disconnecting from TWS {}:{}", host, port);
		handlerManager.removeAllHandlers();
		if (eClientSocket.isConnected()) {
			eClientSocket.eDisconnect();
		}
	}
	
	public boolean isConnected() {
		return eClientSocket.isConnected();
	}

	protected synchronized int getNextId() {
		if (curRequestId < 0)
			throw new IllegalStateException("NextValidId is not set, connect() failed or was not called");
		return curRequestId++;
	}

	public void placeOrder(Contract contract, Order order) throws InvalidContractException, OrderException {
		com.ib.client.Contract twsContract = TWSUtils.toTWSContract(contract);
		com.ib.client.Order twsOrder = TWSUtils.toTWSOrder(order);
		eClientSocket.placeOrder(getNextId(), twsContract, twsOrder);
	}

	/*
	 * Account data
	 */
	public CompletableFuture<Double> getAccountValue(String valueType) {
		return getAccountValue(valueType, "");
	}

	public CompletableFuture<Double> getAccountValue(String valueType, String acctCode) {
		return getAccountDataSnapshot(acctCode).thenApply(data -> data != null ? data.getValue(valueType) : 0);
	}

	public CompletableFuture<AccountData> getAccountDataSnapshot() {
		return getAccountDataSnapshot("");
	}

	public CompletableFuture<AccountData> getAccountDataSnapshot(String accountCode) {
		AccountDataHandler v = new AccountDataHandler();
		long startTime = System.currentTimeMillis();
		handlerManager.addHandler(v);
		eClientSocket.reqAccountUpdates(true, accountCode);
		return v.getCompletableFuture()
				.whenComplete((m, e) -> {
					eClientSocket.reqAccountUpdates(false, accountCode);
					handlerManager.removeHandler(v);
				})
				.orTimeout(3000, TimeUnit.MILLISECONDS)
				.whenComplete((m, e) -> {
					if (e == null)
						log.debug("Received account data snapshot in {} ms", System.currentTimeMillis() - startTime);
					else
						log.warn("Error receiving account data snapshot in {} ms", System.currentTimeMillis() - startTime, e);
				});
	}

	public void subscribeAccountData(AccountDataListener listener) {
		subscribeAccountData(listener, "");
	}

	public void subscribeAccountData(AccountDataListener listener, String accountCode) {
		synchronized (accountDataSubscriptions) {
			AccountDataListenerHandler accountDataSubscription = accountDataSubscriptions.get(accountCode);
			if (accountDataSubscription == null) {
				accountDataSubscription = new AccountDataListenerHandler(accountCode);
				accountDataSubscriptions.put(accountCode, accountDataSubscription);
			}
			boolean subscribe = !accountDataSubscription.hasListeners();
			accountDataSubscription.addListener(listener);
			if (subscribe) {
				handlerManager.addHandler(accountDataSubscription);
				eClientSocket.reqAccountUpdates(true, "");
			}
		}
	}

	public void cancelAccountDataSubscription(AccountDataListener listener) {
		cancelAccountDataSubscription(listener, "");
	}

	public void cancelAccountDataSubscription(AccountDataListener listener, String accountCode) {
		synchronized (accountDataSubscriptions) {
			AccountDataListenerHandler accountDataSubscription = accountDataSubscriptions.get(accountCode);
			if (accountDataSubscription == null || !accountDataSubscription.removeListener(listener))
				throw new IllegalArgumentException("Attempted to cancel account " + accountCode + " data for listener that is not subscribed: " + listener);
			else {
				if (!accountDataSubscription.hasListeners()) {
					eClientSocket.reqAccountUpdates(false, accountCode);
					handlerManager.removeHandler(accountDataSubscription);
					accountDataSubscriptions.remove(accountCode);
				}
			}
		}
	}

	public CompletableFuture<MarketData> getMktDataSnapshot(Contract contract) {
		int tickerId = getNextId();
		MarketDataHandler mkd = new MarketDataHandler(tickerId);
		long startTime = System.currentTimeMillis();
		handlerManager.addHandler(mkd);
		eClientSocket.reqMktData(tickerId, TWSUtils.toTWSContract(contract), null, true, false, Collections.emptyList());
		return mkd.getCompletableFuture()
				.whenComplete((m, e) -> handlerManager.removeHandler(mkd))
				.orTimeout(2500, TimeUnit.MILLISECONDS)
				.whenComplete((m, e) -> {
					if (e == null)
						log.debug("Received contract details for {} in {} ms", contract, System.currentTimeMillis() - startTime);
					else
						log.warn("Error receiving contract details for {} in {} ms", contract, System.currentTimeMillis() - startTime, e);
				});
	}

	public MarketData getDataSnapshot(Contract contract) throws IOException {
		try {
			return getMktDataSnapshot(contract).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new IOException(e);
		}
	}

	public void subscribeMarketData(Contract contract, MarketDataListener marketDataListener) {
		int tickerId = getNextId();
		synchronized (marketDataSubscriptions) {
			MarketDataListenerHandler mkdlHandler = marketDataSubscriptions.get(contract);
			if (mkdlHandler == null) {
				mkdlHandler = new MarketDataListenerHandler(tickerId, contract);
				marketDataSubscriptions.put(contract, mkdlHandler);
				handlerManager.addHandler(mkdlHandler);
				eClientSocket.reqMktData(tickerId, TWSUtils.toTWSContract(contract), null, false, false, Collections.emptyList());
			}
			mkdlHandler.addListener(marketDataListener);
		}
	}

	public void cancelMarketData(Contract contract, MarketDataListener marketDataListener) {
		synchronized (marketDataSubscriptions) {
			MarketDataListenerHandler mkdlHandler = marketDataSubscriptions.get(contract);
			if (mkdlHandler == null || !mkdlHandler.removeListener(marketDataListener))
				throw new IllegalArgumentException("Attempted to cancel market data for listener that is not subscribed: " + contract + ", " + marketDataListener);
			else {
				if (!mkdlHandler.hasListeners()) {
					eClientSocket.cancelMktData(mkdlHandler.getId());
					handlerManager.removeHandler(mkdlHandler);
					marketDataSubscriptions.remove(contract);
				}
			}
		}
	}

	public CompletableFuture<ContractDetails> getContractDetails(Contract contract) {
		int tickerId = getNextId();
		ContractDetailsHandler mkd = new ContractDetailsHandler(tickerId);
		long startTime = System.currentTimeMillis();
		handlerManager.addHandler(mkd);
		eClientSocket.reqContractDetails(tickerId, TWSUtils.toTWSContract(contract));
		return mkd.getCompletableFuture().whenComplete((m, e) -> handlerManager.removeHandler(mkd))
				.orTimeout(2500, TimeUnit.MILLISECONDS)
				.whenComplete((m, e) -> {
					if (e == null)
						log.debug("Received contract details for {} in {} ms", contract, System.currentTimeMillis() - startTime);
					else
						log.warn("Error receiving contract details for {} in {} ms", contract, System.currentTimeMillis() - startTime, e);
				});
	}

	/*
	 * Historical Data
	 */
	private static final String histPeriodUnit[] = { "1 sec", "5 secs", "15 secs", "30 secs", "1 min", "2 mins", "3 mins", "5 mins", "15 mins", "30 mins", "1 hour", "1 day", "1 week", "1 month", "3 months", "1 year" };
	private static final String histDurationUnit[] = { "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "D", "W", "M", "M", "Y" };
	private static final int histDurationMultiplier[] = { 1, 5, 15, 30, 60, 120, 180, 300, 900, 1800, 3600, 1, 1, 1, 3, 1 };

	public OHLC[] getHistoricalData(Contract contract, Date endDate, int numPeriods, int periodUnit) throws InvalidContractException, DataUnavailableException
	{
		String duration = (numPeriods * histDurationMultiplier[periodUnit]) + " " + histDurationUnit[periodUnit];
		return getHistoricalData(contract, endDate, duration, periodUnit, true);
	}

	public OHLC[] getHistoricalData(Contract contract, Date endDate, String duration, int periodUnit, boolean onlyRTH) throws InvalidContractException, DataUnavailableException
	{
		if (!isConnected())
			throw new DataUnavailableException("TWS service is not connected");
		DateFormat df = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		df.setTimeZone(HistoricalDataSource.timeZone);
		log.debug("getHistoricalData: " + contract + " " + duration + ", " + histPeriodUnit[periodUnit] + " ending " + df.format(new Date(endDate.getTime() - 1)));

		boolean success, retry;
		HistoricalDataHandler hdh;
		do {
			int tickerId = getNextId();
			hdh = new HistoricalDataHandler(tickerId);
			handlerManager.addHandler(hdh);
			eClientSocket.reqHistoricalData(tickerId, TWSUtils.toTWSContract(contract), df.format(new Date(endDate.getTime() - 1)) + " EST", duration, histPeriodUnit[periodUnit], "TRADES", onlyRTH ? 1 : 0, 2, false, Collections.emptyList());
			synchronized (hdh) {
				success = hdh.block();
			}

			retry = !success && hdh.getErrorMsg() != null && hdh.getErrorMsg().indexOf("pacing violation") != -1;
			if (retry) {
				log.info("Pacing violation, waiting to retry data request...");
				try {
					Thread.sleep(15000);
				} catch (InterruptedException e) { }
			}
		} while (retry);

		if (!success && (hdh.getErrorCode() == 200 || hdh.getErrorCode() == 203 || hdh.getErrorCode() == 162))
			throw new InvalidContractException(contract, hdh.getErrorMsg());
		else if (!success)
			log.warn(hdh.getErrorCode() + ": " + hdh.getErrorMsg());

		return success ? hdh.getData() : null;
	}

	@Override
	public String toString() {
		return "Live TWSService " + (isConnected() ? "connected, " : "disconnected, ") + handlerManager.getHandlers().size() + " handlers";
	}

	public void cancelExecutionSubscription(ExecutionListener listener) {
		// TODO Auto-generated method stub
		
	}

	public void subscribeExecutions(ExecutionListener listener) {
		// TODO Auto-generated method stub
		
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getHost() {
		return host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getPort() {
		return port;
	}

	public void setClientId(int clientid) {
		this.clientId = clientid;
	}

	public int getClientId() {
		return clientId;
	}

}
