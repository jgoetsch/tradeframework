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
package com.jgoetsch.tradeframework.marketdata;

import java.io.IOException;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.tradeframework.Contract;
import com.jgoetsch.tradeframework.InvalidContractException;
import com.jgoetsch.tradeframework.OHLC;
import com.jgoetsch.tradeframework.TFUtils;
import com.jgoetsch.tradeframework.data.HistoricalDataSource;
import com.jgoetsch.tradeframework.data.DataUnavailableException;
import com.jgoetsch.tradeframework.data.HistoricalDataUtils;

public class HistoricalMarketDataFeed extends SimulatedMarketDataFeed {

	private final Logger log = LoggerFactory.getLogger(HistoricalMarketDataFeed.class);

	private final HistoricalDataSource historicalDataSource;
	private final Contract contract;
	private final int dataPeriod;
	private final int bufferLength;

	private long curTimestamp;
	private long bufferEndTime;
	private OHLC[] data;
	private int dataIndex;

	public HistoricalMarketDataFeed(Contract contract, HistoricalDataSource historicalDataSource, int period, int bufferLength) {
		this.contract = contract;
		this.historicalDataSource = historicalDataSource;
		this.dataPeriod = period;
		this.bufferLength = bufferLength;
	}

	@Override
	protected final MarketData retrieveNextTick() throws IOException, InvalidContractException {
		if (curTimestamp == 0)
			throw new IllegalStateException("Time not initialized");
		boolean newTime = (data == null);

		while (data == null || dataIndex >= data.length) {
			try {
				bufferEndTime = adjustTimestampToRTH(bufferEndTime, contract);
				bufferEndTime += HistoricalDataUtils.getPeriodDurationInMillis(dataPeriod) * bufferLength;
				if (log.isDebugEnabled())
					log.debug("Requesting historical data for " + contract + " from " + TFUtils.getDateFormat().print(bufferEndTime - HistoricalDataUtils.getPeriodDurationInMillis(dataPeriod) * bufferLength) + " to " + TFUtils.getDateFormat().print(bufferEndTime));
				data = historicalDataSource.getHistoricalData(contract,
						new Date(bufferEndTime),
						bufferLength, dataPeriod);
			} catch (DataUnavailableException e) {
				return null;
			}
			if (data == null)
				return null;
			if (log.isTraceEnabled())
				log.trace("Received historical data for " + contract + " from " + TFUtils.getDateFormat().print(data[0].getDate().getTime())
						+ " to " + TFUtils.getDateFormat().print(data[data.length-1].getDate().getTime())
						+ ", starting at " + TFUtils.getDateFormat().print(curTimestamp));

			dataIndex = 0;
			while (dataIndex < data.length && data[dataIndex].getDate().getTime() <= curTimestamp)
				dataIndex++;
			if (newTime && dataIndex > 0) dataIndex--;
		}

		MarketData mkd = marketDataFromBar(data[dataIndex++]);
		curTimestamp = mkd.getTimestamp();
		return mkd;
	}

	@Override
	public final void advanceTo(long timestamp) throws IOException, InvalidContractException {
		if (timestamp > curTimestamp) {
			data = null;
			curTimestamp = timestamp;
			bufferEndTime = curTimestamp;
			reset();
			setLastTick(retrieveNextTick());
		}
	}

	protected MarketData marketDataFromBar(OHLC ohlc) {
		SimpleMarketData mkd = new SimpleMarketData();
		mkd.setLast(ohlc.getOpen());
		mkd.setBid(mkd.getLast() - 0.00);
		mkd.setAsk(mkd.getLast() + 0.00);
		mkd.setBidSize(10000);
		mkd.setAskSize(10000);
		mkd.setTimestamp(ohlc.getDate().getTime());
		mkd.setLastTimestamp(ohlc.getDate().getTime());
		return mkd;
	}

	/**
	 * Overridable method to adjust a timestamp to the next regular trading session
	 * of the given contract if it falls outside of the contract's regular trading hours.
	 * 
	 * @param timestamp
	 * @param contract
	 * @return millisecond timestamp adjusted up to a regular trading session.
	 */
	protected long adjustTimestampToRTH(long timestamp, Contract contract) {
		if ("STK".equals(contract.getType()) && "USD".equals(contract.getCurrency())) {
			DateTime dt = new DateTime(timestamp, DateTimeZone.forID("America/New_York"));
			LocalTime time = new LocalTime(dt);
			if (time.compareTo(new LocalTime(9, 30)) < 0)
				dt = dt.withHourOfDay(9).withMinuteOfHour(30);
			else if (time.compareTo(new LocalTime(16, 0)) >= 0) {
				dt = dt.plusDays(1).withHourOfDay(9).withMinuteOfHour(30);
			}
			if (dt.getDayOfWeek() == DateTimeConstants.SATURDAY)
				dt = dt.plusDays(2);
			else if (dt.getDayOfWeek() == DateTimeConstants.SUNDAY)
				dt = dt.plusDays(1);
			return dt.getMillis();
		}
		else if ("FUT".equals(contract.getType()) && "USD".equals(contract.getCurrency())) {
			DateTime dt = new DateTime(timestamp, DateTimeZone.forID("America/Chicago"));
			if (dt.getDayOfWeek() == DateTimeConstants.SATURDAY)
				dt = dt.plusDays(2).withHourOfDay(9).withMinuteOfHour(0);
			else if (dt.getDayOfWeek() == DateTimeConstants.SUNDAY)
				dt = dt.plusDays(1).withHourOfDay(9).withMinuteOfHour(0);
			return dt.getMillis();
		}
		else
			return timestamp;
	}

	public final void close() throws IOException {
		data = null;
	}

}
