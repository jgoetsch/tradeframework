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
	public double minTick;
	public int priceMagnifier;
	public String orderTypes;
	public String validExchanges;
	public int underConid;
	public String longName;
	public String industry;
	public String category;
	public String subcategory;

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

	public int getUnderConid() {
		return underConid;
	}

	public void setUnderConid(int underConId) {
		this.underConid = underConId;
	}

	public String getLongName() {
		return longName;
	}

	public void setLongName(String longName) {
		this.longName = longName;
	}

	public String getIndustry() {
		return industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getSubcategory() {
		return subcategory;
	}

	public void setSubcategory(String subcategory) {
		this.subcategory = subcategory;
	}

}
