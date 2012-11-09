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
package com.jgoetsch.ib.handlers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jgoetsch.tradeframework.OHLC;
import com.jgoetsch.tradeframework.SimpleOHLC;
import com.jgoetsch.tradeframework.data.HistoricalDataSource;

public class HistoricalDataHandler extends BaseIdHandler {

	private List<OHLC> data;
	private volatile boolean finished;

	public HistoricalDataHandler(int tickerId) {
		super(tickerId);
		this.data = new ArrayList<OHLC>();
		this.finished = false;
	}

	@Override
	public int getStatus() {
		if (finished)
			return STATUS_SUCCESS;
		else if (getErrorCode() == 162 || getErrorCode() == 200)
			return STATUS_FAILED;
		else
			return super.getStatus();
	}

	@Override
	protected void onHistoricalData(String date, double open, double high,
			double low, double close, int volume, int count, double WAP, boolean hasGaps)
	{
		if (date.startsWith("finished")) {
			synchronized (this) {
				finished = true;
				this.notifyAll();
			}
		}
		else {
			SimpleOHLC ohlc = new SimpleOHLC();
			ohlc.setOpen(open);
			ohlc.setHigh(high);
			ohlc.setLow(low);
			ohlc.setClose(close);
			ohlc.setVolume(volume * 100);
			try {
				if (date.length() == 8) {
					DateFormat df = new SimpleDateFormat("yyyyMMdd");
					df.setTimeZone(HistoricalDataSource.timeZone);
					ohlc.setDate(df.parse(date));
				}
				else
					ohlc.setDate(new Date(Long.parseLong(date) * 1000));
			}
			catch (ParseException e) {
				e.printStackTrace();
			}
			catch (NumberFormatException e) {
				e.printStackTrace();
			}

			synchronized (this) {
				data.add(ohlc);
			}
		}
	}

	public OHLC[] getData() {
		return data.toArray(new OHLC[0]);
	}

}
