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

import org.junit.Assert;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.eventtrader.TradeType;
import com.jgoetsch.eventtrader.source.MsgHandler;
import com.jgoetsch.eventtrader.source.parser.ProfidingMsgParser;
import com.jgoetsch.tradeframework.Contract;

import junit.framework.TestCase;

public class ProfidingMsgParserTest extends TestCase {

	private ProfidingMsgParser msgParser = new ProfidingMsgParser();

	public void testCommentary() throws Exception {
		msgParser.parseContent(
			"{\"command\":\"Commentary\",\"message\":{\"newsletter\":3,\"date\":1344264740426,\"username\":\"timothysykes\",\"image\":\"http://a1.twimg.com/profile_images/1166026278/TimCover1_normal.jpg\",\"msg\":\"Message boards were speculating early in the year about their ability to manufacture iPad tablet screens, I'm gonna listen to conference call now and see if they confirmed it, then this could REALLY run, otherwise probly just gonna sell for a small gain \",\"msgId\":27889}}",
			null, new MsgHandler() {
			public boolean newMsg(Msg msg) {
				Assert.assertEquals(Msg.class, msg.getClass());
				Assert.assertEquals("timothysykes", msg.getSourceName());
				Assert.assertEquals("http://a1.twimg.com/profile_images/1166026278/TimCover1_normal.jpg", msg.getImageUrl());
				Assert.assertEquals(1344264740426L, msg.getDate().toDate().getTime());
				return false;
			}
		});

		// a classic one for sure
		msgParser.parseContent(
				"{\"command\":\"Commentary\",\"message\":{\"newsletter\":3,\"date\":1344347965675,\"username\":\"timothysykes\",\"image\":\"http://a1.twimg.com/profile_images/1166026278/TimCover1_normal.jpg\",\"msg\":\"I'm pissed at your laziness and pissed at myself for not following through on my CRMB buy idea int he 2.80s, now 3.50+, what a waste we all are\",\"msgId\":28006}}",
				null, new MsgHandler() {
				public boolean newMsg(Msg msg) {
					Assert.assertEquals(Msg.class, msg.getClass());
					Assert.assertEquals("timothysykes", msg.getSourceName());
					Assert.assertEquals(1344347965675L, msg.getDate().toDate().getTime());
					Assert.assertTrue(msg.getMessage().startsWith("I'm pissed at your laziness and pissed at myself"));
					return false;
				}
			});
	}

	public void testExit() throws Exception {
		msgParser.parseContent(
				"{\"command\":\"Trade\",\"message\":{\"entry\":{\"exitPrice\":3.2,\"dateAdded\":1344346779000,\"newsletterIds\":[3,26,2,24],\"type\":\"Short Stock\",\"dateClosed\":1344347916726,\"amount\":null,\"username\":\"timothysykes\",\"compareDate\":1344347916726,\"ticker\":\"CRMB\",\"action\":\"Covered\",\"entryPrice\":3.1,\"shortSell\":true,\"shortUrl\":\"1Mn25g\",\"optionType\":\"CALL\",\"callOption\":true,\"entryComments\":\"Spiked waaaay too much on low volume so I shorted some, shares available at IB and SureTrader\",\"percentage\":null,\"optionExpiration\":null,\"entryDate\":1344346779000,\"futuresMonth\":0,\"futuresYear\":0,\"shares\":300,\"entryType\":\"STOCK\",\"exitDate\":1344347916726,\"optionStrike\":null,\"comments\":\"Superman now spiking it due to its low float, took a small loss on the remainder of my short, overall small profit, definite potential reshort on any more big spike\",\"openTrade\":false},\"msgId\":28001,\"image\":\"http://a1.twimg.com/profile_images/1166026278/TimCover1_normal.jpg\"}}",
			null, new MsgHandler() {
			public boolean newMsg(Msg msg) {
				Assert.assertEquals(TradeSignal.class, msg.getClass());
				TradeSignal trade = (TradeSignal) msg;
				Assert.assertEquals("timothysykes", trade.getSourceName());
				Assert.assertEquals("http://a1.twimg.com/profile_images/1166026278/TimCover1_normal.jpg", trade.getImageUrl());
				Assert.assertEquals(1344347916726L, trade.getDate().toDate().getTime());
				Assert.assertEquals(TradeType.COVER, trade.getType());
				Assert.assertEquals(Contract.stock("CRMB"), trade.getContract());
				Assert.assertEquals(300, trade.getNumShares());
				Assert.assertEquals(3.2, trade.getPrice(), 0.0001);
				return false;
			}
		});
	}

	/*
	@SuppressWarnings("rawtypes")
	public void testPartialEntry() throws Exception {
		msgParser.parseData("alert", (Map)JSONValue.parse(
			"{\"partial_entry\":{\"exitPrice\":null,\"dateAdded\":1344264185000,\"newsletterIds\":[3,26,2,24],\"type\":\"Long Stock\",\"dateClosed\":null,\"amount\":null,\"username\":\"timothysykes\",\"compareDate\":1344264185000,\"ticker\":\"NTE\",\"action\":\"Bought\",\"entryPrice\":7.0453,\"shortSell\":false,\"shortUrl\":\"1Mn22v\",\"optionType\":\"CALL\",\"callOption\":true,\"entryComments\":\"Bought small position on GIANT earnings win, this was a runner from a decade ago, now manufacturing tablets/smartphones, can't trust numbers 100% since they're a shady Chinese company, but it's the right sector and the #s look amazing, might sell into any big spike as I dig further into these blowout numbers\",\"percentage\":null,\"optionExpiration\":null,\"entryDate\":1344264185000,\"futuresMonth\":0,\"futuresYear\":0,\"shares\":6000,\"entryType\":\"STOCK\",\"exitDate\":null,\"optionStrike\":null,\"comments\":null,\"openTrade\":true},\"msgId\":27895,\"image\":\"http://a1.twimg.com/profile_images/1166026278/TimCover1_normal.jpg\",\"partial\":{\"shares\":4240,\"short\":false,\"tradeDate\":1344266235912,\"transactionType\":\"Bought\",\"price\":7.06,\"adding\":true}}"
			), new MsgHandler() {
			public boolean newMsg(Msg msg) {
				Assert.assertEquals(TradeSignal.class, msg.getClass());
				TradeSignal trade = (TradeSignal) msg;
				Assert.assertEquals("timothysykes", trade.getSourceName());
				Assert.assertEquals("http://a1.twimg.com/profile_images/1166026278/TimCover1_normal.jpg", trade.getImageUrl());
				Assert.assertEquals(1344266235912L, trade.getDate().toDate().getTime());
				Assert.assertEquals(TradeSignal.TYPE_BUY, trade.getType());
				Assert.assertEquals(Contract.stock("NTE"), trade.getContract());
				Assert.assertEquals(4240, trade.getNumShares());
				Assert.assertEquals(7.06, trade.getPrice());
				return false;
			}
		});
	}

	@SuppressWarnings("rawtypes")
	public void testPartialExit() throws Exception {
		msgParser.parseData("alert", (Map)JSONValue.parse(
			"{\"partial_entry\":{\"exitPrice\":null,\"dateAdded\":1344346779000,\"newsletterIds\":[3,26,2,24],\"type\":\"Short Stock\",\"dateClosed\":null,\"amount\":null,\"username\":\"timothysykes\",\"compareDate\":1344346779000,\"ticker\":\"CRMB\",\"action\":\"Shorted\",\"entryPrice\":3.1,\"shortSell\":true,\"shortUrl\":\"1Mn25g\",\"optionType\":\"CALL\",\"callOption\":true,\"entryComments\":\"Spiked waaaay too much on low volume so I shorted some, shares available at IB and SureTrader\",\"percentage\":null,\"optionExpiration\":null,\"entryDate\":1344346779000,\"futuresMonth\":0,\"futuresYear\":0,\"shares\":300,\"entryType\":\"STOCK\",\"exitDate\":null,\"optionStrike\":null,\"comments\":null,\"openTrade\":true},\"msgId\":27997,\"image\":\"http://a1.twimg.com/profile_images/1166026278/TimCover1_normal.jpg\",\"partial\":{\"shares\":1400,\"short\":true,\"tradeDate\":1344347770227,\"transactionType\":\"Covered\",\"price\":3.02,\"adding\":false}}"
			), new MsgHandler() {
			public boolean newMsg(Msg msg) {
				Assert.assertEquals(TradeSignal.class, msg.getClass());
				TradeSignal trade = (TradeSignal) msg;
				Assert.assertEquals("timothysykes", trade.getSourceName());
				Assert.assertEquals("http://a1.twimg.com/profile_images/1166026278/TimCover1_normal.jpg", trade.getImageUrl());
				Assert.assertEquals(1344347770227L, trade.getDate().toDate().getTime());
				Assert.assertEquals(TradeSignal.TYPE_COVER, trade.getType());
				Assert.assertEquals(Contract.stock("CRMB"), trade.getContract());
				Assert.assertEquals(1400, trade.getNumShares());
				Assert.assertEquals(3.02, trade.getPrice());
				return false;
			}
		});
	}

	@SuppressWarnings("rawtypes")
	public void testIntPrice() throws Exception {
		msgParser.parseData("alert", (Map)JSONValue.parse(
			"{\"entry\":{\"exitPrice\":null,\"dateAdded\":1346162335587,\"newsletterIds\":[3,2,24,26],\"type\":\"Short Stock\",\"dateClosed\":null,\"amount\":null,\"username\":\"timothysykes\",\"compareDate\":1346162335587,\"ticker\":\"NTE\",\"action\":\"Shorted\",\"entryPrice\":9,\"shortSell\":true,\"shortUrl\":\"1Mn43l\",\"optionType\":\"CALL\",\"callOption\":true,\"entryComments\":\"Shorted some into this mammoth spike, this is not a volatile stock, Seeking Alpha hyped it up based on rumors, goal is to make 50 cents/share in 1-2 days\",\"percentage\":null,\"optionExpiration\":null,\"entryDate\":1346162335587,\"futuresMonth\":0,\"futuresYear\":0,\"shares\":2000,\"entryType\":\"STOCK\",\"exitDate\":null,\"optionStrike\":null,\"comments\":null,\"openTrade\":true},\"msgId\":30040,\"image\":\"http://a1.twimg.com/profile_images/1166026278/TimCover1_normal.jpg\"}"
			), new MsgHandler() {
			public boolean newMsg(Msg msg) {
				Assert.assertEquals(TradeSignal.class, msg.getClass());
				TradeSignal trade = (TradeSignal) msg;
				Assert.assertEquals("timothysykes", trade.getSourceName());
				Assert.assertEquals(1346162335587L, trade.getDate().toDate().getTime());
				Assert.assertEquals(TradeSignal.TYPE_SHORT, trade.getType());
				Assert.assertEquals(Contract.stock("NTE"), trade.getContract());
				Assert.assertEquals(2000, trade.getNumShares());
				Assert.assertEquals(9.0, trade.getPrice());
				return false;
			}
		});
	}

	@SuppressWarnings("rawtypes")
	public void testOption() throws Exception {
		msgParser.parseData("alert", (Map)JSONValue.parse(
			"{\"entry\": {\"exitPrice\": null,\"dateAdded\": 1355246510000,\"newsletterIds\": [58,59],\"type\": \"Long Option\",\"dateClosed\": null,\"amount\": null,\"username\": \"super_trades\",\"compareDate\": 1355246510000,\"ticker\": \"SGYP\",\"action\": \"Bought\",\"entryPrice\": 0.7,\"shortSell\": false,\"shortUrl\": \"1MnC9Y\",\"optionType\": \"CALL\",\"callOption\": true,\"entryComments\": \"All or nothing option position like SRPT was.  If drug data is good, I see targets out there from 15-40.  If data is not good,  then these options WILL BE WORTH ZERO.  Data due first week of January.  \",\"percentage\": null,\"optionExpiration\": 1358571600000,\"entryDate\": 1355246510000,\"futuresMonth\": 0,\"futuresYear\": 0,\"shares\": 50,\"entryType\": \"OPTION\",\"exitDate\": null,\"optionStrike\": 7.5,\"comments\": null,\"openTrade\": true},\"msgId\":30040,\"image\":\"http://a1.twimg.com/profile_images/1166026278/TimCover1_normal.jpg\"}"
			), new MsgHandler() {
			public boolean newMsg(Msg msg) {
				Assert.assertEquals(TradeSignal.class, msg.getClass());
				TradeSignal trade = (TradeSignal) msg;
				Assert.assertEquals("super_trades", trade.getSourceName());
				Assert.assertEquals(1355246510000L, trade.getDate().toDate().getTime());
				Assert.assertEquals(TradeSignal.TYPE_BUY, trade.getType());
				Assert.assertEquals("SGYP", trade.getContract().getSymbol());
				Assert.assertEquals(Contract.OPTIONS, trade.getContract().getType());
				Assert.assertEquals(50, trade.getNumShares());
				Assert.assertEquals(0.7, trade.getPrice());
				return false;
			}
		});
	}
*/
}
