package com.jgoetsch.tradeframework.rounding;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implementation of a BiFunction to round values to the nearest multiple of the minimum
 * tick size, where the tick size is defined by a map of decimal value floors to tick sizes.
 * The tick size for a given input will be calculated as the value corresponding to the greatest
 * key in the map that is less than or equal to the absolute value of the input.
 * 
 * @author jgoetsch
 */
public class RangeBoundedTickRounding extends TickRounding {

	private final TreeMap<BigDecimal, BigDecimal> floorsToTickSizes;

	/**
	 * Constructs a PriceMappedTickSize using a single tick size for all prices.
	 * @param tickSize the tick size to use
	 * @param roundingMode {@link java.math.RoundingMode RoundingMode} with which to round
	 *        input prices between valid ticks to the higher or lower tick.
	 */
	public RangeBoundedTickRounding(BigDecimal tickSize, RoundingMode roundingMode) {
		this(Collections.singletonMap(BigDecimal.ZERO, tickSize), roundingMode);
	}

	public RangeBoundedTickRounding(BigDecimal tickSize) {
		this(tickSize, RoundingMode.HALF_UP);
	}

	/**
	 * Constructs a RangeBoundedTickRounding with the specified mapping of price floors to
	 * tick sizes.
	 * 
	 * @param floorsToTickSizes map containing price to tick size pairs.
	 *        At minimum, an entry for key 0 is required be present in the map in order
	 *        for all prices to have a defined tick size.
	 * @param roundingMode {@link java.math.RoundingMode RoundingMode} with which to round
	 *        input prices between valid ticks to the higher or lower tick.
	 *
	 * @apiNote The supplied map is copied into a {@link java.util.TreeMap TreeMap}, so
	 *          {@link java.math.BigDecimal BigDecimal} prices used as keys are treated as
	 *          equal based on their values regardless of scale. This means, for instance,
	 *          that an entry for {@code new BigDecimal("1")} is equivalent to that of
	 *          {@code new BigDecimal("1.00")}.
	 */
	public RangeBoundedTickRounding(Map<BigDecimal, BigDecimal> floorsToTickSizes, RoundingMode roundingMode) {
		super(roundingMode);
		this.floorsToTickSizes = new TreeMap<BigDecimal, BigDecimal>(floorsToTickSizes);
		if (!this.floorsToTickSizes.containsKey(BigDecimal.ZERO))
			throw new IllegalArgumentException("Tick size for price 0 is required; got only " + floorsToTickSizes.toString());
	}

	public RangeBoundedTickRounding(Map<BigDecimal, BigDecimal> floorsToTickSizes) {
		this(floorsToTickSizes, RoundingMode.HALF_UP);
	}

	public BigDecimal getTickSize(BigDecimal price) {
		return floorsToTickSizes.floorEntry(price.abs()).getValue();
	}

	public String toString() {
		return "RangeBoundedTickRounding" + floorsToTickSizes.toString();
	}
}
