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
package com.jgoetsch.eventtrader.order.size;

import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.eventtrader.processor.ProcessorContext;
import com.jgoetsch.tradeframework.account.AccountData;
import com.jgoetsch.tradeframework.account.AccountDataSource;

public class AvailableMarginSize extends MultipliedOrderSize {

	private AccountDataSource accountDataSource;
	private double marginFactor = 1.0;

	@Override
	protected int getBaseValue(TradeSignal trade, double price, ProcessorContext context) {
		AccountData accountData = context.getAccountData(accountDataSource);
		double available = accountData.getValue("AvailableFunds");

		if (trade.getType().isBuy()) {
			return (int)(available / (marginFactor * price));
		}
		else if (price > 5.0) {
			return (int)(available / Math.max(marginFactor * price, 5.0));
		}
		else {
			return (int)(available / Math.max(price, 2.5));
		}
	}

	public void setAccountDataSource(AccountDataSource accountDataSource) {
		this.accountDataSource = accountDataSource;
	}

	public AccountDataSource getAccountDataSource() {
		return accountDataSource;
	}

	public void setMarginFactor(double marginFactor) {
		this.marginFactor = marginFactor;
	}

	public double getMarginFactor() {
		return marginFactor;
	}

}
