package com.jgoetsch.tradeframework;

import spock.lang.Specification
import java.time.ZonedDateTime
import java.time.ZoneId;

class SimpleOHLCSpec extends Specification {
	
	def "toString converts to string"() {
		given:
		def ohlc = new SimpleOHLC()
		ohlc.setDate(Date.from(ZonedDateTime.of(2020, 5, 8, 10, 15, 0, 0, ZoneId.of("America/New_York")).toInstant()))
		ohlc.setOpen(1.5)
		ohlc.setHigh(2.43)
		ohlc.setLow(1.32)
		ohlc.setClose(1.88)
		
		expect:
		ohlc.toString() == "05/08/20 10:15:00 EDT O=1.5 H=2.43 L=1.32 C=1.88"
	}
}