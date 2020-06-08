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
package com.jgoetsch.tradeframework;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;

public class Execution {

	private BigDecimal quantity;
	private BigDecimal price;
	private BigDecimal commission;
	private Instant date;

	public Execution() {	
	}

	public Execution(Execution source) {
		this.quantity = source.quantity;
		this.price = source.price;
		this.commission = source.commission;
		this.date = source.date;
	}
	/**
	 * @param side
	 * @param quantity
	 * @param price
	 * @param date
	 */
	public Execution(BigDecimal quantity, BigDecimal price, Instant date) {
		this.quantity = quantity;
		this.price = price;
		this.date = date;
	}

	@Override
	public String toString() {
		return (getQuantity().signum() > 0 ? "BOT " : "SLD ") + getQuantity().abs() + " @" + NumberFormat.getNumberInstance().format(getPrice());
	}

	public BigDecimal getQuantity() {
		return quantity;
	}
	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	public Instant getDate() {
		return date;
	}
	public void setDate(Instant date) {
		this.date = date;
	}

	public void setCommission(BigDecimal commission) {
		this.commission = commission;
	}

	public BigDecimal getCommission() {
		return commission;
	}

}
