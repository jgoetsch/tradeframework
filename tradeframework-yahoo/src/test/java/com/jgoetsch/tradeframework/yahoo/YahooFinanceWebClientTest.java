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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.jgoetsch.tradeframework.Contract;
import com.jgoetsch.tradeframework.OHLC;
import com.jgoetsch.tradeframework.data.FundamentalData;
import com.jgoetsch.tradeframework.data.HistoricalDataSource;

@Ignore
public class YahooFinanceWebClientTest {

	YahooFinanceWebClient client;

	@Before
	public void setUp() {
		client = new YahooFinanceWebClient();
	}

	@After
	public void tearDown() throws IOException {
		client.close();
	}

	protected void assertValidOHLC(OHLC ohlc) {
		assertTrue(ohlc.getOpen() >= ohlc.getLow());
		assertTrue(ohlc.getOpen() <= ohlc.getHigh());
		assertTrue(ohlc.getClose() >= ohlc.getLow());
		assertTrue(ohlc.getClose() <= ohlc.getHigh());
	}

	@Test
	public void testHistoricalData() throws Exception {
		OHLC data[] = client.getHistoricalData(Contract.stock("MSFT"), new Date(), 20, HistoricalDataSource.PERIOD_1_DAY);
		assertNotNull(data);
		assertTrue(data.length > 10);
		for (OHLC ohlc : data)
			assertValidOHLC(ohlc);
		
		data = client.getHistoricalData(Contract.stock("GOOG"), new Date(), 5, HistoricalDataSource.PERIOD_1_WEEK);
		assertNotNull(data);
		assertEquals(5, data.length, 2);
		for (OHLC ohlc : data)
			assertValidOHLC(ohlc);
	}

	@Test
	public void testFundamentalData() throws Exception {
		FundamentalData data = client.getDataSnapshot(Contract.stock("BAC"));
		assertNotNull(data);
		System.out.println(data);
	}
}
