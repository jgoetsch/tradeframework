package com.jgoetsch.eventtrader.filter;

import java.util.Map;

import com.jgoetsch.eventtrader.TradeSignal;

public class PriceFilter extends FilterProcessor<TradeSignal> {

	private Double min;
	private Double max;

	@Override
	protected boolean handleProcessing(TradeSignal msg, Map<Object, Object> context) throws Exception {
		double priceValue = msg.getPrice();
		return priceValue > 0 && (min == null || priceValue >= min) && (max == null || priceValue <= max);
	}

	public Double getMin() {
		return min;
	}

	public void setMin(Double min) {
		this.min = min;
	}

	public Double getMax() {
		return max;
	}

	public void setMax(Double max) {
		this.max = max;
	}

}
