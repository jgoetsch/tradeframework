package com.jgoetsch.eventtrader

import com.jgoetsch.eventtrader.order.price.PriceMappedTickSize
import com.jgoetsch.eventtrader.order.price.TickRounding
import spock.lang.Specification

class TickRoundingSpec extends Specification {

	def "TickRounding.DEFAULT_STOCK round to 4 decimals up to 1.00 and 2 above 1.00"() {
		expect:
		TickRounding.DEFAULT_STOCK.roundToTick(price, sell) == result

		where:
		sell  | price    | result
		false | 2.53     | 2.53
		false | 3.534    | 3.53
		false | 3.539991 | 3.54
		false | 1.005    | 1.00
		true  | 1.005    | 1.01
		false | 0.0923   | 0.0923
		true  | 0.723333 | 0.7233
		false | 0.99995  | 0.9999
		true  | 0.99995  | 1.00
		false | 10/3     | 3.33
		false | 1/3      | 0.3333
	}
	
	def "PriceMappedTickSize rounds price to tick"() {
		expect:
		new PriceMappedTickSize(rounding).roundToTick(price, true) == result

		where:
		rounding  | price    | result
		[(Double.valueOf(0)): new BigDecimal(".01"),(Double.valueOf(5)): new BigDecimal("0.05")] | 2.53     | 2.53
		[(Double.valueOf(0)): new BigDecimal(".01"),(Double.valueOf(5)): new BigDecimal("0.05")] | 5.53     | 5.55
		[(Double.valueOf(0)): new BigDecimal(".01"),(Double.valueOf(5)): new BigDecimal("0.25")] | 5.53     | 5.50
		[(Double.valueOf(0)): new BigDecimal(".01"),(Double.valueOf(5)): new BigDecimal("0.25"),(Double.valueOf(50)): new BigDecimal("12")] | 70 | 72
		[(Double.valueOf(0)): new BigDecimal(".01"),(Double.valueOf(40)): new BigDecimal("0.025")] | (40.0+40.175)/2.0 + (0.025 * 2) | 40.15
	}
}
