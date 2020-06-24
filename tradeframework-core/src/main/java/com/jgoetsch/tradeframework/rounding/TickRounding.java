package com.jgoetsch.tradeframework.rounding;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.UnaryOperator;

public abstract class TickRounding implements UnaryOperator<BigDecimal> {
	public static final RoundingMode ROUNDING_MODE_DEFAULT = RoundingMode.HALF_EVEN;
	public static final UnaryOperator<BigDecimal> DEFAULT_STOCK = defaultStockTickSize(ROUNDING_MODE_DEFAULT);
	public static final UnaryOperator<BigDecimal> ROUND_TO_ONE = new RangeBoundedTickRounding(BigDecimal.ONE, ROUNDING_MODE_DEFAULT);

	private static UnaryOperator<BigDecimal> defaultStockTickSize(RoundingMode roundingMode) {
		Map<BigDecimal, BigDecimal> priceFloors = new TreeMap<BigDecimal, BigDecimal>();
		priceFloors.put(BigDecimal.ZERO, BigDecimal.valueOf(1, 4));
		priceFloors.put(BigDecimal.ONE, BigDecimal.valueOf(1, 2));
		return new RangeBoundedTickRounding(priceFloors, roundingMode);
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
