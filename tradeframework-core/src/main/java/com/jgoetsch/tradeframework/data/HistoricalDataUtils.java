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

public class HistoricalDataUtils {

	private static long periodDurations[] = {
			1000L,		// 1 sec
			5000L,		// 5 sec
			15000l,		// 15 sec
			30000L,		// 30 sec
			60000L,		// 1 min
			120000L,	// 2 min
			180000L,	// 3 min
			300000L,	// 5 min
			900000L,	// 15 min
			1800000L,	// 30 min
			3600000L,	// 1 hour
			86400000L	// 1 day
		};

	public static long getPeriodDurationInMillis(int period) {
		return periodDurations[period];
	}

}
