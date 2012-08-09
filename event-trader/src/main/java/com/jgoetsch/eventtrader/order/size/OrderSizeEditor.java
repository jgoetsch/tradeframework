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

import java.beans.PropertyEditorSupport;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Property editor to accept an integer position size value as a
 * <code>PositionSizeCalculation</code> and convert it to a
 * <code>FixedPositionSize</code>, or <code>FixedPositionAmount</code> if value
 * is preceded by a dollar sign ($).
 * 
 * @author jgoetsch
 * 
 */
public class OrderSizeEditor extends PropertyEditorSupport {

	static final Pattern sizeDef = Pattern.compile("\\s*([\\w\\.\\$]+)\\s*(\\*?\\s*[\\d\\.]*)\\s*");
	static final Pattern numberPattern = Pattern.compile("[\\d\\.\\$]+");

	private String getFullClassName(String className) {
		String fullName = className;
		if (!className.contains("."))
			fullName = getClass().getPackage().getName() + "." + fullName;
		if (!className.endsWith("Size"))
			fullName = fullName + "Size";
		return fullName;
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		List<OrderSize> orderSizes = new ArrayList<OrderSize>();
		for (String tok : text.split("\\s*,\\s*")) {
			Matcher m = sizeDef.matcher(tok);
			if (m.matches()) {
				String className = m.group(1);
				String multiplier = m.group(2).replaceAll("\\*\\s*", "");

				OrderSize sizeObj;
				if (numberPattern.matcher(className).matches()) {
					try {
						if (tok.contains("$"))
							sizeObj = new FixedAmount(NumberFormat.getCurrencyInstance().parse(tok).doubleValue());
						else
							sizeObj = new FixedSize(NumberFormat.getIntegerInstance().parse(tok).intValue());
					} catch (ParseException e) {
						throw new IllegalArgumentException("Invalid order size format: " + tok);
					}
				}
				else {
					try {
						sizeObj = (OrderSize)Class.forName(getFullClassName(className)).newInstance();
					} catch (ClassNotFoundException e) {
						throw new IllegalArgumentException("Order size class \"" + className + "\" does not exist or is invalid", e);
					} catch (InstantiationException e) {
						throw new IllegalArgumentException("Order size class \"" + className + "\" cannot be instantiated", e);
					} catch (IllegalAccessException e) {
						throw new IllegalArgumentException("Order size class \"" + className + "\" cannot be instantiated", e);
					}
				}
				if (multiplier.length() > 0) {
					if (sizeObj instanceof MultipliedOrderSize) {
						try {
							((MultipliedOrderSize)sizeObj).setMultiplier(NumberFormat.getNumberInstance().parse(multiplier).doubleValue());
						} catch (ParseException e) {
							throw new IllegalArgumentException("Invalid size multiplier format (must be decimal number): " + e.getMessage());
						}
					}
					else
						throw new IllegalArgumentException("Order size class \"" + className + "\" cannot be used with a multiplier");
				}

				orderSizes.add(sizeObj);
			}
			else {
				throw new IllegalArgumentException("Invalid order size format: " + tok);
			}
		}
		if (orderSizes.size() > 1)
			setValue(new ConstrainedSize(orderSizes));
		else if (orderSizes.size() == 1)
			setValue(orderSizes.iterator().next());
		else
			throw new IllegalArgumentException("Bad order size: " + text);
	}

}
