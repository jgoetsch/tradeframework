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

import com.ib.client.Contract;
import com.ib.client.Execution;
import com.jgoetsch.ib.TWSUtils;


public class ConsoleOutputHandler extends BaseHandler<Void> {

	@Override
	public void nextValidId(int orderId) {
		System.out.println("Initial order id = " + orderId);
	}

	/*
	public void updateAccountValue(String key, String value, String currency, String accountName) {
		System.out.println(accountName + ": " + key + " = " + value + " " + currency);
	}
	*/
	public void execDetails(int orderId, Contract contract, Execution execution) {
		System.out.println(TWSUtils.fromTWSExecution(execution) + " " + TWSUtils.fromTWSContract(contract));
		System.out.println(execution.time());
	}

	public void error(Exception e) {
		e.printStackTrace();
	}

	public void error(String str) {
		System.err.println(str);
	}

	public void error(int id, int errorCode, String errorMsg) {
		System.err.println("{id:" + id + ", code:" + errorCode + "} " + errorMsg);
	}

	public void connectionClosed() {
		System.err.println("Connection to TWS was lost!");
		
	}

}
