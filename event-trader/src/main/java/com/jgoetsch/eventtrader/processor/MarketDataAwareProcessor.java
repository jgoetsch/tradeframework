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
package com.jgoetsch.eventtrader.processor;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.tradeframework.InvalidContractException;
import com.jgoetsch.tradeframework.PropertyNotSetException;
import com.jgoetsch.tradeframework.data.DataUnavailableException;
import com.jgoetsch.tradeframework.marketdata.MarketData;
import com.jgoetsch.tradeframework.marketdata.MarketDataSource;

/**
 * Abstract class for processors requiring a market data snapshot for the
 * contract in question. The first time a given contract is processed by a
 * processor extending <code>MarketDataAwareProcessor</code>, a market data
 * snapshot request is made and the <code>MarketData</code> object is cached in
 * the <code>context</code> object. Any further
 * <code>MarketDataAwareProcessor</code> processing the same message will use
 * the cached <code>MarketData</code> so that only one market data request is
 * made per contract per message.
 * 
 * @author jgoetsch
 */
public abstract class MarketDataAwareProcessor implements Processor<TradeSignal> {

	private Logger log = LoggerFactory.getLogger(getClass());
	private MarketDataSource marketDataSource;

	public void initialize() {
		if (marketDataSource == null)
			throw new PropertyNotSetException("marketDataSource");
	}

	public final void process(TradeSignal trade, Map<Object, Object> context) throws Exception {
		try {
			MarketData contractData = ContextCacheUtil.getMarketData(marketDataSource, trade.getContract(), context);
			if (contractData != null)
				process(trade, contractData, context);
			else
				log.warn("Market data not available for contract " + trade.getContract());
		} catch (InvalidContractException e) {
			log.warn(e.getMessage());
		} catch (DataUnavailableException e) {
			log.warn(e.getMessage());
		}
	}

	protected abstract void process(TradeSignal trade, MarketData contractData, Map<Object, Object> context) throws Exception;

	public MarketDataSource getMarketDataSource() {
		return marketDataSource;
	}

	public void setMarketDataSource(MarketDataSource marketDataSource) {
		this.marketDataSource = marketDataSource;
	}
	
}
