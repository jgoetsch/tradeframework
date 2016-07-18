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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.eventtrader.processor.Processor;
import com.jgoetsch.eventtrader.processor.PropagatingProcessor;
import com.jgoetsch.tradeframework.Contract;

public class PatternTradeExtractor extends PropagatingProcessor<Msg, TradeSignal> {

	Logger log = LoggerFactory.getLogger(PatternTradeExtractor.class);
	private Pattern pattern;
	private String typeFormat;
	private String symbolFormat;

	public void process(Msg msg, Map<Object, Object> context) throws Exception {
		Matcher m = pattern.matcher(msg.getMessage());
		while (m.find()) {
			TradeSignal tradeSignal = new TradeSignal(msg);
			if (typeFormat != null)
				tradeSignal.setType(formatFromGroups(m, typeFormat));
			if (symbolFormat != null)
				tradeSignal.setContract(Contract.stock(formatFromGroups(m, symbolFormat)));
			log.debug("{}", tradeSignal);
			if (getProcessors() != null) {
				for (Processor<TradeSignal> p : getProcessors())
					p.process(tradeSignal, context);
			}
		}
	}

	private static final Pattern p = Pattern.compile("\\$(\\d+)");
	private String formatFromGroups(Matcher inputMatch, String format) {
		Matcher m = p.matcher(format);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, inputMatch.group(Integer.parseInt(m.group(1))));
		}
		m.appendTail(sb);
		return sb.toString();
	}
	public Pattern getPattern() {
		return pattern;
	}
	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}
	public String getTypeFormat() {
		return typeFormat;
	}
	public void setTypeFormat(String typeFormat) {
		this.typeFormat = typeFormat;
	}
	public String getSymbolFormat() {
		return symbolFormat;
	}
	public void setSymbolFormat(String symbolFormat) {
		this.symbolFormat = symbolFormat;
	}

}
