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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.tradeframework.Contract;

public class SymbolOncePerDayFilter extends FilterProcessor<TradeSignal> {

	private ZoneId timeZone = ZoneId.of("America/New_York");

	private Set<Contract> used = new HashSet<Contract>();
	private LocalDate curDate;

	@Override
	protected boolean handleProcessing(TradeSignal trade, Map<Object,Object> context) {
		LocalDate date = LocalDate.ofInstant(trade.getDate(), timeZone);
		if (!date.equals(curDate)) {
			used.clear();
			curDate = date;
		}
		if (used.contains(trade.getContract()))
			return false;
		else {
			used.add(trade.getContract());
			return true;
		}
	}

	public ZoneId getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(ZoneId timeZone) {
		this.timeZone = timeZone;
	}

}
