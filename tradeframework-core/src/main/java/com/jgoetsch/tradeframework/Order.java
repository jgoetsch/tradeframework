package com.jgoetsch.tradeframework;

import java.math.BigDecimal;
import java.util.Set;

public interface Order {

	public enum OrderType {
		MKT,
		LMT,
		STP,
		STP_LMT,
		TRAIL,
		TRAIL_LIMIT;
	}
	public enum TimeInForce {
		DAY,
		GTC,
		GOOD_TILL_DATE,
		IMMEDIATE_OR_CANCEL,
		FILL_OR_KILL
	}

	Contract getContract();

	OrderType getType();

	BigDecimal getQuantity();

	BigDecimal getLimitPrice();

	BigDecimal getAuxPrice();

	BigDecimal getTrailStopPrice();

	TimeInForce getTimeInForce();

	boolean getAllowOutsideRth();

	/**
	 * Return true if the order is to sell short or buy to cover. Necessary for brokers that require specification
	 * between short/cover orders and long buy/sell orders, ignored otherwise.
	 */
	boolean isShort();

	Set<String> getTags();

	String getAccount();

	String getExternalId();

	static StandardOrder trailingStopOrder(Contract contract, BigDecimal quantity, BigDecimal stopPrice, BigDecimal trailAmount) {
		StandardOrder order = marketOrder(contract, quantity);
		order.setType(OrderType.TRAIL);
		order.setTrailStopPrice(stopPrice);
		order.setAuxPrice(trailAmount);
		return order;
	}

	static StandardOrder limitOrder(Contract contract, BigDecimal quantity, BigDecimal limitPrice) {
		StandardOrder order = marketOrder(contract, quantity);
		order.setType(OrderType.LMT);
		order.setLimitPrice(limitPrice);
		return order;
	}

	static StandardOrder limitOrder(Contract contract, BigDecimal quantity, BigDecimal limitPrice, String account) {
		StandardOrder order = limitOrder(contract, quantity, limitPrice);
		order.setAccount(account);
		return order;
	}

	static StandardOrder marketOrder(Contract contract, BigDecimal quantity) {
		StandardOrder order = new StandardOrder();
		order.setContract(contract);
		order.setType(OrderType.MKT);
		order.setQuantity(quantity);
		return order;
	}

}