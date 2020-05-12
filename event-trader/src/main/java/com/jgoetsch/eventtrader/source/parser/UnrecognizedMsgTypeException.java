package com.jgoetsch.eventtrader.source.parser;

public class UnrecognizedMsgTypeException extends MsgParseException {

	private static final long serialVersionUID = 1L;

	private final String typeId;

	public UnrecognizedMsgTypeException(String message, String typeId) {
		super(message);
		this.typeId = typeId;
	}

	public UnrecognizedMsgTypeException(String typeId, Throwable cause) {
		this(cause.getMessage(), typeId, cause);
	}

	public UnrecognizedMsgTypeException(String message, String typeId, Throwable cause) {
		super(message, cause);
		this.typeId = typeId;
	}

	public String getTypeId() {
		return typeId;
	}
}
