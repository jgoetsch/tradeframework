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
package com.jgoetsch.tradeframework;



public class OHLCUtils {

	public static double range(OHLC ohlc) {
		return ohlc.getHigh() - ohlc.getLow();
	}
	
	public static double change(OHLC ohlc) {
		return ohlc.getClose() - ohlc.getOpen();
	}

	/**
	 * Returns the average high to low range as a percentage of the closing price over the
	 * given periods.
	 */
	public static double averageRange(OHLC prices[]) {
		if (prices == null)
			return 0;
		else {
			double total = 0;
			for (int i=0; i < prices.length; i++) {
				total += range(prices[i]) / prices[i].getClose();
			}
			return total / prices.length;
		}
	}
	
	public static OHLC aggregate(OHLC ohlcs[]) {
		if (ohlcs == null || ohlcs.length == 0)
			return null;
		else {
			SimpleOHLC aggr = new SimpleOHLC();
			aggr.setOpen(ohlcs[0].getOpen());
			for (int i=0; i < ohlcs.length; i++) {
				aggr.setHigh(Math.max(aggr.getHigh(), ohlcs[0].getHigh()));
				aggr.setLow(Math.min(aggr.getLow() == 0 ? Double.MAX_VALUE : aggr.getLow(), ohlcs[0].getLow()));
			}
			aggr.setClose(ohlcs[ohlcs.length-1].getClose());
			return aggr;
		}
	}
}
