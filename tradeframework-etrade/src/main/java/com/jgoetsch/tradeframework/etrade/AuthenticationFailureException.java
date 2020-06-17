package com.jgoetsch.tradeframework.etrade;

import com.jgoetsch.tradeframework.BrokerCommunicationException;

public class AuthenticationFailureException extends BrokerCommunicationException {
	private static final long serialVersionUID = 1L;

	public AuthenticationFailureException(Throwable cause) {
		super(cause);
	}

	public AuthenticationFailureException(String message, Throwable cause) {
		super(message, cause);
	}
}
