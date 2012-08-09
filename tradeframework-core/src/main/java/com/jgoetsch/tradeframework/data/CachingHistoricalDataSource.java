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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.jgoetsch.tradeframework.Contract;
import com.jgoetsch.tradeframework.InvalidContractException;
import com.jgoetsch.tradeframework.OHLC;

/**
 * HistoricalDataSource wrapper that will cache the results of historical data requests to
 * the local disk and return the cached copy if the same request is made again.
 * This can help to alleviate the necessary throttling of requests necessary to avoid
 * historical data pacing violations from the IB system, especially when running simulations
 * on the same data repeatedly.
 * 
 * @author jgoetsch
 *
 */
public class CachingHistoricalDataSource implements HistoricalDataSource {

	private final HistoricalDataSource historicalDataSource;
	private final String cacheLocation;

	public CachingHistoricalDataSource(HistoricalDataSource histDataSource) {
		this.historicalDataSource = histDataSource;
		this.cacheLocation = "historicaldata/";
	}

	public CachingHistoricalDataSource(HistoricalDataSource histDataSource, String cacheLocation) {
		this.historicalDataSource = histDataSource;
		this.cacheLocation = cacheLocation;
	}

	public OHLC[] getHistoricalData(Contract contract, Date endDate, int numPeriods, int periodUnit)
	throws IOException, InvalidContractException, DataUnavailableException
	{
		StringBuilder reqString = new StringBuilder(cacheLocation);
		reqString.append(contract.getType());
		reqString.append("_").append(contract.getSymbol());
		if (contract.getExpiry() != null)
			reqString.append("_").append(contract.getExpiry());
		reqString.append("_").append((new SimpleDateFormat("yyyyMMdd_HHmmss")).format(endDate));
		reqString.append("_").append(numPeriods);
		reqString.append("_").append(periodUnit);

		File cacheFile = new File(reqString.toString());
		OHLC[] data = null;

		if (cacheFile.canRead()) {
			try {
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(cacheFile));
				data = (OHLC[])in.readObject();
				in.close();
				return data;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (data == null) {
			if (historicalDataSource == null)
				throw new DataUnavailableException("Cached data not found and live data source was not set");
			data = historicalDataSource.getHistoricalData(contract, endDate, numPeriods, periodUnit);
			if (data != null) {
				try {
					ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(cacheFile));
					out.writeObject(data);
					out.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return data;
	}

	public void close() throws IOException {
		if (historicalDataSource != null)
			historicalDataSource.close();
	}

}
