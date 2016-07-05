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

import java.util.HashSet;
import java.util.Set;

public class Order {

	private String type;
	private int quantity;
	private String timeInForce;
	private boolean allowOutsideRth;
	private double limitPrice;
	private double auxPrice;
	private double trailStopPrice;

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

	public Order() {
		this.setTimeInForce(TIF_DAY);
		this.setQuantity(0);
		this.setTransmit(true);
	}

	public Order(Order other) {
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

	public static Order marketOrder(int quantity) {
		Order order = new Order();
		order.setType(TYPE_MARKET);
		order.setQuantity(quantity);
		return order;
	}

	public static Order limitOrder(int quantity, double limitPrice) {
		Order order = marketOrder(quantity);
		order.setType(TYPE_LIMIT);
		order.setLimitPrice(limitPrice);
		return order;
	}

	public static Order trailingStopOrder(int quantity, double stopPrice, double trailAmount) {
		Order order = marketOrder(quantity);
		order.setType(TYPE_TRAIL);
		order.setTrailStopPrice(stopPrice);
		order.setAuxPrice(trailAmount);
		return order;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getQuantity() > 0 ? "BUY " : "SELL ").append(Math.abs(getQuantity())).append(" ");
		sb.append(getType());
		if (getLimitPrice() > 0)
			sb.append(" @ ").append(getLimitPrice());
		if (!TIF_DAY.equals(getTimeInForce()))
			sb.append(" ").append(getTimeInForce());
		if (!isTransmit())
			sb.append(" NO TRANSMIT");
		return sb.toString();
	}

	/**
	 * Assure that an incoming double value is rounded to an allowable number of
	 * decimal places.
	 */
	protected static double normalizeDouble(double value) {
		return Math.rint(value * 10000) / 10000;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setLimitPrice(double limitPrice) {
		this.limitPrice = normalizeDouble(limitPrice);
	}

	public double getLimitPrice() {
		return limitPrice;
	}

	public void setAuxPrice(double auxPrice) {
		this.auxPrice = normalizeDouble(auxPrice);
	}

	public double getAuxPrice() {
		return auxPrice;
	}

	public void setTrailStopPrice(double trailStopPrice) {
		this.trailStopPrice = normalizeDouble(trailStopPrice);
	}

	public double getTrailStopPrice() {
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
