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

public class StandardOrder {

	private String type;
	private BigDecimal quantity;
	private String timeInForce;
	private boolean allowOutsideRth;
	private BigDecimal limitPrice;
	private BigDecimal auxPrice;
	private BigDecimal trailStopPrice;

	private String account;
	private boolean bTransmit;

	private final Set<String> tags = new HashSet<String>();

	public static final String TYPE_MARKET = "MKT";
	public static final String TYPE_LIMIT = "LMT";
	public static final String TYPE_STOP = "STP";
	public static final String TYPE_STOPLIMIT = "STPLMT";
	public static final String TYPE_TRAIL = "TRAIL";
	public static final String TYPE_TRAILLIMIT = "TRAILLIMIT";

	public static final String TIF_DAY = "DAY";
	public static final String TIF_GTC = "GTC";

	public StandardOrder() {
		this.setTimeInForce(TIF_DAY);
		this.setQuantity(BigDecimal.ZERO);
		this.setTransmit(true);
	}

	public StandardOrder(StandardOrder other) {
		this.type = other.type;
		this.quantity = other.quantity;
		this.timeInForce = other.timeInForce;
		this.allowOutsideRth = other.allowOutsideRth;
		this.limitPrice = other.limitPrice;
		this.auxPrice = other.auxPrice;
		this.trailStopPrice = other.trailStopPrice;
		this.setAccount(other.getAccount());
		this.bTransmit = other.bTransmit;
	}

	public static StandardOrder marketOrder(BigDecimal quantity) {
		StandardOrder order = new StandardOrder();
		order.setType(TYPE_MARKET);
		order.setQuantity(quantity);
		return order;
	}

	public static StandardOrder limitOrder(BigDecimal quantity, BigDecimal limitPrice) {
		StandardOrder order = marketOrder(quantity);
		order.setType(TYPE_LIMIT);
		order.setLimitPrice(limitPrice);
		return order;
	}

	public static StandardOrder trailingStopOrder(BigDecimal quantity, BigDecimal stopPrice, BigDecimal trailAmount) {
		StandardOrder order = marketOrder(quantity);
		order.setType(TYPE_TRAIL);
		order.setTrailStopPrice(stopPrice);
		order.setAuxPrice(trailAmount);
		return order;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getQuantity().signum() > 0 ? "BUY " : "SELL ").append(getQuantity().abs()).append(" ");
		sb.append(getType());
		if (getLimitPrice() != null)
			sb.append(" @ ").append(getLimitPrice());
		if (!TIF_DAY.equals(getTimeInForce()))
			sb.append(" ").append(getTimeInForce());
		if (!isTransmit())
			sb.append(" NO TRANSMIT");
		return sb.toString();
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setLimitPrice(BigDecimal limitPrice) {
		this.limitPrice = limitPrice;
	}

	public BigDecimal getLimitPrice() {
		return limitPrice;
	}

	public void setAuxPrice(BigDecimal auxPrice) {
		this.auxPrice = auxPrice;
	}

	public BigDecimal getAuxPrice() {
		return auxPrice;
	}

	public void setTrailStopPrice(BigDecimal trailStopPrice) {
		this.trailStopPrice = trailStopPrice;
	}

	public BigDecimal getTrailStopPrice() {
		return trailStopPrice;
	}

	public void setTimeInForce(String timeInForce) {
		this.timeInForce = timeInForce;
	}

	public String getTimeInForce() {
		return timeInForce;
	}

	public void setAllowOutsideRth(boolean allowOutsideRth) {
		this.allowOutsideRth = allowOutsideRth;
	}

	public boolean getAllowOutsideRth() {
		return allowOutsideRth;
	}

	public void setTransmit(boolean bTransmit) {
		this.bTransmit = bTransmit;
	}

	public boolean isTransmit() {
		return bTransmit;
	}

	public Set<String> getTags() {
		return tags;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getAccount() {
		return account;
	}
}
