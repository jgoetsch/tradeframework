package com.jgoetsch.eventtrader.filter;

import java.math.BigDecimal;
import java.util.Map;

import com.jgoetsch.eventtrader.TradeSignal;

public class PriceFilter extends FilterProcessor<TradeSignal> {

	private BigDecimal min;
	private BigDecimal max;

	@Override
	protected boolean handleProcessing(TradeSignal msg, Map<Object, Object> context) throws Exception {
		BigDecimal priceValue = msg.getPrice();
		return priceValue != null && (min == null || priceValue.compareTo(min) >= 0) && (max == null || priceValue.compareTo(max) <= 0);
	}

	public BigDecimal getMin() {
		return min;
	}

	public void setMin(BigDecimal min) {
		this.min = min;
	}

	public BigDecimal getMax() {
		return max;
	}

	public void setMax(BigDecimal max) {
		this.max = max;
	}

}
