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

import java.util.Arrays;

import org.junit.Assert;

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
import com.jgoetsch.tradeframework.Contract;
import com.jgoetsch.tradeframework.marketdata.MarketData;
import com.jgoetsch.tradeframework.marketdata.SimpleMarketData;

import junit.framework.TestCase;

public class OrderPriceTest extends TestCase {

	public void testAskPrice() throws Exception {
		testOffsetPrice(new AskPrice(), new SimpleMarketData(40.0, 41.0, 40.8), 41, 40, 0.01);
	}
	public void testBidPrice() throws Exception {
		testOffsetPrice(new BidPrice(), new SimpleMarketData(40.0, 41.0, 40.8), 40, 41, 0.01);
	}
	public void testLastPrice() throws Exception {
		testOffsetPrice(new LastPrice(), new SimpleMarketData(40.0, 41.0, 40.8), 40.8, 40.8, 0.01);
	}
	public void testClosePrice() throws Exception {
		SimpleMarketData marketData = new SimpleMarketData(40.0, 41.0, 40.8);
		marketData.setClose(37.0);
		testOffsetPrice(new ClosePrice(), marketData, 37, 37, 0.01);
	}
	public void testMidpointPrice() throws Exception {
		MidpointPrice orderPrice = new MidpointPrice();
		testOffsetPrice(orderPrice, new SimpleMarketData(40.0, 41.0, 40.8), 40.5, 40.5, 0.01);
		testOffsetPrice(orderPrice, new SimpleMarketData(40.0, 40.175, 40.125), 40.09, 40.09, 0.01);
		orderPrice.setTickSize(0.0001);
		testOffsetPrice(orderPrice, new SimpleMarketData(40.0, 40.175, 40.125), 40.0875, 40.0875, 0.025);
		orderPrice.setTickSize(0.025);
		testOffsetPrice(orderPrice, new SimpleMarketData(40.0, 40.175, 40.125), 40.075, 40.075, 0.025);
		orderPrice.setTickSize(0.5);
		testOffsetPrice(orderPrice, new SimpleMarketData(40.0, 40.175, 40.125), 40.0, 40.0, 0.5);
	}
	public void testConstrainedPrice() throws Exception {
		AskPrice p1 = new AskPrice();
		p1.setOffset(0.10);
		MidpointPrice p2 = new MidpointPrice();
		p2.setOffset(0.20);
		ConstrainedPrice orderPrice = new ConstrainedPrice(Arrays.asList(p1, p2));
		Assert.assertEquals(40.7, orderPrice.getValue(new TradeSignal(TradeType.BUY, Contract.stock("TEST"), new Msg()), new SimpleMarketData(40, 41, 40.8)), .0001);
		Assert.assertEquals(40.3, orderPrice.getValue(new TradeSignal(TradeType.SELL, Contract.stock("TEST"), new Msg()), new SimpleMarketData(40, 41, 40.8)), .0001);
		Assert.assertEquals(40.2, orderPrice.getValue(new TradeSignal(TradeType.BUY, Contract.stock("TEST"), new Msg()), new SimpleMarketData(40, 40.1, 40.8)), .0001);
		Assert.assertEquals(39.9, orderPrice.getValue(new TradeSignal(TradeType.SELL, Contract.stock("TEST"), new Msg()), new SimpleMarketData(40, 40.1, 40.8)), .0001);
	}

	private void testOffsetPrice(OffsetOrderPrice orderPrice, MarketData marketData, double expectedBuy, double expectedSell, double tickSize) throws Exception {
		for (int i = -5; i < 5; i++) {
			orderPrice.setOffset(i * tickSize);
			Assert.assertEquals(expectedBuy + i * tickSize, orderPrice.getValue(new TradeSignal(TradeType.BUY, Contract.stock("TEST"), new Msg()), marketData), .0001);
			Assert.assertEquals(expectedSell - i * tickSize, orderPrice.getValue(new TradeSignal(TradeType.SELL, Contract.stock("TEST"), new Msg()), marketData), .0001);
		}
	}
}
