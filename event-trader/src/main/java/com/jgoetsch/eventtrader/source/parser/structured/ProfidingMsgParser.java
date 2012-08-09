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
package com.jgoetsch.eventtrader.source.parser.structured;

import java.text.DecimalFormat;
import java.util.Map;

import org.joda.time.DateTime;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.eventtrader.source.MsgHandler;
import com.jgoetsch.eventtrader.source.parser.MsgParseException;
import com.jgoetsch.tradeframework.Contract;

/**
 * Decodes structured Profiding alert into a Msg object.
 * 
 * @author jgoetsch
 * 
 *         {"name":"alert","args":[{"partial_entry":{"exitPrice":null,
 *         "dateAdded"
 *         :1344264185000,"newsletterIds":[3,26,2,24],"type":"Long Stock"
 *         ,"dateClosed"
 *         :null,"amount":null,"username":"timothysykes","compareDate"
 *         :1344264185000
 *         ,"ticker":"NTE","action":"Bought","entryPrice":7.0453,"shortSell"
 *         :false,"shortUrl":"1Mn22v","optionType":"CALL","callOption":true,
 *         "entryComments":
 *         "Bought small position on GIANT earnings win, this was a runner from a decade ago, now manufacturing tablets/smartphones, can't trust numbers 100% since they're a shady Chinese company, but it's the right sector and the #s look amazing, might sell into any big spike as I dig further into these blowout numbers"
 *         ,"percentage":null,"optionExpiration":null,"entryDate":1344264185000,
 *         "futuresMonth"
 *         :0,"futuresYear":0,"shares":6000,"entryType":"STOCK","exitDate"
 *         :null,"optionStrike"
 *         :null,"comments":null,"openTrade":true},"msgId":27895,"image":
 *         "http://a1.twimg.com/profile_images/1166026278/TimCover1_normal.jpg"
 *         ,"partial":{"shares":4240,"short":false,"tradeDate":1344266235912,
 *         "transactionType":"Bought","price":7.06,"adding":true}}]}
 */
public class ProfidingMsgParser implements StructuredMsgParser {

	@SuppressWarnings("rawtypes")
	public boolean parseData(String type, Map data, MsgHandler handler) throws MsgParseException {
		Msg msg = null;
		if ("alert".equals(type)) {
			Map partial = (Map)data.get("partial");
			Map entry = (Map)data.get(partial != null ? "partial_entry" : "entry");
			TradeSignal trade = new TradeSignal();
			trade.setSourceName((String)entry.get("username"));
			trade.setContract(Contract.stock((String)entry.get("ticker")));
			trade.setNumShares(((Long)(partial != null ? partial : entry).get("shares")).intValue());
			trade.setImageUrl((String)data.get("image"));
			trade.setPartial(partial != null);

			String action = (String)(partial != null ? partial.get("transactionType") : entry.get("action"));
			if ("Bought".equalsIgnoreCase(action))
				trade.setType(TradeSignal.TYPE_BUY);
			else if ("Sold".equalsIgnoreCase(action))
				trade.setType(TradeSignal.TYPE_SELL);
			else if ("Shorted".equalsIgnoreCase(action))
				trade.setType(TradeSignal.TYPE_SHORT);
			else if ("Covered".equalsIgnoreCase(action))
				trade.setType(TradeSignal.TYPE_COVER);
			else
				throw new MsgParseException("Unknown alert action " + action);

			if (trade.isPartial()) {
				trade.setDate(new DateTime(partial.get("tradeDate")));
				trade.setPrice((Double)partial.get("price"));
				trade.setMessage(trade.getTradeString() + "\n" + ((Boolean)entry.get("shortSell") ? "Short " : "Long ")
						+ entry.get("shares") + " total at "
						+ DecimalFormat.getCurrencyInstance().format(entry.get("entryPrice")) + " average");
			}
			else if (trade.isExit()) {
				trade.setDate(new DateTime(entry.get("dateClosed")));
				trade.setPrice((Double)entry.get("exitPrice"));
				trade.setMessage(trade.getTradeString() + "\n" + (String)entry.get("comments"));
			}
			else {
				trade.setDate(new DateTime(entry.get("entryDate")));
				trade.setPrice((Double)entry.get("entryPrice"));
				trade.setMessage(trade.getTradeString() + "\n" + (String)entry.get("entryComments"));
			}
			msg = trade;
		}
		else if ("commentary".equals(type)) {
			msg = new Msg(new DateTime(data.get("date")), (String)data.get("username"), (String)data.get("msg"));
			msg.setImageUrl((String)data.get("image"));
		}

		if (msg != null) {
			msg.setSourceType(type);
			return handler.newMsg(msg);
		}
		else
			return true;
	}
}
