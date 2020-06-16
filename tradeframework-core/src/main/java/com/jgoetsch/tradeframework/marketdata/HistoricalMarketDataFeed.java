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
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.tradeframework.Contract;
import com.jgoetsch.tradeframework.InvalidContractException;
import com.jgoetsch.tradeframework.OHLC;
import com.jgoetsch.tradeframework.Contract.SecurityType;
import com.jgoetsch.tradeframework.data.DataUnavailableException;
import com.jgoetsch.tradeframework.data.HistoricalDataSource;
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

	private static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm:ss z").withZone(ZoneId.of("America/New_York"));

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
					log.debug("Requesting historical data for " + contract + " from " + dateFormat.format(Instant.ofEpochMilli(bufferEndTime - HistoricalDataUtils.getPeriodDurationInMillis(dataPeriod) * bufferLength)) + " to " + dateFormat.format(Instant.ofEpochMilli(bufferEndTime)));
				data = historicalDataSource.getHistoricalData(contract,
						new Date(bufferEndTime),
						bufferLength, dataPeriod);
			} catch (DataUnavailableException e) {
				return null;
			}
			if (data == null)
				return null;
			if (log.isTraceEnabled())
				log.trace("Received historical data for " + contract + " from " + dateFormat.format(data[0].getDate().toInstant())
						+ " to " + dateFormat.format(data[data.length-1].getDate().toInstant())
						+ ", starting at " + dateFormat.format(Instant.ofEpochMilli(curTimestamp)));

			dataIndex = 0;
			while (dataIndex < data.length && data[dataIndex].getDate().getTime() <= curTimestamp)
				dataIndex++;
			if (newTime && dataIndex > 0) dataIndex--;
		}

		MarketData mkd = marketDataFromBar(data[dataIndex++]);
		curTimestamp = mkd.getTimestamp().toEpochMilli();
		return mkd;
	}

	@Override
	public final void advanceTo(Instant timestamp) throws IOException, InvalidContractException {
		long millis = timestamp.toEpochMilli();
		if (millis > curTimestamp) {
			data = null;
			curTimestamp = millis;
			bufferEndTime = curTimestamp;
			reset();
			setLastTick(retrieveNextTick());
		}
	}

	protected MarketData marketDataFromBar(OHLC ohlc) {
		SimpleMarketData mkd = new SimpleMarketData();
		mkd.setLast(BigDecimal.valueOf(ohlc.getOpen()));
		mkd.setBid(mkd.getLast());
		mkd.setAsk(mkd.getLast());
		mkd.setBidSize(10000);
		mkd.setAskSize(10000);
		mkd.setTimestamp(ohlc.getDate().toInstant());
		mkd.setLastTimestamp(ohlc.getDate().toInstant());
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
		if (SecurityType.STOCK.equals(contract.getType()) && "USD".equals(contract.getCurrency())) {
			ZonedDateTime dt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("America/New_York"));
			LocalTime time = dt.toLocalTime();
			if (time.isBefore(LocalTime.of(9, 30)))
				dt = dt.withHour(9).withMinute(30);
			else if (time.isAfter(LocalTime.of(16, 0))) {
				dt = dt.plusDays(1).withHour(9).withMinute(30);
			}
			if (dt.getDayOfWeek() == DayOfWeek.SATURDAY)
				dt = dt.plusDays(2);
			else if (dt.getDayOfWeek() == DayOfWeek.SUNDAY)
				dt = dt.plusDays(1);
			return dt.toInstant().toEpochMilli();
		}
		else if (SecurityType.FUTURES.equals(contract.getType()) && "USD".equals(contract.getCurrency())) {
			ZonedDateTime dt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("America/Chicago"));
			if (dt.getDayOfWeek() == DayOfWeek.SATURDAY)
				dt = dt.plusDays(2).withHour(9).withMinute(0);
			else if (dt.getDayOfWeek() == DayOfWeek.SUNDAY)
				dt = dt.plusDays(1).withHour(9).withMinute(0);
			return dt.toInstant().toEpochMilli();
		}
		else
			return timestamp;
	}

	public final void close() throws IOException {
		data = null;
	}

}
