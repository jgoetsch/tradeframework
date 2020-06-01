package com.jgoetsch.ib;

public class TWSException extends Exception {

	private static final long serialVersionUID = 1L;
	private int errorCode;

	public TWSException(int errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}
	
	public TWSException(int errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}
	
	public int getErrorCode() {
		return errorCode;
	}
}
