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
package com.jgoetsch.eventtrader.test;

import java.util.Collection;

import junit.framework.TestCase;

import org.junit.Assert;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.eventtrader.TradeType;
import com.jgoetsch.eventtrader.extractor.AlertTradeExtractor;
import com.jgoetsch.eventtrader.extractor.TradeExtractor;
import com.jgoetsch.tradeframework.Contract;

public class AlertTradeExtractorTest extends TestCase {

	public void testTimAlertExtractor() throws Exception {
		TradeExtractor extractor = new AlertTradeExtractor();
		String src = "TimAlert test";

		assertTrade(extractor.parseTrades(new Msg(src, "shorted 1000 DEXO at $6.45")),
					new TradeSignal(TradeType.SHORT, Contract.stock("DEXO"), 1000, 6.45));
		assertTrade(extractor.parseTrades(new Msg(src, "Shorted 7k DEXO at 6.42")),
				new TradeSignal(TradeType.SHORT, Contract.stock("DEXO"), 7000, 6.42));
		assertNull(extractor.parseTrades(new Msg(src, "shorted 1000 dexo at $6.45")));

		assertTrade(extractor.parseTrades(new Msg(src, "bought 100000 GBOED at $0.13")),
				new TradeSignal(TradeType.BUY, Contract.stock("GBOED"), 100000, 0.13));
		assertTrade(extractor.parseTrades(new Msg(src, "bought 100k GBOED at $.13")),
				new TradeSignal(TradeType.BUY, Contract.stock("GBOED"), 100000, 0.13));
		assertTrade(extractor.parseTrades(new Msg(src, "BUY 50K GBOED at .035")),
				new TradeSignal(TradeType.BUY, Contract.stock("GBOED"), 50000, 0.035));
		assertTrade(extractor.parseTrades(new Msg(src, "bought 50K GBOED at 6 cents")),
				new TradeSignal(TradeType.BUY, Contract.stock("GBOED"), 50000, 0.06));
		assertTrade(extractor.parseTrades(new Msg(src, "shorted 25,000 CPMCF at 64 cents")),
				new TradeSignal(TradeType.SHORT, Contract.stock("CPMCF"), 25000, 0.64));

		assertTrade(extractor.parseTrades(new Msg(src, "Reshorted 30k CPMCF at .36 premarket, I'd wait to get a better price, but shares are going fast")),
				new TradeSignal(TradeType.SHORT, Contract.stock("CPMCF"), 30000, 0.36));
		assertTrade(extractor.parseTrades(new Msg(src, "shorted 3.5k GSL at 5.15 on the intraday breakdown, ")),
				new TradeSignal(TradeType.SHORT, Contract.stock("GSL"), 3500, 5.15));
		assertTrade(extractor.parseTrades(new Msg(src, "shorted 200 CDTID at 36.50, risky but small postiion, goal is low 30s")),
				new TradeSignal(TradeType.SHORT, Contract.stock("CDTID"), 200, 36.50));
		assertTrade(extractor.parseTrades(new Msg(src, "covered 200 CDTID at 33ish, near my goal of the low 30s")),
				new TradeSignal(TradeType.COVER, Contract.stock("CDTID"), 200, 33));
		assertTrade(extractor.parseTrades(new Msg(src, "shorted 9k RITT at 3.90ish via Thinkorswim, nice to hold overnight")),
				new TradeSignal(TradeType.SHORT, Contract.stock("RITT"), 9000, 3.90));
		assertTrade(extractor.parseTrades(new Msg(src, "reshorted 20k PIP at 4.21, looks to be breaking down again, ")),
				new TradeSignal(TradeType.SHORT, Contract.stock("PIP"), 20000, 4.21));
		assertTrade(extractor.parseTrades(new Msg(src, "Shorted 3k AENY at 96 cents via IB, pure pump stock up 100% off its lows, ")),
				new TradeSignal(TradeType.SHORT, Contract.stock("AENY"), 3000, 0.96));

		assertTrade(extractor.parseTrades(new Msg(src, "bought 5k ZAGG at 7.25, solid afternoon breakout, this could keep going LQMT-style since its an AAPL related penny stock, goal is 8+ tomorrow")),
				new TradeSignal(TradeType.BUY, Contract.stock("ZAGG"), 5000, 7.25));
		//assertTrade(extractor.parseTrades(new Msg(src, "Sold ZAGG at 7.40ish for small profit, sellers look persistent")),
		//		new TradeSignal(TradeType.SELL, Contract.stock("ZAGG"), 0, 7.40));

		assertTrade(extractor.parseTrades(new Msg(src, "Reshorted 30k POTG at 1.0867, shares became available so I gotta focus")),
				new TradeSignal(TradeType.SHORT, Contract.stock("POTG"), 30000, 1.0867));
		//assertTrade(extractor.parseTrades(new Msg(src, "Sold RYUN for small gains at $2.42ish, gotta focus")),
		//		new TradeSignal(TradeType.SELL, Contract.stock("RYUN"), 0, 2.42));
		
		//assertTrade(extractor.parseTrades(new Msg(src, "Reshorted 17k SAPX in the 3.30s on the slight bounce off day low of 3.07")),
		//		new TradeSignal(TradeType.SHORT, Contract.stock("SAPX"), 17000, 3.30));
		
		assertTrade(extractor.parseTrades(new Msg(src, "i bought 10,000 XCO at 2.30. it's up 40% today")),
				new TradeSignal(TradeType.BUY, Contract.stock("XCO"), 10000, 2.30));
		assertTrade(extractor.parseTrades(new Msg(src, "I bought 100,000 DCTH .163")),
				new TradeSignal(TradeType.BUY, Contract.stock("DCTH"), 100000, 0.163));

		assertNull(extractor.parseTrades(new Msg(src, "added 5000 MOSY at 2.08")));
	}

	private void assertTrade(Collection<TradeSignal> trades, TradeSignal trade) {
		Assert.assertNotNull("TradeSignal expected: <" + trade + ">", trades);
		Assert.assertTrue("TradeSignal expected: <" + trade + ">", trades.size() == 1);
		Assert.assertEquals(trade, trades.iterator().next());
	}
}
