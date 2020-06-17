package com.jgoetsch.tradeframework.etrade.dto;

import java.util.Collections;

public class PlaceOrderRequest extends PreviewOrderResponse {
	public void setExternalId(Long previewId) {
		PreviewId pid = new PreviewId();
		pid.previewId = previewId;
		this.previewIds = Collections.singletonList(pid);
	}
}
