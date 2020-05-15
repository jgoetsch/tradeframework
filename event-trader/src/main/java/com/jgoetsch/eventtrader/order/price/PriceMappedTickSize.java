package com.jgoetsch.eventtrader.order.price;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class PriceMappedTickSize extends TickSize {

	private final TreeMap<Double, BigDecimal> priceFloorsToTickSizes;

	static TickRounding defaultStockTickSize() {
		Map<Double, BigDecimal> priceFloors = new TreeMap<Double, BigDecimal>();
		priceFloors.put(Double.valueOf(0), BigDecimal.valueOf(1, 4));
		priceFloors.put(Double.valueOf(1), BigDecimal.valueOf(1, 2));
		return new PriceMappedTickSize(priceFloors);
	}

	/**
	 * Constructs a PriceMappedTickSize using a single tick size for all prices.
	 * @param tickSize the tick size to use
	 */
	public PriceMappedTickSize(BigDecimal tickSize) {
		this(Collections.singletonMap(Double.valueOf(0), tickSize));
	}

	/**
	 * Constructs a PriceMappedTickSize with the specified mapping of price floors to
	 * tick sizes.
	 * @param priceFloorsToTickSizes map containing price to tick size pairs. The tick size
	 *        for a given price will be calculated as the value corresponding to the greatest
	 *        key in the map that is less than or equal to the price. At minimum, an entry
	 *        with key 0.0 is required be present to provide a tick size for all possible prices.
	 */
	public PriceMappedTickSize(Map<Double, BigDecimal> priceFloorsToTickSizes) {
		if (!priceFloorsToTickSizes.containsKey(Double.valueOf(0)))
			throw new IllegalArgumentException("Tick size for price above 0 must be specified");
		this.priceFloorsToTickSizes = new TreeMap<Double, BigDecimal>(priceFloorsToTickSizes);
	}

	public BigDecimal getTickSize(double price) {
		return priceFloorsToTickSizes.floorEntry(Math.abs(price)).getValue();
	}
}
