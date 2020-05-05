package com.jgoetsch.eventtrader.source.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.eventtrader.TradeType;
import com.jgoetsch.eventtrader.source.MsgHandler;
import com.jgoetsch.tradeframework.Contract;

public class PdJsonMsgParser implements MsgParser {

	private Logger log = LoggerFactory.getLogger(getClass());

	private static class Root {
		String command;
		BaseMessage<?> message;

		public Msg mapToMsg() {
			Msg msg = message.mapToMsg();
			msg.setSourceType(command);
			return msg;
		}
	}

	@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
	@JsonSubTypes({
	        @JsonSubTypes.Type(value = Commentary.class, name = "DingMessage"),
	        @JsonSubTypes.Type(value = Trade.class, name = "EntryDingMessage"),
	        @JsonSubTypes.Type(value = PartialTrade.class, name = "PartialEntryDingMessage"),
	})
	private static abstract class BaseMessage<M extends Msg> {
		String username;
		String image;

		protected abstract M createMsg();

		public M mapToMsg() {
			M msg = createMsg();
			msg.setSourceName(username);
			msg.setImageUrl(image);
			return msg;
		}
	}

	private static class Commentary extends BaseMessage<Msg> {
		Date date;
		String msg;

		@Override
		protected Msg createMsg() {
			return new Msg();
		}

		@Override
		public Msg mapToMsg() {
			Msg msg = super.mapToMsg();
			msg.setDate(new DateTime(date));
			msg.setMessage(this.msg);
			return msg;
		}
	}

	private static class Trade extends BaseMessage<TradeSignal> {
		Entry entry;

		private static class Entry {
			String ticker;
			Date entryDate;
			String entryComments;
			EntryType entryType;
			Boolean shortSell;
			Integer shares;
			Double entryPrice;

			enum EntryType {
				STOCK(Contract.STOCK),
				OPTION(Contract.OPTIONS),
				FUTURES(Contract.FUTURES);

				final String contractType;
				EntryType(String contractType) {
					this.contractType = contractType;
				}
			};
		}

		protected int getNumShares() {
			return entry.shares;
		}

		protected TradeType getTradeType() {
			return entry.shortSell ? TradeType.SHORT : TradeType.BUY;
		}

		protected Date getDate() {
			return entry.entryDate;
		}

		protected double getPrice() {
			return entry.entryPrice;
		}

		protected String getMessage() {
			return entry.entryComments;
		}

		@Override
		protected TradeSignal createMsg() {
			return new TradeSignal();
		}

		@Override
		public TradeSignal mapToMsg() {
			TradeSignal trade = super.mapToMsg();

			Contract contract = new Contract();
			contract.setSymbol(entry.ticker);
			contract.setType(entry.entryType.contractType);
			trade.setContract(contract);
			trade.setNumShares(getNumShares());
			trade.setType(getTradeType());
			trade.setDate(new DateTime(getDate()));
			trade.setPrice(getPrice());
			trade.setMessage(getMessage());

			return trade;
		}
	}

	private static class PartialTrade extends Trade {
		PartialEntry partialEntry;

		private static class PartialEntry {
			boolean adding;
			Date tradeDate;
			int shares;
			double price;
			String comments;
		}

		protected int getNumShares() {
			return partialEntry.shares;
		}

		protected TradeType getTradeType() {
			return partialEntry.adding ? super.getTradeType() :
					(entry.shortSell ? TradeType.COVER : TradeType.SELL);
		}

		protected Date getDate() {
			return partialEntry.tradeDate;
		}

		protected double getPrice() {
			return partialEntry.price;
		}

		protected String getMessage() {
			return partialEntry.comments;
		}
	}

	private ObjectMapper mapper = new ObjectMapper()
			.setVisibility(PropertyAccessor.ALL, Visibility.NONE)
			.setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	@Override
	public boolean parseContent(InputStream input, long length, String contentType, MsgHandler handler)
			throws IOException, MsgParseException
	{
		Msg msg = mapper.readValue(input, Root.class).mapToMsg();
		log.info(msg.toString());
		return handler.newMsg(msg);
	}

}
