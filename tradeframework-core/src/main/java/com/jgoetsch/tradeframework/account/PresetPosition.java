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
package com.jgoetsch.tradeframework.account;

import java.math.BigDecimal;

public class PresetPosition implements Position {

	private BigDecimal avgPrice;
	private BigDecimal marketPrice;
	private BigDecimal quantity;
	private BigDecimal value;
	private BigDecimal unrealizedPNL;
	private BigDecimal realizedPNL;

	public PresetPosition(BigDecimal position, BigDecimal marketPrice, BigDecimal marketValue, BigDecimal averageCost, BigDecimal unrealizedPNL, BigDecimal realizedPNL)
	{
		this.update(position, marketPrice, marketValue, averageCost, unrealizedPNL, realizedPNL);
	}
	
	public void update(BigDecimal position, BigDecimal marketPrice, BigDecimal marketValue, BigDecimal averageCost, BigDecimal unrealizedPNL, BigDecimal realizedPNL)
	{
		this.quantity = position;
		this.marketPrice = marketPrice;
		this.value = marketValue;
		this.avgPrice = averageCost;
		this.unrealizedPNL = unrealizedPNL;
		this.realizedPNL = realizedPNL;
	}
	
	public BigDecimal getAvgPrice() {
		return avgPrice;
	}

	public BigDecimal getMarketPrice() {
		return marketPrice;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public BigDecimal getRealizedProfitLoss() {
		return realizedPNL;
	}

	public BigDecimal getUnrealizedProfitLoss() {
		return unrealizedPNL;
	}

	public BigDecimal getValue() {
		return value;
	}

	public boolean exists() {
		return quantity.signum() != 0;
	}
}
