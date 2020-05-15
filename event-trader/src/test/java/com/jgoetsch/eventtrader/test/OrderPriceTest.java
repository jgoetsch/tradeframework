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

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.Test;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.eventtrader.TradeType;
import com.jgoetsch.eventtrader.order.price.AskPrice;
import com.jgoetsch.eventtrader.order.price.BidPrice;
import com.jgoetsch.eventtrader.order.price.ClosePrice;
import com.jgoetsch.eventtrader.order.price.ConstrainedPrice;
import com.jgoetsch.eventtrader.order.price.LastPrice;
import com.jgoetsch.eventtrader.order.price.MidpointPrice;
import com.jgoetsch.eventtrader.order.price.OffsetOrderPrice;
import com.jgoetsch.eventtrader.order.price.PriceMappedTickSize;
import com.jgoetsch.tradeframework.Contract;
import com.jgoetsch.tradeframework.marketdata.MarketData;
import com.jgoetsch.tradeframework.marketdata.SimpleMarketData;

public class OrderPriceTest {

	@Test
	public void testAskPrice() throws Exception {
		testOffsetPrice(new AskPrice(), new SimpleMarketData(40.0, 41.0, 40.8), 41, 40, new BigDecimal("0.01"));
	}

	@Test
	public void testBidPrice() throws Exception {
		testOffsetPrice(new BidPrice(), new SimpleMarketData(40.0, 41.0, 40.8), 40, 41, new BigDecimal("0.01"));
	}

	@Test
	public void testLastPrice() throws Exception {
		testOffsetPrice(new LastPrice(), new SimpleMarketData(40.0, 41.0, 40.8), 40.8, 40.8, new BigDecimal("0.01"));
	}
	
	@Test
	public void testClosePrice() throws Exception {
		SimpleMarketData marketData = new SimpleMarketData(40.0, 41.0, 40.8);
		marketData.setClose(37.0);
		testOffsetPrice(new ClosePrice(), marketData, 37, 37, new BigDecimal("0.01"));
	}

	@Test
	public void testMidpointPrice() throws Exception {
		MidpointPrice orderPrice = new MidpointPrice();
		testOffsetPrice(orderPrice, new SimpleMarketData(40.0, 41.0, 40.8), 40.5, 40.5, new BigDecimal("0.01"));
		testOffsetPrice(orderPrice, new SimpleMarketData(40.0, 40.175, 40.125), 40.09, 40.09, new BigDecimal("0.01"));
		testOffsetPrice(orderPrice, new SimpleMarketData(0.10, 0.175, 0.125), 0.1375, 0.1375, new BigDecimal("0.025"));
		orderPrice.setTickRounding(new PriceMappedTickSize(new BigDecimal("0.025")));
		testOffsetPrice(orderPrice, new SimpleMarketData(40.0, 40.175, 40.125), 40.075, 40.1, new BigDecimal("0.025"));
		orderPrice.setTickRounding(new PriceMappedTickSize(new BigDecimal("0.5")));
		testOffsetPrice(orderPrice, new SimpleMarketData(40.0, 40.175, 40.125), 40.0, 40.0, new BigDecimal("0.5"));
	}

	@Test
	public void testConstrainedPrice() throws Exception {
		AskPrice p1 = new AskPrice();
		p1.setOffset(0.10);
		MidpointPrice p2 = new MidpointPrice();
		p2.setOffset(0.20);
		ConstrainedPrice orderPrice = new ConstrainedPrice(Arrays.asList(p1, p2));
		assertEquals(40.7, orderPrice.getValue(new TradeSignal(TradeType.BUY, Contract.stock("TEST"), new Msg()), new SimpleMarketData(40, 41, 40.8)), .0001);
		assertEquals(40.3, orderPrice.getValue(new TradeSignal(TradeType.SELL, Contract.stock("TEST"), new Msg()), new SimpleMarketData(40, 41, 40.8)), .0001);
		assertEquals(40.2, orderPrice.getValue(new TradeSignal(TradeType.BUY, Contract.stock("TEST"), new Msg()), new SimpleMarketData(40, 40.1, 40.8)), .0001);
		assertEquals(39.9, orderPrice.getValue(new TradeSignal(TradeType.SELL, Contract.stock("TEST"), new Msg()), new SimpleMarketData(40, 40.1, 40.8)), .0001);
	}

	private void testOffsetPrice(OffsetOrderPrice orderPrice, MarketData marketData, double expectedBuy, double expectedSell, BigDecimal tickSize) throws Exception {
		for (int i = -5; i < 5; i++) {
			double offs = tickSize.multiply(new BigDecimal(i)).doubleValue();
			orderPrice.setOffset(offs);
			assertEquals(expectedBuy + offs, orderPrice.getValue(new TradeSignal(TradeType.BUY, Contract.stock("TEST"), new Msg()), marketData), .0001);
			assertEquals(expectedSell - offs, orderPrice.getValue(new TradeSignal(TradeType.SELL, Contract.stock("TEST"), new Msg()), marketData), .0001);
		}
	}
}
