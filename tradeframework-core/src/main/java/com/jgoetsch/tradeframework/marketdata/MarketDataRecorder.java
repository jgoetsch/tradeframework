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
package com.jgoetsch.tradeframework.marketdata;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;

import com.jgoetsch.tradeframework.Contract;

public class MarketDataRecorder implements MarketDataListener {

	private PrintWriter out;
	private NumberFormat priceFormat;
	private DateFormat dateFormat;
	private String delimiter = "\t";
	private String lastLine;

	public MarketDataRecorder(OutputStream output) {
		this.out = new PrintWriter(output);
		this.priceFormat = new DecimalFormat("0.00");
	}

	public MarketDataRecorder(OutputStream output, DateFormat dateFormat) {
		this.out = new PrintWriter(output);
		this.priceFormat = new DecimalFormat("0.00");
		this.dateFormat = dateFormat;
	}

	public MarketDataRecorder(OutputStream output, DateFormat dateFormat, NumberFormat priceFormat, String delimiter) {
		this.out = new PrintWriter(output);
		this.priceFormat = priceFormat;
		this.dateFormat = dateFormat;
		this.delimiter = delimiter;
	}

	public synchronized void tick(Contract contract, MarketData data) {
		StringBuilder sb = new StringBuilder();
		sb.append(priceFormat.format(data.getLast()));
		sb.append(delimiter);
		sb.append(data.getLastSize());
		sb.append(delimiter);
		sb.append(priceFormat.format(data.getBid()));
		sb.append(delimiter);
		sb.append(data.getBidSize());
		sb.append(delimiter);
		sb.append(priceFormat.format(data.getAsk()));
		sb.append(delimiter);
		sb.append(data.getAskSize());
		String line = sb.toString();
		if (!line.equals(lastLine)) {
			String ts = dateFormat != null ? dateFormat.format(new Date(data.getTimestamp())) : ""+data.getTimestamp();
			out.println(ts + delimiter + line);
			out.flush();
			lastLine = line;
		}
	}

}
