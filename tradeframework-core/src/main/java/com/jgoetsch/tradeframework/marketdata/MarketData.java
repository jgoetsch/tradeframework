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

import java.math.BigDecimal;
import java.time.Instant;

public interface MarketData {

	public BigDecimal getBid();

	public Integer getBidSize();

	public BigDecimal getAsk();

	public Integer getAskSize();

	public BigDecimal getLast();

	public Integer getLastSize();

	public BigDecimal getHigh();

	public BigDecimal getLow();

	public Integer getVolume();

	public BigDecimal getClose();
	
	public Instant getLastTimestamp();

	public Instant getTimestamp();
}
