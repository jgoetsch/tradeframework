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
package com.jgoetsch.tradeframework.marketdata;

import java.util.Comparator;

public class MarketDataTimestampComparator implements Comparator<MarketData> {

	public int compare(MarketData o1, MarketData o2) {
		if (o1.getTimestamp().equals(o2.getTimestamp()))
			return o1.hashCode() - o2.hashCode();
		else
			return o1.getTimestamp().compareTo(o2.getTimestamp());
	}

}
