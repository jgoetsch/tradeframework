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

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.eventtrader.TradeType;

public class SymbolTradeExtractor extends TradeExtractor {

	private Pattern symbolPattern = Pattern.compile("\\b(?:NASDAQ|Nasdaq|NYSE|OTC|OTCBB)\\:\\s*([A-Z]{2,5})\\b");
	private TradeType side;

	@Override
	public Collection<TradeSignal> parseTrades(Msg msg) {
		Matcher m = symbolPattern.matcher(msg.getMessage());
		if (m.find())
			return Collections.singleton(new TradeSignal(side, m.group(1), msg));
		else
			return null;
	}

	public void setSymbolPattern(Pattern symbolPattern) {
		this.symbolPattern = symbolPattern;
	}

	public Pattern getSymbolPattern() {
		return symbolPattern;
	}

	public TradeType getSide() {
		return side;
	}

	public void setSide(TradeType side) {
		this.side = side;
	}

}
