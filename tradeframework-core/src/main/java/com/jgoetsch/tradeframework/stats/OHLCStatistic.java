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
package com.jgoetsch.tradeframework.stats;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.jgoetsch.tradeframework.OHLC;

/**
 * Convenience class to track open, high, low, and close values
 * @author jgoetsch
 */
public class OHLCStatistic implements OHLC {

	private Statistic open = new Open();
	private Statistic high = new High();
	private Statistic low = new Low();
	private Statistic close = new Close();
	private int numSamples;
	private Date start;
	
	public OHLCStatistic() {
	}
	
	public OHLCStatistic(Date start) {
		this.start = new Date(start.getTime());
	}

	public void addSample(double value) {
		open.addSample(value);
		high.addSample(value);
		low.addSample(value);
		close.addSample(value);
		numSamples++;
	}
	
	public void clear() {
		open.clear();
		high.clear();
		low.clear();
		close.clear();
		numSamples = 0;
	}
	
	public int getCount() {
		return numSamples;
	}

	public double getValue() {
		return getClose();
	}

	public double getOpen() {
		return open.getValue();
	}
	
	public double getHigh() {
		return high.getValue();
	}
	
	public double getLow() {
		return low.getValue();
	}
	
	public double getClose() {
		return close.getValue();
	}

	public Date getDate() {
		return start;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (getDate() != null) {
			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG);
			df.setTimeZone(TimeZone.getTimeZone("America/New_York"));
			sb.append(df.format(getDate())).append(' ');
		}
		sb.append("O=").append(getOpen());
		sb.append(" H=").append(getHigh());
		sb.append(" L=").append(getLow());
		sb.append(" C=").append(getClose());
		return sb.toString();
	}

	public long getVolume() {
		return 0;
	}
}
