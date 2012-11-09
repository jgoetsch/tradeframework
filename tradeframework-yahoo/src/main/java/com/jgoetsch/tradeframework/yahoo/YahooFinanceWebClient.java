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
package com.jgoetsch.tradeframework.yahoo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.tradeframework.Contract;
import com.jgoetsch.tradeframework.OHLC;
import com.jgoetsch.tradeframework.SimpleOHLC;
import com.jgoetsch.tradeframework.data.ContractDataSource;
import com.jgoetsch.tradeframework.data.DataUnavailableException;
import com.jgoetsch.tradeframework.data.FundamentalData;
import com.jgoetsch.tradeframework.data.HistoricalDataSource;

public class YahooFinanceWebClient implements ContractDataSource<FundamentalData>, HistoricalDataSource {

	private static Logger log = LoggerFactory.getLogger(YahooFinanceWebClient.class);
	private static final String fundamentalDataUrl = "http://finance.yahoo.com/d/quotes.csv";
	private static final String historicalDataUrl = "http://ichart.finance.yahoo.com/table.csv";
	private final DateFormat df = new SimpleDateFormat("y-M-d");

	public FundamentalData getDataSnapshot(Contract contract) throws IOException, DataUnavailableException {
		StringBuilder reqUrl = new StringBuilder(fundamentalDataUrl);
		reqUrl.append("?s=").append(contract.getSymbol());
		reqUrl.append("&f=va2j1r");

		BufferedReader content = new BufferedReader(new InputStreamReader(new URL(reqUrl.toString()).openStream()));
		String fields[] = content.readLine().split(",");
		content.close();

		FundamentalData data = new FundamentalData();

		try {
			data.setVolume(NumberFormat.getNumberInstance().parse(fields[0]).longValue());
		} catch (ParseException e) {
			log.warn("Volume parsing failed", e);
		}

		try {
			data.setAvgVolume(NumberFormat.getNumberInstance().parse(fields[1]).longValue());
		} catch (ParseException e) {
			log.warn("Avg volume parsing failed", e);
		}

		try {
			data.setMarketCap(parseMillions(fields[2]));
		} catch (ParseException e) {
			log.warn("Market cap parsing failed", e);
		}

		try {
			data.setPeRatio(NumberFormat.getNumberInstance().parse(fields[2]).doubleValue());
		} catch (ParseException e) {
			log.warn("P/E ratio parsing failed", e);
		}

		return data;
	}

	private double parseMillions(String num) throws ParseException {
		double n = NumberFormat.getNumberInstance().parse(num).doubleValue();
		if (num.endsWith("M"))
			return n;
		else if (num.endsWith("B"))
			return n * 1000;
		else
			return n / 1000000;
	}

	public OHLC[] getHistoricalData(Contract contract, Date endDate, int numPeriods, int periodUnit) throws IOException
	{
		if (!(periodUnit == HistoricalDataSource.PERIOD_1_DAY
				|| periodUnit == HistoricalDataSource.PERIOD_1_WEEK
				|| periodUnit == HistoricalDataSource.PERIOD_1_MONTH))
			throw new IllegalArgumentException("Must specify a period of day, week, or month.");

		GregorianCalendar start = new GregorianCalendar();
		start.setTime(endDate);
		if (periodUnit == HistoricalDataSource.PERIOD_1_DAY)
			start.add(Calendar.DATE, -numPeriods);
		else if (periodUnit == HistoricalDataSource.PERIOD_1_WEEK)
			start.add(Calendar.WEEK_OF_YEAR, -numPeriods);
		else if (periodUnit == HistoricalDataSource.PERIOD_1_MONTH)
			start.add(Calendar.WEEK_OF_YEAR, -numPeriods);

		GregorianCalendar end = new GregorianCalendar();
		end.setTime(endDate);

		StringBuilder reqUrl = new StringBuilder(historicalDataUrl);
		reqUrl.append("?s=").append(contract.getSymbol());
		reqUrl.append("&a=").append(start.get(Calendar.MONTH));
		reqUrl.append("&b=").append(start.get(Calendar.DAY_OF_MONTH));
		reqUrl.append("&c=").append(start.get(Calendar.YEAR));
		reqUrl.append("&d=").append(end.get(Calendar.MONTH));
		reqUrl.append("&e=").append(end.get(Calendar.DAY_OF_MONTH));
		reqUrl.append("&f=").append(end.get(Calendar.YEAR));
		reqUrl.append("&g=");
		if (periodUnit == HistoricalDataSource.PERIOD_1_DAY)
			reqUrl.append("d");
		else if (periodUnit == HistoricalDataSource.PERIOD_1_WEEK)
			reqUrl.append("w");
		else
			reqUrl.append("m");
		reqUrl.append("&ignore=.csv");

		ArrayList<OHLC> prices = new ArrayList<OHLC>();
		BufferedReader content = new BufferedReader(new InputStreamReader(new URL(reqUrl.toString()).openStream()));

		String line;
		for (line = content.readLine(); line != null; line = content.readLine()) {
			String fields[] = line.split(",");
			if (fields.length >= 7) {
				try {
					SimpleOHLC ohlc = new SimpleOHLC();
					ohlc.setDate(df.parse(fields[0]));
					ohlc.setOpen(Double.parseDouble(fields[1]));
					ohlc.setHigh(Double.parseDouble(fields[2]));
					ohlc.setLow(Double.parseDouble(fields[3]));
					ohlc.setClose(Double.parseDouble(fields[4]));
					ohlc.setVolume(Long.parseLong(fields[5]));
					prices.add(ohlc);
				}
				catch (ParseException e) {
				}
			}
		} while (line != null);

		content.close();
		return prices.toArray(new OHLC[0]);
	}

	public void close() throws IOException {
	}

}
