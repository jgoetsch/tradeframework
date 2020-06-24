package com.jgoetsch.tradeframework

import spock.lang.Specification
import spock.lang.Unroll
import java.math.RoundingMode

import com.jgoetsch.tradeframework.rounding.RangeBoundedTickRounding
import com.jgoetsch.tradeframework.rounding.TickRounding

class TickRoundingSpec extends Specification {

	@Unroll
	def "#tickRounding rounds #price to #result"() {
		expect:
		tickRounding.apply(price) == result

		where:
		tickRounding                           | price      | result
		TickRounding.DEFAULT_STOCK             | 2.53       | 2.53
		TickRounding.DEFAULT_STOCK             | 3.534      | 3.53
		TickRounding.DEFAULT_STOCK             | 3.539991   | 3.54
		TickRounding.DEFAULT_STOCK             | 1.005      | 1.00
		TickRounding.DEFAULT_STOCK             | 0.0923     | 0.0923
		TickRounding.DEFAULT_STOCK             | 0.723333   | 0.7233
		TickRounding.DEFAULT_STOCK             | 0.99995    | 1.00
		TickRounding.DEFAULT_STOCK             | 10/3       | 3.33
		TickRounding.DEFAULT_STOCK             | 1/3        | 0.3333
		TickRounding.DEFAULT_STOCK             | Math.PI    | 3.14
		TickRounding.DEFAULT_STOCK             | Math.PI/10 | 0.3142
		tr([0.0: 0.01, 5.0: 0.05])             | 2.53       | 2.53
		tr([0.0: 0.01, 5.0: 0.05])             | 5.53       | 5.55
		tr([0.0: 0.01, 5.0: 0.25])             | 5.53       | 5.50
		tr([0.0: 0.01, 9.9: 0.25, 50.0: 12.0]) | 100/3      | 33.25
		tr([0.0: 0.01, 5.0: 0.25, 50.0: 12.0]) | 70.0       | 72.0
		tr([0.0: 0.01, 40.0: 0.025])           | 40.1375    | 40.15
	}

	def tr(priceFloorsToTickSizes) {
		new RangeBoundedTickRounding(priceFloorsToTickSizes, RoundingMode.HALF_UP)
	}
}
