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

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.ib.client.Contract;
import com.jgoetsch.tradeframework.account.AccountDataListener;

/**
 * Handler that will wait for the specified account value to be returned and
 * store it.
 * 
 * @author jgoetsch
 *
 */
public class AccountDataListenerHandler extends AccountDataHandler {

	private Set<AccountDataListener> listeners = new CopyOnWriteArraySet<AccountDataListener>();

	public AccountDataListenerHandler(String accountCode) {
		super(accountCode);
	}

	public final boolean addListener(AccountDataListener listener) {
		return listeners.add(listener);
	}
	
	public final boolean removeListener(AccountDataListener listener) {
		return listeners.remove(listener);
	}

	public final boolean hasListeners() {
		return !listeners.isEmpty();
	}

	@Override
	public void updateAccountValue(String key, String value, String currency, String accountName) {
		super.updateAccountValue(key, value, currency, accountName);
		listeners.forEach(listener -> listener.updateAccountData(this));
	}

	@Override
	public void updatePortfolio(Contract contract, double position,
			double marketPrice, double marketValue, double averageCost,
			double unrealizedPNL, double realizedPNL, String accountName)
	{
		super.updatePortfolio(contract, position, marketPrice, marketValue, averageCost, unrealizedPNL, realizedPNL, accountName);
		listeners.forEach(listener -> listener.updateAccountData(this));
	}

	@Override
	public void updateAccountTime(String timeStamp) {
		super.updateAccountTime(timeStamp);
		listeners.forEach(listener -> listener.updateAccountData(this));
	}

}
