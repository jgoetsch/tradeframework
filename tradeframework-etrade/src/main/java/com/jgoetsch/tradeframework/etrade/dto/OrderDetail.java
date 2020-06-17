package com.jgoetsch.tradeframework.etrade.dto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.google.api.client.util.Key;
import com.google.api.client.util.Value;

public class OrderDetail {

	public enum Type {
		@Value EQ, @Value OPTN
	}

	public enum OrderTerm {
		@Value GOOD_UNTIL_CANCEL, @Value GOOD_FOR_DAY,
		@Value GOOD_TILL_DATE, @Value IMMEDIATE_OR_CANCEL, @Value FILL_OR_KILL
	}

	public enum PriceType {
		@Value MARKET, @Value LIMIT, @Value STOP, @Value STOP_LIMIT,
		@Value TRAILING_STOP_CNST, @Value INVALID
	}

	public enum MarketSession {
		@Value REGULAR, @Value EXTENDED
	}

	public static class Instrument {
		public static class Product {
			@Key public String symbol;
			@Key public Type securityType;
		}
		public enum OrderAction {
			@Value SELL, @Value SELL_SHORT, @Value BUY, @Value BUY_TO_COVER
		}
		public enum QuantityType {
			@Value QUANTITY, @Value DOLLAR, @Value ALL_I_OWN
		}
		@Key("Product") public Product product;
		@Key public OrderAction orderAction;
		@Key public QuantityType quantityType = QuantityType.QUANTITY;
		@Key public Integer quantity;
	}

	@Key public OrderDetail.OrderTerm orderTerm;
	@Key public OrderDetail.PriceType priceType;
	@Key public BigDecimal limitPrice;
	@Key public BigDecimal stopPrice;
	@Key public BigDecimal stopLimitPrice;
	@Key public BigDecimal trailPrice;
	@Key public OrderDetail.MarketSession marketSession;

	@Key("Instrument") List<Instrument> instrument;
	@Key public MessageList messages;

	public Instrument getInstrument() {
		return Stream.ofNullable(instrument).flatMap(List::stream).findFirst().orElse(null);
	}

	public void setInstrument(Instrument instrument) {
		this.instrument = Collections.singletonList(instrument);
	}
}