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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Contract {

	private SecurityType type;
	private String symbol;
	private String expiry;
	private String exchange;
	private Long multiplier;

	private String currency;

	public enum SecurityType {
		STOCK, FUTURES, OPTIONS
	}
	public static final String SMART = "SMART";
	public static final String USD = "USD";

	public Contract() {
		this.type = SecurityType.STOCK;
		this.exchange = SMART;
		this.currency = USD;
	}

	private Contract(String symbol) {
		this();
		this.symbol = symbol;
	}

	public Contract(Contract other) {
		this.type = other.type;
		this.symbol = other.symbol;
		this.expiry = other.expiry;
		this.exchange = other.exchange;
		this.multiplier = other.multiplier;
		this.currency = other.currency;
	}

	public static Contract stock(String symbol) {
		return new Contract(symbol);
	}

	public static Contract futures(String symbol, String expiry, String exchange) {
		return futures(symbol, expiry, exchange, 1);
	}

	public static Contract futures(String symbol, String expiry, String exchange, long multiplier) {
		Contract contract = new Contract(symbol);
		contract.type = SecurityType.FUTURES;
		contract.expiry = expiry;
		contract.exchange = exchange;
		contract.multiplier = multiplier;
		return contract;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getSymbol());
		//sb.append(' ').append(contract.m_secType);
		if (getExpiry() != null) {
			try {
				Date expDate = new SimpleDateFormat("yyyyMMdd").parse(getExpiry());
				sb.append(' ').append(new SimpleDateFormat("MMM yy").format(expDate));
			} catch (ParseException e) {
				sb.append(' ').append(getExpiry());
			}
		}
		if (getExchange() != null && !SMART.equals(getExchange()))
			sb.append(" (").append(getExchange()).append(')');
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((expiry == null) ? 0 : expiry.hashCode());
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Contract other = (Contract) obj;
		if (expiry == null) {
			if (other.expiry != null)
				return false;
		} else if (!expiry.equals(other.expiry))
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	public SecurityType getType() {
		return type;
	}

	public void setType(SecurityType type) {
		this.type = type;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getExpiry() {
		return expiry;
	}

	public void setExpiry(String expiry) {
		this.expiry = expiry;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public Long getMultiplier() {
		return multiplier;
	}

	public void setMultiplier(Long multiplier) {
		this.multiplier = multiplier;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

}
