/*
Copyright (c) 2012, Jeremy Goetsch
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that
the following conditions are met:

    Redistributions of source code must retain the above copyright notice, this list of conditions and
    the following disclaimer.
    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
    the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.jgoetsch.eventtrader.processor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.jgoetsch.tradeframework.Contract;
import com.jgoetsch.tradeframework.ContractDetails;
import com.jgoetsch.tradeframework.InvalidContractException;
import com.jgoetsch.tradeframework.account.AccountData;
import com.jgoetsch.tradeframework.account.AccountDataSource;
import com.jgoetsch.tradeframework.data.ContractDetailsSource;
import com.jgoetsch.tradeframework.data.DataUnavailableException;
import com.jgoetsch.tradeframework.marketdata.MarketData;
import com.jgoetsch.tradeframework.marketdata.MarketDataSource;

public class ContextCacheUtil {

	private ContextCacheUtil() { }

	private static final String marketDataKey = "MarketData";
	private static final String contractDetailsKey = "ContractDetails";
	private static final String accountDataKey = "AccountData";

	public static MarketData getMarketData(MarketDataSource marketDataSource, Contract contract, Map<Object, Object> context) throws IOException, InvalidContractException, DataUnavailableException {
		Map<Contract, MarketData> cache = ((Map<Contract, MarketData>)context.get(marketDataKey));
		if (cache == null) {
			cache = new HashMap<Contract, MarketData>();
			context.put(marketDataKey, cache);
		}
		if (cache.containsKey(contract))
			return cache.get(contract);
		else {
			MarketData mktData = marketDataSource.getDataSnapshot(contract);
			cache.put(contract, mktData);
			return mktData;
		}
	}

	public static ContractDetails getContractDetails(ContractDetailsSource contractDetailsSource, Contract contract, Map<Object, Object> context) throws IOException, InvalidContractException, DataUnavailableException {
		Map<Contract, ContractDetails> cache = ((Map<Contract, ContractDetails>)context.get(contractDetailsKey));
		if (cache == null) {
			cache = new HashMap<Contract, ContractDetails>();
			context.put(contractDetailsKey, cache);
		}
		if (cache.containsKey(contract))
			return cache.get(contract);
		else {
			ContractDetails mktData = contractDetailsSource.getContractDetails(contract);
			cache.put(contract, mktData);
			return mktData;
		}
	}

	public static AccountData getAccountData(AccountDataSource accountDataSource, Map<Object, Object> context) {
		AccountData accountData = (AccountData)context.get(accountDataKey);
		if (accountData != null)
			return accountData;
		else {
			accountData = accountDataSource.getAccountDataSnapshot();
			context.put(accountDataKey, accountData);
			return accountData;
		}
	}

}
