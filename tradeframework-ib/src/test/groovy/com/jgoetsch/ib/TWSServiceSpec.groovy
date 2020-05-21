package com.jgoetsch.ib;

import spock.lang.Specification
import java.time.ZonedDateTime
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

import com.ib.client.EClientSocket
import com.ib.client.TickType
import com.jgoetsch.ib.handlers.BaseHandler
import com.jgoetsch.ib.handlers.SimpleHandlerDelegatingWrapper
import com.jgoetsch.tradeframework.Contract
import com.jgoetsch.tradeframework.marketdata.SimpleMarketData

import java.time.ZoneId;

class TWSServiceSpec extends Specification {

	static final REQUEST_ID = 100

	EClientSocket clientSocket = Mock(EClientSocket)
	SimpleHandlerDelegatingWrapper wrapper = new SimpleHandlerDelegatingWrapper()
	TWSService twsService = new TWSService(clientSocket, wrapper)
	ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

	def setup() {
		clientSocket.eConnect(*_) >> { scheduler.schedule({ wrapper.nextValidId(REQUEST_ID) }, 200, TimeUnit.MILLISECONDS) }
		assert twsService.connect()
	}

	def "Returns market data snapshot"() {
		when:
		def data = twsService.getMktDataSnapshot(Contract.stock("ABCD"))

		then:
		1 * clientSocket.reqMktData(_, *_) >> {arg -> scheduler.schedule({
			wrapper.tickPrice(arg[0], TickType.BID.index(), 1.50, null)
			wrapper.tickPrice(arg[0], TickType.ASK.index(), 1.60, null)
			wrapper.tickPrice(arg[0], TickType.LAST.index(), 1.55, null)
		}, 500, TimeUnit.MILLISECONDS) }
		data.bid == 1.50
		data.ask == 1.60
		data.last == 1.55
	}

	def "Market data snapshot times out"() {
		when:
		def data = twsService.getMktDataSnapshot(Contract.stock("ABCD"))

		then:
		1 * clientSocket.reqMktData(_, *_) >> {arg -> scheduler.schedule({
			wrapper.tickPrice(arg[0], TickType.BID.index(), 1.50, null)
			wrapper.tickPrice(arg[0], TickType.ASK.index(), 1.60, null)
			wrapper.tickPrice(arg[0], TickType.LAST.index(), 1.55, null)
		}, BaseHandler.WAIT_TIME + 500, TimeUnit.MILLISECONDS) }
		data.bid == 0
		data.ask == 0
		data.last == 0
	}
}