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

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class SMA extends Statistic {

	private Queue<Double> samples;
	private double sum;

	/**
	 * Constructs an SMA statistic over the specified period and period unit length
	 * 
	 * @param numPeriods number of time periods to use
	 * @param periodLength length of time period of each sample unit in milliseconds
	 */
	public SMA(int period) {
		samples = new ArrayBlockingQueue<Double>(period);
	}

	public void addSample(double value) {
		if (!samples.offer(value)) {
			sum -= samples.remove();
			samples.add(value);
		}
		sum += value;
	}

	public double getValue() {
		if (samples.isEmpty())
			throw new IllegalStateException("Attempted to get SMA value with no sample data.");
		return sum / samples.size();
	}

	public void clear() {
		samples.clear();
		sum = 0;
	}
}
