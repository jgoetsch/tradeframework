package com.jgoetsch.tradeframework.etrade.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import com.google.api.client.util.Key;

public class QuoteResponse {
	public static class QuoteData {
		@Key public Long dateTimeUTC;
		@Key public Quote Intraday;
		@Key public Quote All;
	}
	public static class Quote {
		@Key public BigDecimal bid;
		@Key public Integer bidSize;
		@Key public BigDecimal ask;
		@Key public Integer askSize;
		@Key public BigDecimal lastTrade;
		@Key public BigDecimal high;
		@Key public BigDecimal low;
		@Key public BigDecimal previousClose;
		@Key public Integer totalVolume;
		@Key public long timeOfLastTrade;
	}

	@Key List<QuoteData> QuoteData;
	@Key("Messages") public MessageList messages;

	public QuoteData getQuoteData() {
		return Stream.ofNullable(QuoteData).flatMap(List::stream).findFirst().orElse(null);
	}
}
