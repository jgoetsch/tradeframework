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
package com.jgoetsch.eventtrader.order.size;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.regex.Pattern;

import com.jgoetsch.eventtrader.order.BaseOrderAttributeEditor;

/**
 * Property editor to accept an integer position size value as a
 * <code>PositionSizeCalculation</code> and convert it to a
 * <code>FixedPositionSize</code>, or <code>FixedPositionAmount</code> if value
 * is preceded by a dollar sign ($).
 * 
 * @author jgoetsch
 * 
 */
public class OrderSizeEditor extends BaseOrderAttributeEditor<OrderSize> {

	public OrderSizeEditor() {
		super(OrderSize.class, "Size", Pattern.compile("\\s*(\\$?[\\w\\.]+)\\s*(?:(\\*)\\s*([\\d\\.]+\\%?))?\\s*"));
	}

	@Override
	protected OrderSize createFixed(String specifier) {
		if (specifier.startsWith("$"))
			return new FixedAmount(new BigDecimal(specifier.substring(1)));
		else
			return new FixedSize(Integer.parseInt(specifier));
	}

	@Override
	protected void applyModifier(OrderSize target, String operation, BigDecimal value, String originalModifier) {
		if (target instanceof MultipliedOrderSize)
			((MultipliedOrderSize) target).setMultiplier(value);
		else
			throw new IllegalArgumentException("Order size class \"" + target.getClass().getSimpleName() + "\" cannot be used with a multiplier");
	}

	@Override
	protected OrderSize collectResults(Collection<OrderSize> results) {
		if (results.size() > 1)
			return new ConstrainedSize(results);
		else
			return results.stream().findFirst().orElse(null);
	}

}
