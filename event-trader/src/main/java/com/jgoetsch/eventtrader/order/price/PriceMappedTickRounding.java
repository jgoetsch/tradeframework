package com.jgoetsch.eventtrader.order.price;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implementation of a BiFunction to round prices to the nearest multiple of the minimum
 * tick size, where the tick size is defined by a map of price floors to tick sizes.
 * The tick size for a given price will be calculated as the value corresponding to the greatest
 * key in the map that is less than or equal to the absolute value of the price.
 * 
 * @author jgoetsch
 */
public class PriceMappedTickRounding extends TickRounding {

	private final TreeMap<BigDecimal, BigDecimal> priceFloorsToTickSizes;

	/**
	 * Constructs a PriceMappedTickSize using a single tick size for all prices.
	 * @param tickSize the tick size to use
	 * @param roundingMode {@link java.math.RoundingMode RoundingMode} with which to round
	 *        input prices between valid ticks to the higher or lower tick.
	 */
	public PriceMappedTickRounding(BigDecimal tickSize, RoundingMode roundingMode) {
		this(Collections.singletonMap(BigDecimal.ZERO, tickSize), roundingMode);
	}

	/**
	 * Constructs a PriceMappedTickSize with the specified mapping of price floors to
	 * tick sizes.
	 * 
	 * @param priceFloorsToTickSizes map containing price to tick size pairs.
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
	public PriceMappedTickRounding(Map<BigDecimal, BigDecimal> priceFloorsToTickSizes, RoundingMode roundingMode) {
		super(roundingMode);
		this.priceFloorsToTickSizes = new TreeMap<BigDecimal, BigDecimal>(priceFloorsToTickSizes);
		if (!this.priceFloorsToTickSizes.containsKey(BigDecimal.ZERO))
			throw new IllegalArgumentException("Tick size for price 0 is required; got only " + priceFloorsToTickSizes.toString());
	}

	public BigDecimal getTickSize(BigDecimal price) {
		return priceFloorsToTickSizes.floorEntry(price.abs()).getValue();
	}

	public String toString() {
		return "PriceMappedTickRounding" + priceFloorsToTickSizes.toString();
	}
}
