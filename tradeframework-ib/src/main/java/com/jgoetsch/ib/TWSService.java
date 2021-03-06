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
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
import com.jgoetsch.tradeframework.BrokerCommunicationException;
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

	private static final Logger log = LoggerFactory.getLogger(TWSService.class);
	private static final TWSMapper mapper = TWSMapper.INSTANCE;

	protected final HandlerManager handlerManager;
	protected final EClientSocket eClientSocket;
	protected final Map<Contract, MarketDataListenerHandler> marketDataSubscriptions = new HashMap<Contract, MarketDataListenerHandler>();
	protected final Map<String, AccountDataListenerHandler> accountDataSubscriptions = new HashMap<String, AccountDataListenerHandler>();
	private AtomicInteger curRequestId = new AtomicInteger(-1);

	private String host = "localhost";
	private int port = 7496;
	private int clientId = 1;

	private final EReaderSignal readerSignal;

	public TWSService() {
		handlerManager = new SimpleHandlerDelegatingWrapper();
		handlerManager.addHandler(MessageLogger.createLoggingHandler());
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
	public TWSService(String host, int port, int clientid) throws BrokerCommunicationException {
		this();
		if (!connect(host, port, clientid))
			throw new BrokerCommunicationException("Not connected");
	}

	public boolean connect() {
		NextValidIdHandler h = new NextValidIdHandler(handlerManager);
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

		try {
			curRequestId.set(h.getCompletableFuture().get());
		} catch (InterruptedException | ExecutionException e) {
			log.warn("Failed to connect to TWS at {}:{} clientid:{}", host, port, clientId);
			return false;
		}

		log.info("Connected to TWS at {}:{} clientid:{}, initial order id is {}", host, port, clientId, curRequestId);
		return true;
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

	protected int getNextId() {
		if (curRequestId.get() < 0)
			throw new IllegalStateException("NextValidId is not set, connect() failed or was not called");
		return curRequestId.incrementAndGet();
	}

	@Override
	public CompletableFuture<Order> placeOrder(Order order) throws InvalidContractException, OrderException {
		com.ib.client.Contract twsContract = mapper.toTWSContract(order.getContract());
		com.ib.client.Order twsOrder = mapper.toTWSOrder(order);
		eClientSocket.placeOrder(getNextId(), twsContract, twsOrder);
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Order> previewOrder(Order order) throws InvalidContractException, OrderException, IOException {
		com.ib.client.Contract twsContract = mapper.toTWSContract(order.getContract());
		com.ib.client.Order twsOrder = mapper.toTWSOrder(order);
		twsOrder.transmit(false);
		eClientSocket.placeOrder(getNextId(), twsContract, twsOrder);
		return CompletableFuture.completedFuture(null);
	}

	/*
	 * Account data
	 */
	public CompletableFuture<BigDecimal> getAccountValue(String valueType) {
		return getAccountValue(valueType, "");
	}

	public CompletableFuture<BigDecimal> getAccountValue(String valueType, String acctCode) {
		return getAccountDataSnapshot(acctCode).thenApply(data -> data != null ? data.getValue(valueType) : null);
	}

	public CompletableFuture<AccountData> getAccountDataSnapshot() {
		return getAccountDataSnapshot("");
	}

	public CompletableFuture<AccountData> getAccountDataSnapshot(String accountCode) {
		AccountDataHandler v = new AccountDataHandler(accountCode, handlerManager);
		long startTime = System.currentTimeMillis();
		eClientSocket.reqAccountUpdates(true, accountCode);
		return v.getCompletableFuture()
				.whenComplete((m, e) -> {
					eClientSocket.reqAccountUpdates(false, accountCode);
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
		MarketDataHandler mkd = new MarketDataHandler(tickerId, handlerManager);
		long startTime = System.currentTimeMillis();
		eClientSocket.reqMktData(tickerId, mapper.toTWSContract(contract), null, true, false, Collections.emptyList());
		return mkd.getCompletableFuture()
				.orTimeout(2500, TimeUnit.MILLISECONDS)
				.whenComplete((m, e) -> {
					if (e == null)
						log.debug("Received market data for {} in {} ms", contract, System.currentTimeMillis() - startTime);
					else
						log.warn("Error receiving market data for {} in {} ms", contract, System.currentTimeMillis() - startTime, e);
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
				eClientSocket.reqMktData(tickerId, mapper.toTWSContract(contract), null, false, false, Collections.emptyList());
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
		ContractDetailsHandler mkd = new ContractDetailsHandler(tickerId, handlerManager);
		long startTime = System.currentTimeMillis();
		eClientSocket.reqContractDetails(tickerId, mapper.toTWSContract(contract));
		return mkd.getCompletableFuture()
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

		int retryCount = 5, delay = 2000;
		do {
			int tickerId = getNextId();
			HistoricalDataHandler hdh = new HistoricalDataHandler(tickerId, handlerManager);
			eClientSocket.reqHistoricalData(tickerId, mapper.toTWSContract(contract), df.format(new Date(endDate.getTime() - 1)) + " EST", duration, histPeriodUnit[periodUnit], "TRADES", onlyRTH ? 1 : 0, 2, false, Collections.emptyList());

			try {
				return hdh.getCompletableFuture().get();
			} catch (ExecutionException ex) {
				String err = ex.getCause().getMessage();
				if (err == null || err.indexOf("pacing violation") == -1) {
					if (ex.getCause() instanceof DataUnavailableException)
						throw (DataUnavailableException)ex.getCause();
					else
						return null;
				}
			} catch (InterruptedException e) {
				return null;
			}

			log.info("Pacing violation, waiting to retry data request...");
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				return null;
			}
			delay *= 2;
		} while (retryCount-- > 0);

		return null;
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
