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
package com.jgoetsch.eventtrader.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.eventtrader.processor.ProcessorContext;
import com.jgoetsch.tradeframework.account.AccountData;
import com.jgoetsch.tradeframework.account.AccountDataSource;
import com.jgoetsch.tradeframework.account.Position;

/**
 * Filters out entering trades (BUY or SHORT) of a security in which you already have a
 * position. Exit trades (COVER, SELL) are not filtered, since it is presumed
 * that there would be an existing position when placing an exit trade.
 * 
 * @author jgoetsch
 * 
 */
public class ExistingPositionFilter extends FilterProcessor<TradeSignal> {

	private Logger log = LoggerFactory.getLogger(ExistingPositionFilter.class);
	private AccountDataSource accountDataSource;

	@Override
	protected boolean handleProcessing(TradeSignal trade, ProcessorContext context) {
		if (trade.getType().isExit())
			return true;
		else {
			AccountData account = context.getAccountData(accountDataSource);
			if (account == null)
				return true;
			else {
				Position pos = account.getPositions().get(trade.getContract());
				if (pos != null && pos.getQuantity() != 0) {
					log.info("Already have position in " + trade.getContract());
					return false;
				}
				else
					return true;
			}
		}
	}

	public void setAccountDataSource(AccountDataSource accountDataSource) {
		this.accountDataSource = accountDataSource;
	}

	public AccountDataSource getAccountDataSource() {
		return accountDataSource;
	}

}
