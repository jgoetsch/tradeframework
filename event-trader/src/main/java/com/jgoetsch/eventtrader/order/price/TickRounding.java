package com.jgoetsch.eventtrader.order.price;

import java.math.BigDecimal;

public interface TickRounding {

	public BigDecimal roundToTick(BigDecimal price, boolean isSell);

	public static TickRounding DEFAULT_STOCK = PriceMappedTickSize.defaultStockTickSize();

}
