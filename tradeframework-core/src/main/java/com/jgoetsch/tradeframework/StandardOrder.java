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

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class StandardOrder implements Order {

	private Contract contract;
	private OrderType type;
	private BigDecimal quantity;
	private TimeInForce timeInForce;
	private boolean allowOutsideRth;
	private boolean shortFlag;
	private BigDecimal limitPrice;
	private BigDecimal auxPrice;
	private BigDecimal trailStopPrice;

	private String account;
	private String previewId;

	private final Set<String> tags = new HashSet<String>();

	public StandardOrder() {
		this.setTimeInForce(TimeInForce.DAY);
		this.setQuantity(BigDecimal.ZERO);
	}

	public StandardOrder(Order other) {
		this.type = other.getType();
		this.quantity = other.getQuantity();
		this.timeInForce = other.getTimeInForce();
		this.allowOutsideRth = other.getAllowOutsideRth();
		this.limitPrice = other.getLimitPrice();
		this.auxPrice = other.getAuxPrice();
		this.trailStopPrice = other.getTrailStopPrice();
		this.setAccount(other.getAccount());
		this.previewId = other.getPreviewId();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getQuantity().signum() > 0 ? "BUY " : "SELL ").append(getQuantity().abs()).append(" ");
		if (getContract() != null)
			sb.append(getContract()).append(" ");
		sb.append(getType());
		if (getLimitPrice() != null)
			sb.append(" @ ").append(getLimitPrice());
		if (!TimeInForce.DAY.equals(getTimeInForce()))
			sb.append(" ").append(getTimeInForce());
		if (getPreviewId() != null) {
			sb.append(" (previewId=").append(getPreviewId()).append(")");
		}
		return sb.toString();
	}

	public Contract getContract() {
		return contract;
	}

	public void setContract(Contract contract) {
		this.contract = contract;
	}

	public void setType(OrderType type) {
		this.type = type;
	}

	@Override
	public OrderType getType() {
		return type;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	@Override
	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setLimitPrice(BigDecimal limitPrice) {
		this.limitPrice = limitPrice;
	}

	@Override
	public BigDecimal getLimitPrice() {
		return limitPrice;
	}

	public void setAuxPrice(BigDecimal auxPrice) {
		this.auxPrice = auxPrice;
	}

	@Override
	public BigDecimal getAuxPrice() {
		return auxPrice;
	}

	public void setTrailStopPrice(BigDecimal trailStopPrice) {
		this.trailStopPrice = trailStopPrice;
	}

	@Override
	public BigDecimal getTrailStopPrice() {
		return trailStopPrice;
	}

	public void setTimeInForce(TimeInForce timeInForce) {
		this.timeInForce = timeInForce;
	}

	@Override
	public TimeInForce getTimeInForce() {
		return timeInForce;
	}

	public void setAllowOutsideRth(boolean allowOutsideRth) {
		this.allowOutsideRth = allowOutsideRth;
	}

	@Override
	public boolean getAllowOutsideRth() {
		return allowOutsideRth;
	}

	public void setPreviewId(String previewId) {
		this.previewId = previewId;
	}

	@Override
	public String getPreviewId() {
		return previewId;
	}

	@Override
	public Set<String> getTags() {
		return tags;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	@Override
	public String getAccount() {
		return account;
	}

	@Override
	public boolean isShort() {
		return shortFlag;
	}

	/**
	 * Set to true if the order is to sell short or buy to cover. Necessary for brokers that require specification
	 * between short/cover orders and long buy/sell orders, ignored otherwise.
	 */
	public void setShort(boolean shortFlag) {
		this.shortFlag = shortFlag;
	}
}
