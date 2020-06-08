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

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.tradeframework.Execution;
import com.jgoetsch.tradeframework.marketdata.MarketData;

public class StopOrderProcessor extends OrderProcessor {

	private Logger log = LoggerFactory.getLogger(StopOrderProcessor.class);
	private BigDecimal stopPrice;
	private OrderProcessor triggeredOrder;
	private final TriggerMethod triggerMethod;

	public StopOrderProcessor(BigDecimal quantity, BigDecimal stopPrice) {
		super(quantity);
		this.stopPrice = stopPrice;
		this.triggerMethod = new LastTriggerMethod();
	}

	public StopOrderProcessor(BigDecimal quantity, BigDecimal stopPrice, TriggerMethod triggerMethod) {
		super(quantity);
		this.stopPrice = stopPrice;
		this.triggerMethod = triggerMethod;
	}

	public interface TriggerMethod {
		public boolean isTriggered(MarketData marketData, boolean isSell, BigDecimal stopPrice);
	}

	/**
	 * Basic method that triggers when last price passes stop amount.
	 */
	public static class LastTriggerMethod implements TriggerMethod {
		public boolean isTriggered(MarketData marketData, boolean isSell, BigDecimal stopPrice) {
			return ((isSell && marketData.getLast().compareTo(stopPrice) < 0) || (!isSell && marketData.getLast().compareTo(stopPrice) > 0));
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
		public boolean isTriggered(MarketData marketData, boolean isSell, BigDecimal stopPrice) {
			if (!LocalTime.of(15, 45).isAfter(LocalDateTime.ofInstant(marketData.getTimestamp(), ZoneId.of("America/New_York")).toLocalTime()))
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

	protected BigDecimal getStopPrice() {
		return stopPrice;
	}
	
	protected void setStopPrice(BigDecimal stopPrice) {
		this.stopPrice = stopPrice;
	}

}
