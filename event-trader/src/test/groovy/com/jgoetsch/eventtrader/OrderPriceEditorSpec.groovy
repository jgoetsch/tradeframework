package com.jgoetsch.eventtrader

import com.jgoetsch.eventtrader.order.price.ConstrainedPrice
import com.jgoetsch.eventtrader.order.price.OrderPriceEditor
import spock.lang.Specification
import spock.lang.Unroll

class OrderPriceEditorSpec extends Specification {

	@Unroll
	def "creates price for #propertyValue"() {
		when:
		def editor = new OrderPriceEditor()
		editor.setAsText(propertyValue)

		then:
		editor.getValue().toString() == priceToString
		
		where:
		propertyValue                                 | priceToString
		"Ask + 0.01, TradeSignal + 0.02"              | "ConstrainedPrice: [AskPrice+0.01, TradeSignalPrice+0.02]"
		"Ask - 0.015, TradeSignalPrice + 2.5%"        | "ConstrainedPrice: [AskPrice-0.015, TradeSignalPrice+2.5%]"
		"Bid + 0.02, TradeSignal - 0.02, Close + .30" | "ConstrainedPrice: [BidPrice+0.02, TradeSignalPrice-0.02, ClosePrice+0.30]"
		"Midpoint"                                    | "MidpointPrice+0"
		"Bid+0.15,Ask-1%,TradeSignal +5.0%"           | "ConstrainedPrice: [BidPrice+0.15, AskPrice-1%, TradeSignalPrice+5%]"
	}

}
