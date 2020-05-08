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
package com.jgoetsch.eventtrader;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import com.jgoetsch.tradeframework.Contract;

/**
 * A {@link com.jgoetsch.eventtrader.Msg Msg} from which a signal to
 * buy or sell some instrument has been parsed.
 * 
 * @author jgoetsch
 *
 */
public class TradeSignal extends Msg {

	private static final long serialVersionUID = 1L;

	private TradeType type;
	private int numShares;
	private double price;
	private Contract contract;
	private boolean partial;

	public TradeSignal(Msg msg) {
		super(msg);
		if (msg instanceof TradeSignal) {
			TradeSignal other = (TradeSignal) msg;
			this.type = other.type;
			this.numShares = other.numShares;
			this.price = other.price;
			this.contract = other.contract;
			this.partial = other.partial;
		}
	}

	public TradeSignal() {
	}

	/**
	 * Construct a TradeSignal for the given side and symbol.
	 * 
	 * @param side String &quot;BUY&quot; or &quot;SELL&quot;.
	 * @param symbol Ticker symbol.
	 * @param msg The ChatMsg that this TradeSignal was derived from for reference.
	 */
	public TradeSignal(TradeType side, Contract contract, Msg msg) {
		super(msg);
		this.type = side;
		this.contract = contract;
	}

	public TradeSignal(TradeType side, String symbol, Msg msg) {
		super(msg);
		this.type = side;
		this.contract = Contract.stock(symbol);
	}

	public TradeSignal(TradeType type, Contract contract, int numShares, double price) {
		this(type, contract, numShares, price, null);
	}

	public TradeSignal(TradeType type, Contract contract, int numShares, double price, Msg msg) {
		super(msg);
		this.type = type;
		this.contract = contract;
		this.numShares = numShares;
		this.price = price;
	}

	/**
	 * Creates a list of TradeSignal objects of the given side from a whitespace
	 * delimited list of symbols.
	 * 
	 * @param side String &quot;BUY&quot; or &quot;SELL&quot;.
	 * @param symbols Whitespace delimited symbol string.
	 * @param msg The ChatMsg that this TradeSignal was derived from for reference.
	 * @return List of TradeSignal objects.
	 */
	public static List<TradeSignal> fromList(TradeType side, String symbols, Msg msg) {
		List<TradeSignal> list = new ArrayList<TradeSignal>();
		for (String symbol : symbols.split("\\s+")) {
			list.add(new TradeSignal(side, Contract.stock(symbol), msg));
		}
		return list;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		TradeSignal other = (TradeSignal) obj;
		if (getType() == null || !getType().equals(other.getType()))
			return false;
		if (getNumShares() != other.getNumShares())
			return false;
		if (getPrice() != other.getPrice())
			return false;
		if (getContract() == null || !getContract().equals(other.getContract()))
			return false;

		return true;
	}
	
	public String toString() {
		return getTradeString() + ": " + super.toString();
	}

	public String getTradeString() {
		StringBuilder sb = new StringBuilder();
		if (getType() != null)
			sb.append(getType().getDisplayString());
		if (getNumShares() != 0)
			sb.append(" ").append(new DecimalFormat("#,###").format(getNumShares()));
		if (getContract() != null)
			sb.append(" ").append(getContract());
		if (getPrice() != 0)
			sb.append(" at ").append(DecimalFormat.getCurrencyInstance().format(getPrice()));
		return sb.toString();
	}

	public TradeType getType() {
		return type;
	}

	public void setType(TradeType type) {
		this.type = type;
	}

	public int getNumShares() {
		return numShares;
	}

	public void setNumShares(int numShares) {
		this.numShares = numShares;
	}

	public Contract getContract() {
		return contract;
	}

	public void setContract(Contract contract) {
		this.contract = contract;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getPrice() {
		return price;
	}

	public boolean isPartial() {
		return partial;
	}

	public void setPartial(boolean isPartial) {
		this.partial = isPartial;
	}

}
