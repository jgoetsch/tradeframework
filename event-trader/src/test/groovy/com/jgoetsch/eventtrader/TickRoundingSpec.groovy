package com.jgoetsch.eventtrader

import spock.lang.Specification
import spock.lang.Unroll
import java.math.RoundingMode
import java.util.Map

import com.jgoetsch.tradeframework.rounding.PriceMappedTickRounding
import com.jgoetsch.tradeframework.rounding.TickRounding

class TickRoundingSpec extends Specification {

	@Unroll
	def "#tickRounding rounds #price to #result"() {
		expect:
		tickRounding.apply(price) == result

		where:
		tickRounding                          | price      | result
		TickRounding.DEFAULT_STOCK_BUY        | 2.53       | 2.53
		TickRounding.DEFAULT_STOCK_BUY        | 3.534      | 3.53
		TickRounding.DEFAULT_STOCK_BUY        | 3.539991   | 3.54
		TickRounding.DEFAULT_STOCK_BUY        | 1.005      | 1.00
		TickRounding.DEFAULT_STOCK_SELL       | 1.005      | 1.01
		TickRounding.DEFAULT_STOCK_BUY        | 0.0923     | 0.0923
		TickRounding.DEFAULT_STOCK_SELL       | 0.723333   | 0.7233
		TickRounding.DEFAULT_STOCK_BUY        | 0.99995    | 0.9999
		TickRounding.DEFAULT_STOCK_SELL       | 0.99995    | 1.00
		TickRounding.DEFAULT_STOCK_BUY        | 10/3       | 3.33
		TickRounding.DEFAULT_STOCK_BUY        | 1/3        | 0.3333
		TickRounding.DEFAULT_STOCK_SELL       | Math.PI    | 3.14
		TickRounding.DEFAULT_STOCK_BUY        | Math.PI/10 | 0.3142
		pmtr([0.0: 0.01, 5.0: 0.05])             | 2.53    | 2.53
		pmtr([0.0: 0.01, 5.0: 0.05])             | 5.53    | 5.55
		pmtr([0.0: 0.01, 5.0: 0.25])             | 5.53    | 5.50
		pmtr([0.0: 0.01, 9.9: 0.25, 50.0: 12.0]) | 100/3   | 33.25
		pmtr([0.0: 0.01, 5.0: 0.25, 50.0: 12.0]) | 70.0    | 72.0
		pmtr([0.0: 0.01, 40.0: 0.025])           | 40.1375 | 40.15
	}

	def pmtr(priceFloorsToTickSizes) {
		new PriceMappedTickRounding(priceFloorsToTickSizes, RoundingMode.HALF_UP)
	}
}
