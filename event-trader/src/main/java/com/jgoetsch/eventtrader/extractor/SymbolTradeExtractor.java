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
import java.util.regex.Pattern;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.TradeSignal;

public class SymbolTradeExtractor extends TradeExtractor {

	private Pattern symbolPattern;

	@Override
	public Collection<TradeSignal> parseTrades(Msg msg) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setSymbolPattern(Pattern symbolPattern) {
		this.symbolPattern = symbolPattern;
	}

	public Pattern getSymbolPattern() {
		return symbolPattern;
	}

}
