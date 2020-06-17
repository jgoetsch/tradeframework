package com.jgoetsch.tradeframework;

/**
 * Exception indicating the underlying broker service returned a failure code in response
 * to a requested action
 * @author jgoetsch
 *
 */
public class BrokerResponseException extends BrokerCommunicationException {

	private static final long serialVersionUID = 1L;
	private Integer code;

	public BrokerResponseException(String message) {
		super(message);
	}

	public BrokerResponseException(Throwable cause) {
		super(cause);
	}

	public BrokerResponseException(int code, String message) {
		super(message);
		this.code = code;
	}
	
	public BrokerResponseException(int code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}
	
	public Integer getCode() {
		return code;
	}
}
