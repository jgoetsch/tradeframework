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

public class PresetPosition implements Position {

	private double avgPrice;
	private double marketPrice;
	private int quantity;
	private double value;
	private double unrealizedPNL;
	private double realizedPNL;

	public PresetPosition(int position, double marketPrice, double marketValue, double averageCost, double unrealizedPNL, double realizedPNL)
	{
		this.update(position, marketPrice, marketValue, averageCost, unrealizedPNL, realizedPNL);
	}
	
	public void update(int position, double marketPrice, double marketValue, double averageCost, double unrealizedPNL, double realizedPNL)
	{
		this.quantity = position;
		this.marketPrice = marketPrice;
		this.value = marketValue;
		this.avgPrice = averageCost;
		this.unrealizedPNL = unrealizedPNL;
		this.realizedPNL = realizedPNL;
	}
	
	public double getAvgPrice() {
		return avgPrice;
	}

	public double getMarketPrice() {
		return marketPrice;
	}

	public int getQuantity() {
		return quantity;
	}

	public double getRealizedProfitLoss() {
		return realizedPNL;
	}

	public double getUnrealizedProfitLoss() {
		return unrealizedPNL;
	}

	public double getValue() {
		return value;
	}

}
