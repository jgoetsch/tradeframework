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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.tradeframework.OHLC;
import com.jgoetsch.tradeframework.data.HistoricalDataSource;
import com.jgoetsch.tradeframework.marketdata.MarketData;

public class HistoricalAveragePrice extends OffsetOrderPrice {

	private Logger log = LoggerFactory.getLogger(HistoricalAveragePrice.class);
	private HistoricalDataSource historicalDataSource;
	private int durationMinutes;

	@Override
	protected BigDecimal getBaseValue(TradeSignal trade, Supplier<MarketData> marketData) {
		try {
			OHLC data[] = historicalDataSource.getHistoricalData(trade.getContract(), new Date(), durationMinutes * 4, HistoricalDataSource.PERIOD_15_SECONDS);
			return fromDouble(Arrays.asList(data).stream().mapToDouble(ohlc -> (ohlc.getHigh() + ohlc.getLow()) / 2).average().getAsDouble());
		} catch (Exception e) {
			log.warn("Could not get historical data for average trade limit price, using Last price", e);
			return fromDouble(marketData.get().getLast());
		}
	}

	public void setDurationMinutes(int durationMinutes) {
		this.durationMinutes = durationMinutes;
	}

	public int getDurationMinutes() {
		return durationMinutes;
	}

}
