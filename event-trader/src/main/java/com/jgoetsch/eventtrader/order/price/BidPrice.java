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
package com.jgoetsch.eventtrader.order.price;

import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.tradeframework.marketdata.MarketData;

/**
 * OrderPrice evaluator returning the current bid price if buying, or ask price
 * if selling.
 * 
 * @author jgoetsch
 * 
 */
public class BidPrice extends OffsetOrderPrice {

	@Override
	protected Double getBaseValue(TradeSignal trade, MarketData marketData) {
		return trade.getType().isBuy() ? marketData.getBid() : marketData.getAsk();
	}

}
