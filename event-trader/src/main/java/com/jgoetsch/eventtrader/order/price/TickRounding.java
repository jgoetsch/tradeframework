package com.jgoetsch.eventtrader.order.price;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.UnaryOperator;

public abstract class TickRounding implements UnaryOperator<BigDecimal> {

	public static UnaryOperator<BigDecimal> DEFAULT_STOCK_BUY = defaultStockTickSize(RoundingMode.HALF_DOWN);
	public static UnaryOperator<BigDecimal> DEFAULT_STOCK_SELL = defaultStockTickSize(RoundingMode.HALF_UP);

	private static UnaryOperator<BigDecimal> defaultStockTickSize(RoundingMode roundingMode) {
		Map<BigDecimal, BigDecimal> priceFloors = new TreeMap<BigDecimal, BigDecimal>();
		priceFloors.put(BigDecimal.ZERO, BigDecimal.valueOf(1, 4));
		priceFloors.put(BigDecimal.ONE, BigDecimal.valueOf(1, 2));
		return new PriceMappedTickRounding(priceFloors, roundingMode);
	}

	private final RoundingMode roundingMode;

	/**
	 * 
	 * @param roundingMode {@link java.math.RoundingMode RoundingMode} with which to round
	 *        input prices between valid ticks to the higher or lower tick.
	 */
	protected TickRounding(RoundingMode roundingMode) {
		this.roundingMode = roundingMode;
	}

	@Override
	public BigDecimal apply(BigDecimal price) {
		BigDecimal tick = getTickSize(price);
		return price.divide(tick, 0, roundingMode).multiply(tick);
	}

	protected abstract BigDecimal getTickSize(BigDecimal price);

}
