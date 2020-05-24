package com.jgoetsch.eventtrader

import com.jgoetsch.eventtrader.order.price.ConstrainedPrice
import com.jgoetsch.eventtrader.order.price.OrderPriceEditor
import com.jgoetsch.eventtrader.order.size.OrderSizeEditor
import spock.lang.Specification
import spock.lang.Unroll

class OrderPropertyEditorSpec extends Specification {

	@Unroll
	def "creates price for #propertyValue"() {
		when:
		def editor = new OrderPriceEditor()
		editor.setAsText(propertyValue)

		then:
		editor.getValue().toString() == priceToString
		
		where:
		propertyValue                                 | priceToString
		"Ask + 0.01, TradeSignal + 0.02"              | "[AskPrice+0.01, TradeSignalPrice+0.02]"
		"Ask - 0.015, TradeSignalPrice + 2.5%"        | "[AskPrice-0.015, TradeSignalPrice+2.5%]"
		"Bid + 0.02, TradeSignal - 0.02, Close + .30" | "[BidPrice+0.02, TradeSignalPrice-0.02, ClosePrice+0.30]"
		"Midpoint"                                    | "MidpointPrice+0"
		"Bid+0.15,Ask-1%,TradeSignal +5.0%"           | "[BidPrice+0.15, AskPrice-1%, TradeSignalPrice+5%]"
	}

	@Unroll
	def "creates size for #propertyValue"() {
		when:
		def editor = new OrderSizeEditor()
		editor.setAsText(propertyValue)

		then:
		editor.getValue().toString() == priceToString
		
		where:
		propertyValue                                 | priceToString
		"TradeSignal * .5"                            | "TradeSignalSize*0.5"
		"TradeSignal * 0.3, 5000"                     | "[TradeSignalSize*0.3, 5000]"
		"\$10000, 400, TradeSignal*65%"               | "[\$10000, 400, TradeSignalSize*0.65]"
	}

}
