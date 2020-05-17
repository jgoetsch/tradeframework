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

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Property editor to accept an integer position size value as a
 * <code>PositionSizeCalculation</code> and convert it to a
 * <code>FixedPositionSize</code>.
 * 
 * @author jgoetsch
 *
 */
public class OrderPriceEditor extends PropertyEditorSupport {

	@Override
	public String getAsText() {
		if (getValue() instanceof FixedPrice)
			return String.valueOf(((FixedPrice)getValue()).getPrice());
		else
			return null;
	}

	private String getFullClassName(String className) {
		String fullName = className;
		if (!className.contains("."))
			fullName = getClass().getPackage().getName() + "." + fullName;
		if (!className.endsWith("Price"))
			fullName = fullName + "Price";
		return fullName;
	}

	Pattern priceDef = Pattern.compile("\\s*([A-Za-z]+)\\s*((?:\\+|\\-)?\\s*[\\d\\.]*%?)\\s*");

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		List<OrderPrice> prices = new ArrayList<OrderPrice>();
		for (String tok : text.split("\\s*,\\s*")) {
			Matcher m = priceDef.matcher(tok);
			if (m.matches()) {
				String className = m.group(1);
				String offset = m.group(2).replaceAll("[\\s\\+]", "");
				try {
					Class<?> orderPriceClass = Class.forName(getFullClassName(className));
					if (!OrderPrice.class.isAssignableFrom(orderPriceClass)) {
						throw new IllegalArgumentException(orderPriceClass + " does not implement the OrderPrice interface");
					}
					else {
						try {
							OrderPrice priceObj = (OrderPrice) orderPriceClass.getDeclaredConstructor().newInstance();
							if (offset.length() > 0) {
								if (priceObj instanceof OffsetOrderPrice) {
									try {
										DecimalFormat df;
										if (offset.contains("%")) {
											df = (DecimalFormat)DecimalFormat.getPercentInstance();
											((OffsetOrderPrice)priceObj).setPercentage(true);
										}
										else {
											df = (DecimalFormat)DecimalFormat.getNumberInstance();
										}
										df.setParseBigDecimal(true);
										((OffsetOrderPrice)priceObj).setOffset((BigDecimal)df.parse(offset));
									} catch (ParseException e) {
										throw new IllegalArgumentException("Invalid price offset format (must be decimal number or percentage): " + e.getMessage());
									}
								}
								else
									throw new IllegalArgumentException("Order price class \"" + className + "\" cannot be used with an offset amount");
							}
							prices.add(priceObj);
						}  catch (ReflectiveOperationException e) {
							throw new IllegalArgumentException("Order price class \"" + className + "\" cannot be instantiated", e);
						}
					}
				} catch (ClassNotFoundException e) {
					throw new IllegalArgumentException("Order price class \"" + className + "\" does not exist or is invalid", e);
				}
			}
			else {
				try {
					prices.add(new FixedPrice(new BigDecimal(tok)));
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("Invalid order price format: " + tok);
				}
			}
		}
		if (prices.size() > 1)
			setValue(new ConstrainedPrice(prices));
		else if (prices.size() == 1)
			setValue(prices.iterator().next());
		else
			throw new IllegalArgumentException("Bad order price: " + text);
	}

}
