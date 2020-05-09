package com.jgoetsch.eventtrader

import com.jgoetsch.eventtrader.source.MsgHandler
import com.jgoetsch.eventtrader.source.parser.MsgParseException
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
			getPrice() == price
			isPartial() == partial
		}

		where:
		dataFile        | tradeType       | ticker  | numShares  | price   | partial
		'trade_buy'     | TradeType.BUY   | "WXYZ"  | 2000       | 0.1301  | false
		'trade_sell'    | TradeType.SELL  | "WXYZ"  | 15000      | 0.195   | false
		'trade_short'   | TradeType.SHORT | "ABCD"  | 15000      | 1.32    | false
		'trade_cover'   | TradeType.COVER | "ABCD"  | 15000      | 1.17    | false
		'trade_add_buy' | TradeType.BUY   | "WXYZ"  | 5000       | 2.19    | true
	}

	def "Does not parse unrecognized command"() {
		when:
		def result = parseMsgFromJson('unrecognized_command')
		
		then:
		result == null
		noExceptionThrown()
	}
	
	@Unroll
	def "Throws exception on #dataFile: #exceptionMessage"() {
		when:
		def result = parseMsgFromJson(dataFile)
		
		then:
		result == null
		MsgParseException e = thrown()
		e.message.contains(exceptionMessage)
		
		where:
		dataFile    | exceptionMessage
		'invalid_1' | "message.partialEntry must not be null"
		'invalid_2' | "message.msg must not be null"
		'invalid_3' | "Missing type id"
		
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
