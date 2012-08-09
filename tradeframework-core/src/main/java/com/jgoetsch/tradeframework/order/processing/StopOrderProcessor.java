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
package com.jgoetsch.tradeframework.order.processing;

import java.text.NumberFormat;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.tradeframework.Execution;
import com.jgoetsch.tradeframework.marketdata.MarketData;

public class StopOrderProcessor extends OrderProcessor {

	private Logger log = LoggerFactory.getLogger(StopOrderProcessor.class);
	private double stopPrice;
	private OrderProcessor triggeredOrder;
	private final TriggerMethod triggerMethod;

	public StopOrderProcessor(int quantity, double stopPrice) {
		super(quantity);
		this.stopPrice = stopPrice;
		this.triggerMethod = new LastTriggerMethod();
	}

	public StopOrderProcessor(int quantity, double stopPrice, TriggerMethod triggerMethod) {
		super(quantity);
		this.stopPrice = stopPrice;
		this.triggerMethod = triggerMethod;
	}

	public interface TriggerMethod {
		public boolean isTriggered(MarketData marketData, boolean isSell, double stopPrice);
	}

	/**
	 * Basic method that triggers when last price passes stop amount.
	 */
	public static class LastTriggerMethod implements TriggerMethod {
		public boolean isTriggered(MarketData marketData, boolean isSell, double stopPrice) {
			return ((isSell && marketData.getLast() < stopPrice) || (!isSell && marketData.getLast() > stopPrice));
		}
	}

	/**
	 * Simulated stop that will always trigger before the end of day if it has not yet triggered.
	 */
	public static class EODTriggerMethod implements TriggerMethod {
		private TriggerMethod baseTriggerMethod;
		public EODTriggerMethod() {
			this.baseTriggerMethod = new LastTriggerMethod();
		}
		public EODTriggerMethod(TriggerMethod baseTriggerMethod) {
			this.baseTriggerMethod = baseTriggerMethod;
		}
		public boolean isTriggered(MarketData marketData, boolean isSell, double stopPrice) {
			if (new LocalTime(marketData.getTimestamp(), DateTimeZone.forID("America/New_York")).compareTo(new LocalTime(15, 45)) >= 0)
				return true;
			else
				return baseTriggerMethod.isTriggered(marketData, isSell, stopPrice);
		}
	}

	@Override
	protected final Execution handleProcessing(MarketData marketData) {
		if (triggeredOrder != null)
			return triggeredOrder.process(marketData);
		else {
			onNotTriggered(marketData);
			if (triggerMethod.isTriggered(marketData, isSelling(), getStopPrice())) {
				triggeredOrder = onTriggered(marketData);
				return triggeredOrder.process(marketData);
			}
			else
				return null;
		}
	}

	protected void onNotTriggered(MarketData marketData) {
	}
	
	protected OrderProcessor onTriggered(MarketData marketData) {
		log.debug("Stop order @" + NumberFormat.getNumberInstance().format(stopPrice) + " triggered " + marketData);
		return new MarketOrderProcessor(getQuantityRemaining());
	}

	protected double getStopPrice() {
		return stopPrice;
	}
	
	protected void setStopPrice(double stopPrice) {
		this.stopPrice = stopPrice;
	}

}
