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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.eventtrader.processor.Processor;
import com.jgoetsch.eventtrader.processor.PropagatingProcessor;

public class TradeExtractor extends PropagatingProcessor<Msg, TradeSignal> {

	Logger log = LoggerFactory.getLogger(TradeExtractor.class);
	private Collection<Processor<Msg>> nonTradeProcessors;

	public void process(Msg msg, Map<Object, Object> context) throws Exception {
		Collection<TradeSignal> trades = parseTrades(msg);
		if (trades != null && !trades.isEmpty()) {
			log.debug("{}", msg);
			for (TradeSignal trade : trades) {
				if (getProcessors() != null) {
					for (Processor<TradeSignal> p : getProcessors())
						p.process(trade, context);
				}
			}
		}
		else if (nonTradeProcessors != null) {
			for (Processor<Msg> p : nonTradeProcessors)
				p.process(msg, context);
		}
	}

	public Collection<TradeSignal> parseTrades(Msg msg) {
		if (msg instanceof TradeSignal)
			return Collections.singletonList((TradeSignal)msg);
		else
			return null;
	}

	public void setNonTradeProcessors(Collection<Processor<Msg>> nonTradeProcessors) {
		this.nonTradeProcessors = nonTradeProcessors;
	}

	public Collection<Processor<Msg>> getNonTradeProcessors() {
		return nonTradeProcessors;
	}

	public final void setNonTradeProcessor(Processor<Msg> processor) {
		this.nonTradeProcessors = Collections.singletonList(processor);
	}

	public final Processor<Msg> getNonTradeProcessor() {
		return nonTradeProcessors == null || nonTradeProcessors.size() == 0 ? null : nonTradeProcessors.iterator().next();
	}

}