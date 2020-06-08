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
import java.math.BigDecimal;
import java.time.Instant;

public class RecordedMarketDataFeed extends SimulatedMarketDataFeed {

	private BufferedReader input;
	private BigDecimal defaultIncrement;

	public RecordedMarketDataFeed(InputStream inputStream) {
		this.input = new BufferedReader(new InputStreamReader(inputStream));
		this.defaultIncrement = new BigDecimal("0.1");
	}

	@Override
	protected MarketData retrieveNextTick() throws IOException
	{
		for (;;) {
			String line = input.readLine();
			if (line == null)
				return null;

			String part[] = line.split("[\\s,]");
			try {
				SimpleMarketData mkd = new SimpleMarketData();
				mkd.setLastTimestamp(Instant.ofEpochMilli(Long.parseLong(part[0])));
				mkd.setLast(new BigDecimal(part[1]));
				mkd.setLastSize(Integer.parseInt(part[2]));
				mkd.setBid(part.length >= 4 ? new BigDecimal(part[3]) : mkd.getLast().subtract(defaultIncrement));
				mkd.setBidSize(part.length >= 5 ? Integer.parseInt(part[4]) : 10);
				mkd.setAsk(part.length >= 5 ? new BigDecimal(part[5]) : mkd.getLast().add(defaultIncrement));
				mkd.setAskSize(part.length >= 6 ? Integer.parseInt(part[6]) : 10);
				return mkd;
			} catch (Exception e) {
				System.err.println("Failed to parse input line, skipping: " + line);
			}
		}
	}

	public void close() throws IOException {
		input.close();
	}

}
