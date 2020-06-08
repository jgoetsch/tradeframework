package com.jgoetsch.tradeframework;

import java.math.BigDecimal;
import java.util.Set;

public interface Order {

	String TYPE_MARKET = "MKT";
	String TYPE_LIMIT = "LMT";
	String TYPE_STOP = "STP";
	String TYPE_STOPLIMIT = "STPLMT";
	String TYPE_TRAIL = "TRAIL";
	String TYPE_TRAILLIMIT = "TRAILLIMIT";
	String TIF_DAY = "DAY";
	String TIF_GTC = "GTC";

	String getType();

	BigDecimal getQuantity();

	BigDecimal getLimitPrice();

	BigDecimal getAuxPrice();

	BigDecimal getTrailStopPrice();

	String getTimeInForce();

	boolean getAllowOutsideRth();

	boolean isTransmit();

	Set<String> getTags();

	String getAccount();

	static StandardOrder trailingStopOrder(BigDecimal quantity, BigDecimal stopPrice, BigDecimal trailAmount) {
		StandardOrder order = marketOrder(quantity);
		order.setType(TYPE_TRAIL);
		order.setTrailStopPrice(stopPrice);
		order.setAuxPrice(trailAmount);
		return order;
	}

	static Order limitOrder(BigDecimal quantity, BigDecimal limitPrice) {
		StandardOrder order = marketOrder(quantity);
		order.setType(TYPE_LIMIT);
		order.setLimitPrice(limitPrice);
		return order;
	}

	static StandardOrder marketOrder(BigDecimal quantity) {
		StandardOrder order = new StandardOrder();
		order.setType(TYPE_MARKET);
		order.setQuantity(quantity);
		return order;
	}

}