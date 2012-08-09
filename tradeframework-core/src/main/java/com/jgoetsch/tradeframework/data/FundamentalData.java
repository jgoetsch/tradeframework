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
package com.jgoetsch.tradeframework.data;

import java.io.Serializable;

public class FundamentalData implements Serializable {

	private static final long serialVersionUID = 1L;
	private long volume;
	private long avgVolume;
	private double marketCap;
	private double peRatio;

	public long getVolume() {
		return volume;
	}
	public void setVolume(long volume) {
		this.volume = volume;
	}
	public long getAvgVolume() {
		return avgVolume;
	}
	public void setAvgVolume(long avgVolume) {
		this.avgVolume = avgVolume;
	}
	public double getMarketCap() {
		return marketCap;
	}
	public void setMarketCap(double marketCap) {
		this.marketCap = marketCap;
	}
	public double getPeRatio() {
		return peRatio;
	}
	public void setPeRatio(double peRatio) {
		this.peRatio = peRatio;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Volume: ").append(getVolume());
		sb.append(", Avg Vol: ").append(getAvgVolume());
		sb.append(", Mkt Cap: ").append(getMarketCap()).append(" M");
		sb.append(", P/E ratio: ").append(getPeRatio());
		return sb.toString();
	}
}
