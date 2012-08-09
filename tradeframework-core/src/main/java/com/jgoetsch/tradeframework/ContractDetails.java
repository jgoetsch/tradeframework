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

/**
 * A contract object with extra detail information that can be requested
 * from a ContractDetailSource.
 * 
 * @author jgoetsch
 *
 */
public class ContractDetails extends Contract {

	public String marketName;
	public String tradingClass;
	public double minTick;
	public int priceMagnifier;
	public String orderTypes;
	public String validExchanges;
	public int underConId;
	public String longName;

	public ContractDetails() {
		super();
	}

	public ContractDetails(Contract other) {
		super(other);
	}

	public String getMarketName() {
		return marketName;
	}

	public void setMarketName(String marketName) {
		this.marketName = marketName;
	}

	public String getTradingClass() {
		return tradingClass;
	}

	public void setTradingClass(String tradingClass) {
		this.tradingClass = tradingClass;
	}

	public double getMinTick() {
		return minTick;
	}

	public void setMinTick(double minTick) {
		this.minTick = minTick;
	}

	public int getPriceMagnifier() {
		return priceMagnifier;
	}

	public void setPriceMagnifier(int priceMagnifier) {
		this.priceMagnifier = priceMagnifier;
	}

	public String getOrderTypes() {
		return orderTypes;
	}

	public void setOrderTypes(String orderTypes) {
		this.orderTypes = orderTypes;
	}

	public String getValidExchanges() {
		return validExchanges;
	}

	public void setValidExchanges(String validExchanges) {
		this.validExchanges = validExchanges;
	}

	public int getUnderConId() {
		return underConId;
	}

	public void setUnderConId(int underConId) {
		this.underConId = underConId;
	}

	public String getLongName() {
		return longName;
	}

	public void setLongName(String longName) {
		this.longName = longName;
	}

}
