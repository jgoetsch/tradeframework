package com.jgoetsch.tradeframework.etrade.mapper;

import java.time.Instant;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.jgoetsch.tradeframework.etrade.dto.QuoteResponse;
import com.jgoetsch.tradeframework.etrade.dto.QuoteResponse.Quote;
import com.jgoetsch.tradeframework.marketdata.SimpleMarketData;

@Mapper
public interface QuoteMapper {
	QuoteMapper INSTANCE = Mappers.getMapper(QuoteMapper.class);

	@Mapping(target = "last", source = "quote.lastTrade")
	@Mapping(target = "volume", source = "quote.totalVolume")
	@Mapping(target = "lastTimestamp", source = "quote.timeOfLastTrade")
	@Mapping(target = "timestamp", source = "response.quoteData.dateTimeUTC")
	@Mapping(target = "close", source = "quote.previousClose")
	@Mapping(target = "lastSize", ignore = true)
	SimpleMarketData marketDataFromQuoteResponse(QuoteResponse response, Quote quote);

	default Instant convertTimestamp(Long timestamp) {
		return Instant.ofEpochSecond(timestamp);
	}
}
