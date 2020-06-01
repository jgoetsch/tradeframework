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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionException;

import com.jgoetsch.tradeframework.Contract;
import com.jgoetsch.tradeframework.ContractDetails;
import com.jgoetsch.tradeframework.account.AccountData;
import com.jgoetsch.tradeframework.account.AccountDataSource;
import com.jgoetsch.tradeframework.data.ContractDetailsSource;
import com.jgoetsch.tradeframework.data.DataUnavailableException;
import com.jgoetsch.tradeframework.marketdata.MarketData;
import com.jgoetsch.tradeframework.marketdata.MarketDataSource;

public class ProcessorContext {

	private final Map<Contract, MarketData> marketDataCache = new HashMap<Contract, MarketData>();
	private final Map<Contract, ContractDetails> contractDetailsCache = new HashMap<Contract, ContractDetails>();
	private final Map<String, AccountData> accountDataCache = new HashMap<String, AccountData>();

	public MarketData getMarketData(MarketDataSource marketDataSource, Contract contract) throws DataUnavailableException {
		try {
			return marketDataCache.computeIfAbsent(contract, c -> marketDataSource.getMktDataSnapshot(contract).join());
		} catch (CompletionException ex) {
			throw new DataUnavailableException(ex.getCause());
		}
	}

	public ContractDetails getContractDetails(ContractDetailsSource contractDetailsSource, Contract contract) throws DataUnavailableException {
		try {
			return contractDetailsCache.computeIfAbsent(contract, c -> contractDetailsSource.getContractDetails(contract).join());
		} catch (CompletionException ex) {
			throw new DataUnavailableException(ex.getCause());
		}
	}

	public AccountData getAccountData(AccountDataSource accountDataSource) {
		try {
			return accountDataCache.computeIfAbsent("", c -> accountDataSource.getAccountDataSnapshot().join());
		} catch (CompletionException ex) {
			throw new DataUnavailableException(ex.getCause());
		}
	}

}
