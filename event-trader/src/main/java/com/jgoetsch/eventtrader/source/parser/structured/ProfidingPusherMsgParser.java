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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
{
    "command": "Trade",
    "message": {
        "type": "EntryDingMessage",
        "msgId": 82351,
        "newsletter": 3,
        "date": 1395669845622,
        "username": "timothysykes",
        "image": "https://pbs.twimg.com/profile_images/1166026278/TimCover1_bigger.jpg",
        "entry": {
            "amount": 2000,
            "shares": 20000,
            "ticker": "MNGA",
            "comments": "Got the predictable drop off the bounce highs, but it's taking forever to make a bigger drop that I want so I'm taking safe quick $2k profits to start the week on a good note, this morning bounce on stocks that have just turned red is a common pattern in my video lessons",
            "compareDate": 1395669845000,
            "dateAdded": 1395669130000,
            "dateClosed": 1395669845000,
            "entryDate": 1395669130000,
            "exitDate": 1395669845000,
            "entryComments": "Shorted the morning bounce, goal is to cover in the low 2s or even high 1s",
            "entryPrice": 2.3,
            "exitPrice": 2.2,
            "openTrade": false,
            "entryType": "STOCK",
            "callOption": true,
            "optionType": "CALL",
            "optionStrike": 0,
            "type": "Short Stock",
            "username": "timothysykes",
            "shortSell": true,
            "shortUrl": "1Moffe",
            "percentage": 4.35,
            "futuresMonth": 0,
            "futuresYear": 0
        }
    }
}
 */
public class ProfidingPusherMsgParser implements StructuredMsgParser {
	private Logger log = LoggerFactory.getLogger(getClass());

	@SuppressWarnings("rawtypes")
	public boolean parseData(Map data, MsgHandler handler) throws MsgParseException {
		String type = (String)data.get("command");
		data = (Map)data.get("message");
        Object ts = data.get("date");
        if (ts != null && Number.class.isAssignableFrom(ts.getClass()))
        	log.info("{} alert latency was {} ms", type, System.currentTimeMillis() - ((Number)ts).longValue());

        Msg msg = null;
		if ("Trade".equals(type) || "PartialTrade".equals(type)) {
			Map partial = (Map)data.get("partialEntry");
			Map entry = (Map)data.get("entry");

			Contract contract = new Contract();
			contract.setSymbol((String)entry.get("ticker"));
			if ("OPTION".equals(entry.get("entryType"))) {
				contract.setType(Contract.OPTIONS);
			}
			else if ("FUTURES".equals(entry.get("entryType")))
				contract.setType(Contract.FUTURES);

			TradeSignal trade = new TradeSignal();
			trade.setSourceName((String)entry.get("username"));
			trade.setContract(contract);
			trade.setNumShares(((Number)(partial != null ? partial : entry).get("shares")).intValue());
			trade.setImageUrl((String)data.get("image"));
			trade.setPartial(partial != null);

			if (partial != null) {
				String action = (String)(partial.get("transactionType"));
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
			} else {
				Boolean isShort = (Boolean)entry.get("shortSell");
				if (isShort) {
					if (entry.get("dateClosed") == null)
						trade.setType(TradeSignal.TYPE_SHORT);
					else
						trade.setType(TradeSignal.TYPE_COVER);
				}
				else {
					if (entry.get("dateClosed") == null)
						trade.setType(TradeSignal.TYPE_BUY);
					else
						trade.setType(TradeSignal.TYPE_SELL);
				}
			}

			if (trade.isPartial()) {
				trade.setDate(new DateTime(partial.get("tradeDate")));
				trade.setPrice(((Number)partial.get("price")).doubleValue());
				trade.setMessage(trade.getTradeString() + "\n" + ((Boolean)entry.get("shortSell") ? "Short " : "Long ")
						+ entry.get("shares") + " total at "
						+ DecimalFormat.getCurrencyInstance().format(entry.get("entryPrice")) + " average"
						+ "\n" + (String)partial.get("comments"));
			}
			else if (trade.isExit()) {
				trade.setDate(new DateTime(entry.get("dateClosed")));
				trade.setPrice(((Number)entry.get("exitPrice")).doubleValue());
				trade.setMessage(trade.getTradeString() + "\n" + (String)entry.get("comments"));
			}
			else {
				trade.setDate(new DateTime(entry.get("entryDate")));
				trade.setPrice(((Number)entry.get("entryPrice")).doubleValue());
				trade.setMessage(trade.getTradeString() + "\n" + (String)entry.get("entryComments"));
			}
			msg = trade;
		}
		else if ("Commentary".equals(type)) {
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
