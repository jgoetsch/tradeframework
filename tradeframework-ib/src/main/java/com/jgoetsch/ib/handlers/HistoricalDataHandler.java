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

import com.ib.client.Bar;
import com.jgoetsch.tradeframework.OHLC;
import com.jgoetsch.tradeframework.SimpleOHLC;
import com.jgoetsch.tradeframework.data.HistoricalDataSource;

public class HistoricalDataHandler extends BaseIdHandler {

	private List<OHLC> data;
	private boolean finished;

	public HistoricalDataHandler(int tickerId) {
		super(tickerId);
		this.data = new ArrayList<OHLC>();
		this.finished = false;
	}

	@Override
	public int getStatus() {
		synchronized(this) {
			if (finished)
				return STATUS_SUCCESS;
			else if (getErrorCode() == 162 || getErrorCode() == 200)
				return STATUS_FAILED;
		}
		return super.getStatus();
	}

	@Override
	protected void onHistoricalData(Bar bar)
	{
		SimpleOHLC ohlc = new SimpleOHLC();
		ohlc.setOpen(bar.open());
		ohlc.setHigh(bar.high());
		ohlc.setLow(bar.low());
		ohlc.setClose(bar.close());
		ohlc.setVolume(bar.volume() * 100);
		try {
			if (bar.time().length() == 8) {
				DateFormat df = new SimpleDateFormat("yyyyMMdd");
				df.setTimeZone(HistoricalDataSource.timeZone);
				ohlc.setDate(df.parse(bar.time()));
			}
			else
				ohlc.setDate(new Date(Long.parseLong(bar.time()) * 1000));
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

	@Override
	protected synchronized void onHistoricalDataEnd(String startDateStr, String endDateStr) {
		finished = true;
		this.notifyAll();
	}

	public OHLC[] getData() {
		return data.toArray(new OHLC[0]);
	}

}
