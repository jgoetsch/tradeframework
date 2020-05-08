package com.jgoetsch.eventtrader.source.parser.mapper;

import java.util.Arrays;
import java.util.Date;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.eventtrader.TradeType;
import com.jgoetsch.tradeframework.Contract;

public class PdMsgMapper implements MsgMappable {
	@NotNull String command;
	@NotNull @Valid BaseMessage<?> message;

	@Override
	public boolean hasMsg() {
		return Arrays.asList("Commentary", "Trade", "PartialTrade").contains(command);
	}

	@Override
	public Msg toMsg() {
		Msg msg = message.toMsg();
		msg.setSourceType(command);
		return msg;
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

		public M toMsg() {
			M msg = createMsg();
			msg.setSourceName(username);
			msg.setImageUrl(image);
			return msg;
		}
	}

	private static class Commentary extends BaseMessage<Msg> {
		@NotNull Date date;
		@NotNull String msg;

		@Override
		protected Msg createMsg() {
			return new Msg();
		}

		@Override
		public Msg toMsg() {
			Msg msg = super.toMsg();
			msg.setDate(date.toInstant());
			msg.setMessage(this.msg);
			return msg;
		}
	}

	private static class Trade extends BaseMessage<TradeSignal> {
		@NotNull @Valid Entry entry;

		private static class Entry {
			@NotNull String ticker;
			@NotNull Date entryDate;
			@NotNull String entryComments;
			@NotNull EntryType entryType;
			@NotNull Boolean shortSell;
			@NotNull Integer shares;
			@NotNull Double entryPrice;

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
		public TradeSignal toMsg() {
			TradeSignal trade = super.toMsg();

			Contract contract = new Contract();
			contract.setSymbol(entry.ticker);
			contract.setType(entry.entryType.contractType);
			trade.setContract(contract);
			trade.setNumShares(getNumShares());
			trade.setType(getTradeType());
			trade.setDate(getDate().toInstant());
			trade.setPrice(getPrice());
			trade.setMessage(getMessage());

			return trade;
		}
	}

	private static class PartialTrade extends Trade {
		@NotNull @Valid PartialEntry partialEntry;

		private static class PartialEntry {
			@NotNull Boolean adding;
			@NotNull Date tradeDate;
			@NotNull Integer shares;
			@NotNull Double price;
			@NotNull String comments;
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

		@Override
		public TradeSignal toMsg() {
			TradeSignal trade = super.toMsg();
			if (partialEntry.adding || !partialEntry.shares.equals(entry.shares))
				trade.setPartial(true);
			return trade;
		}
	}

}
