package com.jgoetsch.eventtrader

import com.jgoetsch.eventtrader.Msg
import com.jgoetsch.eventtrader.TradeSignal
import com.jgoetsch.eventtrader.order.price.AskPrice
import com.jgoetsch.eventtrader.order.price.BidPrice
import com.jgoetsch.eventtrader.order.price.ClosePrice
import com.jgoetsch.eventtrader.order.price.ConstrainedPrice
import com.jgoetsch.eventtrader.order.price.LastPrice
import com.jgoetsch.eventtrader.order.price.MidpointPrice
import com.jgoetsch.eventtrader.order.price.OffsetOrderPrice
import com.jgoetsch.eventtrader.order.price.PriceMappedTickRounding
import com.jgoetsch.eventtrader.order.price.TickRounding
import com.jgoetsch.tradeframework.Contract
import com.jgoetsch.tradeframework.marketdata.MarketData
import com.jgoetsch.tradeframework.marketdata.SimpleMarketData
import java.math.RoundingMode
import spock.lang.Specification
import spock.lang.Unroll

class OrderPriceSpec extends Specification {

	@Unroll
	def "#type.class.simpleName with offsets"() {
		given:
		def marketData = new SimpleMarketData(40.0, 41.0, 40.8)
		marketData.setClose(37)

		expect:
		testOffsetPrice(type, marketData, expectedBuy, expectedSell, 0.01)

		where:
		type             | expectedBuy | expectedSell
		new AskPrice()   | 41.00          | 40.00
		new BidPrice()   | 40.00          | 41.00
		new LastPrice()  | 40.80          | 40.80
		new ClosePrice() | 37.00          | 37.00
	}

	@Unroll
	def "MidpointPrice with tick size #tickSize and offsets"() {
		given:
		def orderPrice = new MidpointPrice();

		when:
		def marketData = new SimpleMarketData(bid, ask, ask)
		orderPrice.setBuyTickRounding(new PriceMappedTickRounding(tickSize, RoundingMode.HALF_DOWN))
		orderPrice.setSellTickRounding(new PriceMappedTickRounding(tickSize, RoundingMode.HALF_UP))
		
		then:
		testOffsetPrice(orderPrice, marketData, expectForBuy, expectForSell, tickSize)

		where:
		bid    | ask    | tickSize | expectForBuy | expectForSell
		40.0   | 41.0   | 0.01     | 40.50        | 40.50
		40.0   | 40.175 | 0.01     | 40.09        | 40.09
		0.10   | 0.175  | 0.0025   | 0.1375       | 0.1375
		0.10   | 0.175  | 0.025    | 0.125        | 0.150
		0.10   | 0.1751 | 0.025    | 0.150        | 0.150
		40.0   | 40.175 | 0.025    | 40.075       | 40.100
		40.0   | 40.175 | 0.5      | 40.0         | 40.0
	}

	@Unroll
	def "ConstrainedPrice"() {
		given:
		def p1 = new AskPrice();
		p1.setOffset(0.10);
		def p2 = new MidpointPrice();
		p2.setOffset(0.20);
		def orderPrice = new ConstrainedPrice(Arrays.asList(p1, p2));

		expect:
		price.equals(orderPrice.getValue(new TradeSignal(type, Contract.stock("TEST"), new Msg()), new SimpleMarketData(bid, ask, 40.8)))

		where:
		bid   | ask   | type           | price
		40.0  | 41.0  | TradeType.BUY  | 40.70
		40.0  | 41.0  | TradeType.SELL | 40.30
		40.0  | 40.1  | TradeType.BUY  | 40.20
		40.0  | 40.1  | TradeType.SELL | 39.90
	}

	void testOffsetPrice(OffsetOrderPrice orderPrice, MarketData marketData, BigDecimal expectForBuy, BigDecimal expectForSell, BigDecimal increment) {
		(-5..5).each {
			def offs = increment.multiply(it).stripTrailingZeros();
			orderPrice.setOffset(offs);
			assert expectForBuy.add(offs).equals(orderPrice.getValue(new TradeSignal(TradeType.BUY, Contract.stock("TEST"), new Msg()), marketData));
			assert expectForSell.subtract(offs).equals(orderPrice.getValue(new TradeSignal(TradeType.SELL, Contract.stock("TEST"), new Msg()), marketData));
		}
	}

}
