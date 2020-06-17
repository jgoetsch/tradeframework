package com.jgoetsch.tradeframework.etrade.dto;
import java.util.List;
import java.util.stream.Stream;

import com.google.api.client.util.Key;

public class PreviewOrderResponse extends PreviewOrderRequest {
	@Key public String accountId;
	@Key("PreviewIds") List<PreviewId> previewIds;

	public static class PreviewId {
		@Key Long previewId;
	}

	public Long getExternalId() {
		return Stream.ofNullable(previewIds).flatMap(List::stream).findFirst()
				.map(p -> p.previewId).orElse(null);
	}
}
