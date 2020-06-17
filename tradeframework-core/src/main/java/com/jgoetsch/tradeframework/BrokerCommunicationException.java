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

/**
 * Exception indicating failure to send a request or receive the expected response from the
 * broker service, for instance from an underlying IOException.
 * @author jgoetsch
 *
 */
public class BrokerCommunicationException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public BrokerCommunicationException() {
	}
	
	public BrokerCommunicationException(String detail) {
		super(detail);
	}

	public BrokerCommunicationException(Throwable cause) {
		super(cause);
	}

	public BrokerCommunicationException(String detail, Throwable cause) {
		super(detail, cause);
	}

}
