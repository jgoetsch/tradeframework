package com.jgoetsch.tradeframework.etrade.dto;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.google.api.client.util.Key;

public class PreviewOrderRequest {
	@Key public OrderDetail.Type orderType;
	@Key public String clientOrderId;

	@Key("Order") List<OrderDetail> order;

	public OrderDetail getOrder() {
		return Stream.ofNullable(order).flatMap(List::stream).findFirst().orElse(new OrderDetail());
	}

	public void setOrder(OrderDetail orderDetail) {
		order = Collections.singletonList(orderDetail);
	}
}
