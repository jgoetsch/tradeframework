package com.jgoetsch.eventtrader

import com.jgoetsch.eventtrader.source.MsgHandler
import com.jgoetsch.eventtrader.source.parser.MsgParseException
import com.jgoetsch.eventtrader.source.parser.UnrecognizedMsgTypeException
import com.jgoetsch.eventtrader.source.parser.JsonMsgParser
import com.jgoetsch.eventtrader.source.parser.mapper.PdMsgMapper
import com.jgoetsch.tradeframework.Contract

import spock.lang.Specification
import spock.lang.Unroll

class MsgParserSpec extends Specification {

	def msgParser = new JsonMsgParser(PdMsgMapper)

	@Unroll
	def "Parses Msg #dataFile"() {
		when:
		def result = parseMsgFromJson(dataFile)

		then:
		result.getClass() == type
		with(result) {
			getSourceName() == sourceName
			getImageUrl() == "https://pbs.twimg.com/profile_images/example.jpg"
			getDate().toEpochMilli() == date
			getMessage().startsWith(text)
		}
		
		where:
		dataFile        | type        | sourceName      | date           | text
		'commentary_1'  | Msg         | "test_user1"    | 1344264740426L | "The quick brown fox jumped over the lazy dogs,"
		'commentary_2'  | Msg         | "test_user2"    | 1588605541436L | "Some commentary about some stock \$TICKER"
		'trade_buy'     | TradeSignal | "test_user1"    | 1588603047149L | "Buying a very volatile stock here on a dip,"
		'trade_sell'    | TradeSignal | "test_user1"    | 1588599616345L | "Sold position for a large profit"
		'trade_short'   | TradeSignal | "test_user2"    | 1588772127524L | "Shorted on high of the day spike to new levels"
		'trade_cover'   | TradeSignal | "test_user2"    | 1588772644631L | "Cover of short trade on a lower price for medium profit!"
		'trade_add_buy' | TradeSignal | "test_user1"    | 1588777300294L | "Adding shares to long position"
	}

	@Unroll
	def "Parses TradeSignal #dataFile"() {
		when:
		def result = parseMsgFromJson(dataFile)

		then:
		result.getClass() == TradeSignal
		with (result as TradeSignal) {
			getType() == tradeType
			getContract() == Contract.stock(ticker)
			getNumShares() == numShares
			getPrice().equals(new BigDecimal(price))
			isPartial() == partial
			getTradeString() == tradeString
		}

		where:
		dataFile        | tradeType       | ticker  | numShares  | price     | partial | tradeString
		'trade_buy'     | TradeType.BUY   | "WXYZ"  | 2000       | '0.1301'  | false   | "Buy 2,000 WXYZ at \$0.1301"
		'trade_sell'    | TradeType.SELL  | "WXYZ"  | 15000      | '0.195'   | false   | "Sell 15,000 WXYZ at \$0.195"
		'trade_short'   | TradeType.SHORT | "ABCD"  | 15000      | '1.0'     | false   | "Short 15,000 ABCD at \$1.00"
		'trade_cover'   | TradeType.COVER | "ABCD"  | 15000      | '1.17'    | false   | "Cover 15,000 ABCD at \$1.17"
		'trade_add_buy' | TradeType.BUY   | "WXYZ"  | 5000       | '12.19'   | true    | "Buy 5,000 WXYZ at \$12.19"
	}

	@Unroll
	def "Throws exception on unrecognized #command"() {
		when:
		def result = parseMsgFromJson(dataFile)
		
		then:
		UnrecognizedMsgTypeException e = thrown()
		e.getTypeId() == command
		result == null

		where:
		dataFile                 | command
		'unrecognized_command_1' | 'UserTrade'
		'unrecognized_command_2' | 'SingleKarmaMessage'
	}
	
	@Unroll
	def "Throws exception for #dataFile"() {
		when:
		def result = parseMsgFromJson(dataFile)
		
		then:
		MsgParseException e = thrown()
		e.message.contains(exceptionMessage)
		result == null

		where:
		dataFile          | exceptionMessage
		'missing_field_1' | "message.partialEntry must not be null"
		'missing_field_2' | "message.msg must not be null"
		'missing_type_id' | "Missing type id"
		'malformed'       | "Unexpected end-of-input"
	}

	Msg parseMsgFromJson(String dataFile) {
		def data = new File("src/test/resources/messages/${dataFile}.json")
		def result
		msgParser.parseContent(data.newInputStream(), data.length(), null,
			{ msg ->
				result = msg
				false
			}
		)
		result
	}
}
