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

import java.io.Closeable;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

import com.jgoetsch.tradeframework.InvalidContractException;


public abstract class SimulatedMarketDataFeed implements Closeable {

	private MarketData lastTick;
	private MarketData nextTick;

	public final MarketData getLastTick() {
		return lastTick;
	}
	
	protected final void setLastTick(MarketData lastTick) {
		this.lastTick = lastTick;
	}

	protected final void reset() {
		this.nextTick = null;
	}

	public final List<MarketData> getTicksUpTo(Instant timestamp) throws IOException, InvalidContractException {
		List<MarketData> ticks = new LinkedList<MarketData>();
		if (nextTick == null)
			nextTick = retrieveNextTick();
		while (true) {
			if (nextTick == null)
				return null;
			else if (nextTick.getTimestamp().isAfter(timestamp))
				return ticks;
			else {
				lastTick = nextTick;
				ticks.add(lastTick);
				nextTick = retrieveNextTick();
			}
		}
	}

	public final MarketData nextTick() throws IOException, InvalidContractException {
		if (nextTick != null) {
			lastTick = nextTick;
			nextTick = null;
		}
		else
			lastTick = retrieveNextTick();
		return lastTick;
	}

	public void advanceTo(Instant timestamp) throws IOException, InvalidContractException {
		if (nextTick == null)
			nextTick = retrieveNextTick();
		while (nextTick != null && !nextTick.getTimestamp().isBefore(timestamp)) {
			lastTick = nextTick;
			nextTick = retrieveNextTick();
		}
	}

	protected abstract MarketData retrieveNextTick() throws IOException, InvalidContractException;

}
