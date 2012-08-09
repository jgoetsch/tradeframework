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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class TickWriterMarketDataFeed extends SimulatedMarketDataFeed {

	private BufferedReader input;
	private double defaultIncrement;
	private DateFormat dateFormat;
	private double lastPrice = -1;
	private long lastTimestamp;

	public TickWriterMarketDataFeed(InputStream inputStream, double defaultIncrement, DateFormat dateFormat) {
		this.input = new BufferedReader(new InputStreamReader(inputStream));
		this.defaultIncrement = defaultIncrement;
		this.dateFormat = dateFormat;
	}

	@Override
	protected MarketData retrieveNextTick() throws IOException
	{
		for (;;) {
			String line = input.readLine();
			if (line == null)
				return null;

			String part[] = line.split("[\\t,]");
			try {
				int field = 0;
				Date ts;
				if (dateFormat != null) {
					try {
						ts = dateFormat.parse(part[field]);
					} catch (ParseException e) {
						ts = dateFormat.parse(part[field] + " " + part[++field]);
					}
				}
				else
					ts = new Date(Long.parseLong(part[field]));

				SimpleMarketData mkd = new SimpleMarketData();
				mkd.setTimestamp(ts.getTime());
				mkd.setLast(Double.parseDouble(part[++field]));
				mkd.setLastSize(part.length >= ++field ? Integer.parseInt(part[field]) : 0);
				if (mkd.getLast() != lastPrice) {
					lastTimestamp = mkd.getTimestamp();
					lastPrice = mkd.getLast();
				}
				mkd.setLastTimestamp(lastTimestamp);

				mkd.setBid(part.length > ++field ? Double.parseDouble(part[field]) : mkd.getLast() - defaultIncrement);
				mkd.setBidSize(part.length > ++field ? Integer.parseInt(part[field]) : 10);
				mkd.setAsk(part.length > ++field ? Double.parseDouble(part[field]) : mkd.getLast() + defaultIncrement);
				mkd.setAskSize(part.length > ++field ? Integer.parseInt(part[field]) : 10);

				return mkd;
			} catch (Exception e) {
				System.err.println("Failed to parse input line, skipping: " + line + " (" + e.getMessage() + ")");
			}
		}
	}

	public void close() throws IOException {
		input.close();
	}

}
