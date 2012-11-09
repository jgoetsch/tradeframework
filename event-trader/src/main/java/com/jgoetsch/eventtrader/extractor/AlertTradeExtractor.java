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
package com.jgoetsch.eventtrader.extractor;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.tradeframework.Contract;

/**
 * Regex based parser to extract a TradeSignal from a textual trade alert service, i.e. TimAlerts.
 * The regex supplied accommodates various common irregularities.
 * 
 * 	alertPattern is the regex and should yield the following groups
 * 		1: Action - bought, sold, shorted, covered
 * 		2: Position size
 * 		3: Symbol
 * 		4: Price
 * 		5: optional price unit ("cents")
 * 
 * @author jgoetsch
 *
 */
public class AlertTradeExtractor extends TradeExtractor {

	private static Pattern alertPattern = Pattern.compile(
		"(?:\\w+:?\\s*)?(?:[Jj]ust)?\\s*([Cc]over(?:ed)|(?:[Rr]e)?[Ss]hort(?:ed)?|[Bb]oug?h?t|[Bb]uy|[Ss]old|[Ss]ell)\\s*([\\d\\,\\.kK]*|[Aa]ll)\\s*([A-Z]+)\\s+(?:\\w+\\s+){0,5}?(?:[Aa]t\\s+)\\$?(\\d*\\.?\\d*)\\s*([Cc]ents?)?");
	
	public Collection<TradeSignal> parseTrades(Msg msg) {
		Matcher mAlert = getAlertPattern().matcher(msg.getMessage());
		if (mAlert.lookingAt()) {
			TradeSignal trade = new TradeSignal(msg);

			if (mAlert.group(1).equalsIgnoreCase("Shorted") || mAlert.group(1).equalsIgnoreCase("Short") || mAlert.group(1).equalsIgnoreCase("Reshorted"))
				trade.setType(TradeSignal.TYPE_SHORT);
			else if (mAlert.group(1).equalsIgnoreCase("Covered") || mAlert.group(1).equalsIgnoreCase("Cover"))
				trade.setType(TradeSignal.TYPE_COVER);
			else if (mAlert.group(1).equalsIgnoreCase("Bought") || mAlert.group(1).equalsIgnoreCase("Buy"))
				trade.setType(TradeSignal.TYPE_BUY);
			else if (mAlert.group(1).equalsIgnoreCase("Sold") || mAlert.group(1).equalsIgnoreCase("Sell"))
				trade.setType(TradeSignal.TYPE_SELL);

			if (mAlert.group(2).length() > 0 && !mAlert.group(2).equalsIgnoreCase("all")) {
				try {
					float shares = NumberFormat.getInstance().parse(mAlert.group(2)).floatValue();
					if (mAlert.group(2).endsWith("k") || mAlert.group(2).endsWith("K"))
						shares *= 1000;
					trade.setNumShares(Math.round(shares));
				} catch (ParseException e) { }
			}
			trade.setContract(Contract.stock(mAlert.group(3)));
			try {
				trade.setPrice(Double.parseDouble(mAlert.group(4)));
			} catch (NumberFormatException e) { }
			if ("cents".equalsIgnoreCase(mAlert.group(5)))
				trade.setPrice(trade.getPrice() / 100);

			return Collections.singletonList(trade);
		}
		else
			return null;
	}

	public static void setAlertPattern(String alertPattern) {
		AlertTradeExtractor.alertPattern = Pattern.compile(alertPattern);
	}

	public static void setAlertPattern(Pattern alertPattern) {
		AlertTradeExtractor.alertPattern = alertPattern;
	}

	public static Pattern getAlertPattern() {
		return alertPattern;
	}

}
