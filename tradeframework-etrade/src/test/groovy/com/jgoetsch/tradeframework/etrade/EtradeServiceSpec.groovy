package com.jgoetsch.tradeframework.etrade

import java.time.Instant
import java.util.concurrent.ExecutionException

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.google.api.client.http.HttpStatusCodes
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.LowLevelHttpRequest
import com.google.api.client.http.LowLevelHttpResponse
import com.google.api.client.json.Json
import com.google.api.client.testing.http.MockHttpTransport
import com.google.api.client.testing.http.MockLowLevelHttpRequest
import com.google.api.client.testing.http.MockLowLevelHttpResponse
import com.jgoetsch.tradeframework.Contract
import com.jgoetsch.tradeframework.Order
import com.jgoetsch.tradeframework.Order.OrderType
import com.jgoetsch.tradeframework.data.DataUnavailableException
import com.jgoetsch.tradeframework.etrade.dto.PlaceOrderRequest
import com.jgoetsch.tradeframework.etrade.dto.PlaceOrderResponse
import com.jgoetsch.tradeframework.etrade.dto.PreviewOrderRequest
import com.jgoetsch.tradeframework.etrade.dto.PreviewOrderResponse
import com.jgoetsch.tradeframework.etrade.dto.QuoteResponse

import groovy.json.JsonBuilder
import spock.lang.Specification

class EtradeServiceSpec extends Specification {
	def log = LoggerFactory.getLogger(EtradeServiceSpec.class);
	
	def transport = new MockHttpTransport() {
		@Override
		public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
			def uri = new URI(url)
			return new MockLowLevelHttpRequest() {
				@Override
				public LowLevelHttpResponse execute() throws IOException {
					def response = new MockLowLevelHttpResponse();
					def data = new File("src/test/resources/${uri.path}")
					if (data.exists()) {
						response.content = data.text
						response.statusCode = HttpStatusCodes.STATUS_CODE_OK
					} else {
						response.statusCode = HttpStatusCodes.STATUS_CODE_NOT_FOUND
					}
					response.setContentType(Json.MEDIA_TYPE)
				}
			}
		}
	}

	def client = Spy(new EtradeOAuthClient(transport))
	def etradeService = new EtradeService(client)

	def setup() {
		client.consumerKey = 'dummyconsumerkey'
		client.consumerSecret = 'dummyconsumersecret'
		client.setAccessTokens('dummytoken', 'dummytokensecret')
	}

	def "can get market data snapshot"() {
		when:
		def result = etradeService.getMktDataSnapshot(Contract.stock('GOOG')).get()

		then:
		1 * client.doGet(QuoteResponse, 'v1/market/quote/GOOG.json', _)
		
		with(result) {
			bid == 574.04
			bidSize == 100
			ask == 579.73
			askSize == 100
			last == 577.51
			lastTimestamp == Instant.parse('2012-07-03T17:00:00Z')
			timestamp == Instant.parse('2012-06-20T20:00:00Z')
		}
	}

	def "market data snapshot for invalid contract"() {
		when:
		etradeService.getMktDataSnapshot(Contract.stock('XXXX')).get()

		then:
		def ex = thrown(ExecutionException)
		ex.getCause() instanceof DataUnavailableException
	}

	def "preview order"() {
		given:
		def order = Order.limitOrder(Contract.stock("FB"), 1, 169, 'testaccount')

		when:
		def result = etradeService.previewOrder(order).get()

		then:
		1 * client.doPost(_ as PreviewOrderRequest, PreviewOrderResponse, 'v1/accounts/testaccount/orders/preview.json', _)

		with (result) {
			type == OrderType.LMT
			limitPrice == 169
			quantity == 1
			externalId == '3429395279'
			account == '83879627'
		}
	}

	def "place order with preview id"() {
		given:
		def order = Order.limitOrder(Contract.stock("FB"), 1, 169, 'testaccount')
		order.externalId = 3429395279

		when:
		def result = etradeService.placeOrder(order).get()

		then:
		1 * client.doPost({PlaceOrderRequest r -> r.previewIds.first().previewId == 3429395279}, PlaceOrderResponse,
			'v1/accounts/testaccount/orders/place.json', _)

		with (result) {
			type == OrderType.LMT
			limitPrice == 169
			quantity == 1
			externalId == '485'
			account == '83879627'
		}
	}

	def "placing order without preview id performs preview then place order"() {
		given:
		def order = Order.limitOrder(Contract.stock("FB"), 1, 169, 'testaccount')

		when:
		def result = etradeService.placeOrder(order).get()

		then:
		1 * client.doPost(_ as PreviewOrderRequest, PreviewOrderResponse, 'v1/accounts/testaccount/orders/preview.json', _)

		then:
		1 * client.doPost({PlaceOrderRequest r -> r.previewIds.first().previewId == 3429395279}, PlaceOrderResponse,
			'v1/accounts/testaccount/orders/place.json', _)

		with (result) {
			type == OrderType.LMT
			limitPrice == 169
			quantity == 1
			externalId == '485'
			account == '83879627'
		}

	}

}
