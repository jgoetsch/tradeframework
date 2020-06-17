package com.jgoetsch.tradeframework.etrade.dto;

import java.util.List;
import java.util.stream.Stream;

import com.google.api.client.util.Key;

public class PlaceOrderResponse extends PreviewOrderResponse {
	@Key("OrderIds") List<OrderId> orderIds;

	public static class OrderId {
		@Key Long orderId;
	}

	public Long getExternalId() {
		return Stream.ofNullable(orderIds).flatMap(List::stream).findFirst()
				.map(p -> p.orderId).orElse(null);
	}
}
