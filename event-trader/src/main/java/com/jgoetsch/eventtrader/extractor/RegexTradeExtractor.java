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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.TradeSignal;

public class RegexTradeExtractor extends TradeExtractor {

	private String side;
	private Pattern matchPattern;
	private Pattern excludePattern;
	private int symbolsGroupIndex;

	/**
	 * @param matchPattern
	 * @param noMatchPattern
	 */
	public RegexTradeExtractor(String side, String matchRegex, int symbolsGroupIndex) {
		this.side = side;
		this.matchPattern = Pattern.compile(matchRegex);
		this.symbolsGroupIndex = symbolsGroupIndex;
	}

	/**
	 * @param matchPattern
	 * @param noMatchPattern
	 */
	public RegexTradeExtractor(String side, String matchRegex, String excludeRegex, int symbolsGroupIndex) {
		this.side = side;
		this.matchPattern = Pattern.compile(matchRegex);
		this.excludePattern = excludeRegex == null ? null : Pattern.compile(excludeRegex);
		this.symbolsGroupIndex = symbolsGroupIndex;
	}

	public Collection<TradeSignal> parseTrades(Msg msg)
	{
		Matcher match = matchPattern.matcher(msg.getMessage());

		if (match.find() && (excludePattern == null || !excludePattern.matcher(match.group()).find())) {
			return TradeSignal.fromList(side, match.group(symbolsGroupIndex), msg);
		}
		else
			return null;
	}

}
