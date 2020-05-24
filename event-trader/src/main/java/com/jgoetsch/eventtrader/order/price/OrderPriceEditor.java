/*
 * Copyright (c) 2012 Jeremy Goetsch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jgoetsch.eventtrader.order.price;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.regex.Pattern;

import com.jgoetsch.eventtrader.order.BaseOrderAttributeEditor;

/**
 * Property editor to accept an integer position size value as a
 * <code>PositionSizeCalculation</code> and convert it to a
 * <code>FixedPositionSize</code>.
 * 
 * @author jgoetsch
 *
 */
public class OrderPriceEditor extends BaseOrderAttributeEditor<OrderPrice> {

	public OrderPriceEditor() {
		super(OrderPrice.class, "Price", Pattern.compile("\\s*([\\w\\.]+)\\s*(?:(\\+|\\-)\\s*([\\d\\.]+%?))?\\s*"));
	}

	@Override
	protected OrderPrice createFixed(String specifier) {
		return new FixedPrice(new BigDecimal(specifier));
	}

	@Override
	protected void applyModifier(OrderPrice target, String operation, BigDecimal value, String originalModifier) {
		if (target instanceof OffsetOrderPrice) {
			if (originalModifier.endsWith("%")) {
				((OffsetOrderPrice)target).setPercentage(true);
			}
			((OffsetOrderPrice) target).setOffset(operation.equals("-") ? value.negate() : value);
		}
		else
			throw new IllegalArgumentException("Order price class \"" + target.getClass().getSimpleName() + "\" cannot be used with an offset amount");
	}

	@Override
	protected OrderPrice collectResults(Collection<OrderPrice> results) {
		if (results.size() > 1)
			return new ConstrainedPrice(results);
		else
			return results.stream().findFirst().orElse(null);
	}

}
