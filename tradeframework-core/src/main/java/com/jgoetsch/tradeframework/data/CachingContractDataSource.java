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

import com.jgoetsch.tradeframework.Contract;
import com.jgoetsch.tradeframework.InvalidContractException;

public class CachingContractDataSource<T> implements ContractDataSource<T> {

	private ContractDataSource<T> baseDataSource;
	private final String cacheLocation;

	public CachingContractDataSource(ContractDataSource<T> baseDataSource) {
		this.baseDataSource = baseDataSource;
		this.cacheLocation = "contractdata/";
	}

	public T getDataSnapshot(Contract contract) throws IOException, InvalidContractException, DataUnavailableException {
		StringBuilder reqString = new StringBuilder(cacheLocation);
		reqString.append(contract.getType());
		reqString.append("_").append(contract.getSymbol());

		File cacheFile = new File(reqString.toString());
		T data = null;
		if (cacheFile.canRead()) {
			try {
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(cacheFile));
				data = (T)in.readObject();
				in.close();
				return data;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (data == null) {
			if (baseDataSource == null)
				throw new DataUnavailableException("Cached data not found and live data source was not set");
			data = baseDataSource.getDataSnapshot(contract);
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
		baseDataSource.close();
	}

}
