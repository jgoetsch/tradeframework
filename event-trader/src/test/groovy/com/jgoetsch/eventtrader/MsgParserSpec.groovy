package com.jgoetsch.eventtrader

import org.junit.Assert

import com.jgoetsch.eventtrader.source.MsgHandler
import com.jgoetsch.eventtrader.source.parser.PdJsonMsgParser
import com.jgoetsch.tradeframework.Contract

import spock.lang.Specification
import spock.lang.Unroll

class MsgParserSpec extends Specification {

	def msgParser = new PdJsonMsgParser()

	@Unroll
	def "Parses Msg #testData"() {
		when:
		def result = parseMsgFromJson(dataFile)

		then:
		result.getClass() == type
		with(result) {
			getSourceName() == sourceName
			getImageUrl() == "https://pbs.twimg.com/profile_images/example.jpg"
			getDate().toDate().getTime() == date
			getMessage().startsWith(text)
		}
		
		where:
		dataFile        | type        | sourceName      | date           | text
		'commentary_1'  | Msg         | "test_user1"    | 1344264740426L | "The quick brown fox jumped over the lazy dogs,"
		'commentary_2'  | Msg         | "test_user2"    | 1588605541436L | "Some commentary about some stock \$TICKER"
		'trade_cover_1' | TradeSignal | "test_user2"    | 1588690380113L | "Cover of short trade on a lower price for medium profit."
		'trade_buy_1'   | TradeSignal | "test_user1"    | 1588603047149L | "Buying a very volatile stock here on a dip,"
		'trade_sell_1'  | TradeSignal | "test_user1"    | 1588599616345L | "Sold position for a large profit"
	}

	@Unroll
	def "Parses TradeSignal #testData"() {
		when:
		def result = parseMsgFromJson(dataFile)

		then:
		result.getClass() == TradeSignal
		with (result as TradeSignal) {
			getType() == tradeType
			getContract() == contract
			getNumShares() == numShares
			getPrice() == price
		}

		where:
		dataFile        | tradeType       | contract               | numShares  | price
		'trade_cover_1' | TradeType.COVER | Contract.stock("ABCD") | 5000       | 2.35
		'trade_buy_1'   | TradeType.BUY   | Contract.stock("WXYZ") | 2000       | 0.1301
		'trade_sell_1'  | TradeType.SELL  | Contract.stock("WXYZ") | 15000      | 0.195
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
