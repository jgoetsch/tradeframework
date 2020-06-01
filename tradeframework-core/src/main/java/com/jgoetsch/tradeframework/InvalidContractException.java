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

import com.jgoetsch.tradeframework.data.DataUnavailableException;

public class InvalidContractException extends DataUnavailableException {

	private static final long serialVersionUID = 1L;
	private final Contract contract;

	public InvalidContractException(Contract contract) {
		this.contract = contract;
	}

	public InvalidContractException(Contract contract, String message) {
		super(message);
		this.contract = contract;
	}

	@Override
	public String getMessage() {
		String msg = super.getMessage();
		StringBuilder sb = new StringBuilder();
		if (msg != null) {
			sb.append(msg).append(": ");
		}
		sb.append(contract).append(" ").append(contract.getType()).append(" ").append(contract.getCurrency());
		return sb.toString();
	}

	public InvalidContractException(Contract contract, Throwable cause) {
		super(cause);
		this.contract = contract;
	}

	public InvalidContractException(Contract contract, String message, Throwable cause) {
		super(message, cause);
		this.contract = contract;
	}

	public Contract getContract() {
		return contract;
	}
}
