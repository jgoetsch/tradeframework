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
package com.jgoetsch.tradeframework.data;

import java.io.Closeable;
import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

import com.jgoetsch.tradeframework.Contract;
import com.jgoetsch.tradeframework.InvalidContractException;
import com.jgoetsch.tradeframework.OHLC;

public interface HistoricalDataSource extends Closeable {

	public static final int PERIOD_1_SECOND = 0;
	public static final int PERIOD_5_SECONDS = 1;
	public static final int PERIOD_15_SECONDS = 2;
	public static final int PERIOD_30_SECONDS = 3;
	public static final int PERIOD_1_MINUTE = 4;
	public static final int PERIOD_2_MINUTES = 5;
	public static final int PERIOD_3_MINUTES = 6;
	public static final int PERIOD_5_MINUTES = 7;
	public static final int PERIOD_15_MINUTES = 8;
	public static final int PERIOD_30_MINUTES = 9;
	public static final int PERIOD_1_HOUR = 10;
	public static final int PERIOD_1_DAY = 11;
	public static final int PERIOD_1_WEEK = 12;
	public static final int PERIOD_1_MONTH = 13;
	public static final int PERIOD_3_MONTHS = 14;
	public static final int PERIOD_1_YEAR = 15;

	public static final TimeZone timeZone = TimeZone.getTimeZone("America/New_York");

	/**
	 * Retrieves historical prices for the given symbol over the given number
	 * of periods prior to the given start date.
	 * 
	 * @param symbol symbol for which to retrieve historical price data.
	 * @param endDate date of the last period for which to retrieve price data.
	 * @param periodsBack number of periods back from the endDate for which to retrieve price data.
	 * @param period Calendar.DATE, Calendar.WEEK_OF_YEAR, or Calendar.MONTH for daily, weekly, or monthly prices.
	 * @return array of OHLC objects with the open, high, low, and close values of each period retrieved.
	 * @throws IOException
	 */
	public OHLC[] getHistoricalData(Contract contract, Date endDate, int numPeriods, int periodUnit) throws IOException, InvalidContractException, DataUnavailableException;

	/**
	 * Retrieves historical prices for the given symbol over the given dates.
	 * 
	 * @param symbol symbol for which to retrieve historical price data.
	 * @param startDate date of the first period for which to retrive price data.
	 * @param endDate date of the last period for which to retrieve price data.
	 * @param period Calendar.DATE, Calendar.WEEK_OF_YEAR, or Calendar.MONTH for daily, weekly, or monthly prices.
	 * @return array of OHLC objects with the open, high, low, and close values of each period retrieved.
	 * @throws IOException
	 */
	//public OHLC[] getHistoricalData(String symbol, Date startDate, Date endDate, int period) throws IOException;

}