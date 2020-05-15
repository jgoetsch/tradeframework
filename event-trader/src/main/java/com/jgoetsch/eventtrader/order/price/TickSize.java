package com.jgoetsch.eventtrader.order.price;

import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class TickSize implements TickRounding {

	@Override
	public BigDecimal roundToTick(BigDecimal price, boolean isSell) {
		BigDecimal tick = getTickSize(price.doubleValue());
		return price.divide(tick, 0, isSell ? RoundingMode.HALF_UP : RoundingMode.HALF_DOWN).multiply(tick);
	}

	protected abstract BigDecimal getTickSize(double price);

}
