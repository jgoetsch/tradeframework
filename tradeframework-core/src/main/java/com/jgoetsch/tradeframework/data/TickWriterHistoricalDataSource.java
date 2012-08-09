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
package com.jgoetsch.tradeframework.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jgoetsch.tradeframework.Contract;
import com.jgoetsch.tradeframework.OHLC;
import com.jgoetsch.tradeframework.stats.OHLCStatistic;
import com.jgoetsch.tradeframework.stats.Tick;

public class TickWriterHistoricalDataSource implements HistoricalDataSource {

	private DateFormat dateFormat;
	private List<Tick> data;

	public TickWriterHistoricalDataSource(InputStream inputStream, DateFormat dateFormat) throws IOException {
		this.dateFormat = dateFormat;
		this.data = new ArrayList<Tick>(10000);

		BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));
		String line = input.readLine();
		while (line != null) {
			Tick tick = parseTick(line);
			if (tick != null)
				data.add(tick);
			line = input.readLine();
		}
		input.close();
	}

	protected Tick parseTick(String line)
	{
		String part[] = line.split("[\\s,]");
		try {
			Tick tick = new Tick();
			Date ts = dateFormat.parse(part[0] + " " + part[1]);
			tick.setDate(ts);
			tick.setPrice(Double.parseDouble(part[2]));
			tick.setVolume(part.length >= 4 ? Integer.parseInt(part[3]) : 0);
			return tick;
		} catch (Exception e) {
			System.err.println("Failed to parse input line, skipping: " + line);
			return null;
		}
	}

	private static final long durations[] = { 1000, 5000, 15000, 30000, 60000, 120000, 180000, 300000, 900000, 1800000, 3600000, 86400000, 604800000, 2592000000L, 7776000000L, 31536000000L };

	public OHLC[] getHistoricalData(Contract contract, Date endDate, int numPeriods, int periodUnit) throws IOException, DataUnavailableException
	{
		long periodLength = durations[periodUnit];
		Date start = new Date(endDate.getTime() - (periodLength * numPeriods));
		int dataIdx = findStart(start);
		if (dataIdx == -1 || data.get(dataIdx).getDate().getTime() >= endDate.getTime())
			throw new DataUnavailableException("Source file does not contain data for start date " + start);

		OHLCStatistic[] ohlc = new OHLCStatistic[numPeriods];
		for (int i=0; i < numPeriods; i++) {
			ohlc[i] = new OHLCStatistic(new Date(start.getTime() + periodLength * i));
		}
		while (data.get(dataIdx).getDate().getTime() < endDate.getTime() && dataIdx < data.size()) {
			int period = (int)((data.get(dataIdx).getDate().getTime() - start.getTime()) / periodLength);
			ohlc[period].addSample(data.get(dataIdx).getPrice());
			dataIdx++;
		}

		// fill in empty periods with single value of adjacent period start/end
		for (int i=0; i < numPeriods; i++) {
			if (ohlc[i].getOpen() == -1) {
				if (i == 0) {
					for (int n=0; n < numPeriods; n++) {
						if (ohlc[n].getOpen() != -1) {
							ohlc[i].addSample(ohlc[n].getOpen());
							break;
						}
					}
				}
				else {
					ohlc[i].addSample(ohlc[i-1].getClose());
				}
			}
		}
		return ohlc;
	}

	private int findStart(Date date) {
		for (int i = 0; i < data.size(); i++) {
			if (data.get(i).getDate().after(date))
				return i;
		}
		return -1;
	}

	public void close() {
	}
}
