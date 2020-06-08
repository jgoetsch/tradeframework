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
package com.jgoetsch.tradeframework.tradingsystem;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.jgoetsch.tradeframework.Contract;
import com.jgoetsch.tradeframework.OHLC;
import com.jgoetsch.tradeframework.marketdata.MarketData;
import com.jgoetsch.tradeframework.stats.OHLCStatistic;

public abstract class IntervalTradingSystem extends AbstractTradingSystem {

	private long intervalLength;
	private long tradingDayStart;
	private long tradingDayEnd;
	private long lastTimestamp;
	private OHLCStatistic ohlc;
	private GregorianCalendar cal;

	public IntervalTradingSystem(long intervalLength) {
		this.intervalLength = intervalLength * 1000;
		this.ohlc = new OHLCStatistic();
		this.tradingDayStart = 9 * 3600 + 30 * 60;	// 9:30 am eastern
		this.tradingDayEnd = 16 * 3600;	// 4:00 pm eastern
		this.cal = new GregorianCalendar();
	}

	public IntervalTradingSystem(long intervalLength, long tradingDayStart, long tradingDayEnd) {
		this.intervalLength = intervalLength * 1000;
		this.ohlc = new OHLCStatistic();
		this.tradingDayStart = tradingDayStart;
		this.tradingDayEnd = tradingDayEnd;
		this.cal = new GregorianCalendar();
	}
	
	public synchronized void tick(Contract contract, MarketData data) {
		long ts = data.getTimestamp().toEpochMilli();
		if (lastTimestamp == 0)
			lastTimestamp = (ts / intervalLength) * intervalLength;
		while (ts - lastTimestamp > intervalLength) {
			if (isWithinTradingHours(lastTimestamp)) {
				if (ohlc.getClose() == -1)
					ohlc.addSample(data.getLast().doubleValue());
				intervalTick(data, ohlc, lastTimestamp, intervalLength);
			}
			ohlc.clear();
			lastTimestamp += intervalLength;
		}
		ohlc.addSample(data.getLast().doubleValue());
	}

	public void onStop() {
		super.onStop();
		if (isWithinTradingHours(lastTimestamp) && ohlc.getClose() != -1)
			intervalTick(null, ohlc, lastTimestamp, intervalLength);
	}

	protected boolean isWithinTradingHours(long timestamp) {
		cal.setTime(new Date(lastTimestamp));
		if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
			long sec = cal.get(Calendar.SECOND) + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.HOUR_OF_DAY) * 3600;
			if (sec >= tradingDayStart && sec < tradingDayEnd)
				return true;
		}
		return false;
	}

	protected abstract void intervalTick(MarketData data, OHLC ohlc, long timestamp, long interval);
}
