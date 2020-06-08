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
package com.jgoetsch.tradeframework.account;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

/**
 * Proxy class to implement a single AccountDataSource from a MultiAccountDataSource
 * given an account id.
 * 
 * @author jgoetsch
 *
 */

public class SingleAccountDataSourceProxy implements AccountDataSource {

	private String accountCode;
	private MultiAccountDataSource accountDataSource;

	public SingleAccountDataSourceProxy() {
	}

	public SingleAccountDataSourceProxy(String accountCode, MultiAccountDataSource accountDataSource) {
		this.accountCode = accountCode;
		this.accountDataSource = accountDataSource;
	}

	public void cancelAccountDataSubscription(AccountDataListener listener) {
		accountDataSource.cancelAccountDataSubscription(listener, accountCode);
	}

	public CompletableFuture<AccountData> getAccountDataSnapshot() {
		return accountDataSource.getAccountDataSnapshot(accountCode);
	}

	public CompletableFuture<BigDecimal> getAccountValue(String valueType) {
		return accountDataSource.getAccountValue(valueType, accountCode);
	}

	public void subscribeAccountData(AccountDataListener listener) {
		accountDataSource.subscribeAccountData(listener, accountCode);
	}

	public void setAccountCode(String accountCode) {
		this.accountCode = accountCode;
	}

	public String getAccountCode() {
		return accountCode;
	}

	public void setAccountDataSource(MultiAccountDataSource accountDataSource) {
		this.accountDataSource = accountDataSource;
	}

	public MultiAccountDataSource getAccountDataSource() {
		return accountDataSource;
	}

}
