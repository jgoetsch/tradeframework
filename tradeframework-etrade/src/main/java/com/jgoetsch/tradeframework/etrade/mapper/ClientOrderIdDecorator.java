package com.jgoetsch.tradeframework.etrade.mapper;

import java.security.SecureRandom;

import com.jgoetsch.tradeframework.Order;
import com.jgoetsch.tradeframework.etrade.dto.PlaceOrderRequest;
import com.jgoetsch.tradeframework.etrade.dto.PreviewOrderRequest;

public abstract class ClientOrderIdDecorator implements OrderMapper {
	private final OrderMapper delegate;
	private final SecureRandom random = new SecureRandom();

	public ClientOrderIdDecorator(OrderMapper delegate) {
		this.delegate = delegate;
	}

	@Override
	public PreviewOrderRequest createPreviewOrderRequest(Order order) {
		return applyClientId(delegate.createPreviewOrderRequest(order));
	}

	@Override
	public PlaceOrderRequest createPlaceOrderRequest(Order order) {
		return applyClientId(delegate.createPlaceOrderRequest(order));
	}

	private <T extends PreviewOrderRequest> T applyClientId(T request) {
		request.clientOrderId = Long.toHexString(Math.abs(random.nextLong()));
		return request;
	}
}
