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

import java.text.NumberFormat;
import java.util.Date;

public class Execution {

	private int quantity;
	private double price;
	private double commission;
	private Date date;

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
	public Execution(int quantity, double price, Date date) {
		this.quantity = quantity;
		this.price = price;
		this.date = date;
	}

	@Override
	public String toString() {
		return (getQuantity() > 0 ? "BOT " : "SLD ") + Math.abs(getQuantity()) + " @" + NumberFormat.getNumberInstance().format(getPrice());
	}

	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}

	public void setCommission(double commission) {
		this.commission = commission;
	}

	public double getCommission() {
		return commission;
	}

}
