package com.jgoetsch.tradeframework.etrade;

import java.io.StringReader;
import java.util.Collections;

import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.util.GenericData;
import com.jgoetsch.tradeframework.BrokerCommunicationException;
import com.jgoetsch.tradeframework.BrokerResponseException;

public class ErrorResponseException extends BrokerResponseException {
	private static final long serialVersionUID = 1L;

	private int statusCode;

	public ErrorResponseException(int statusCode, int code, String message, Throwable cause) {
		super(code, message, cause);
	}

	public int getStatusCode() {
		return statusCode;
	}

	public static BrokerCommunicationException create(JsonFactory jsonFactory, Throwable cause) {
		if (cause instanceof HttpResponseException) {
			HttpResponseException ex = (HttpResponseException)cause;
			try {
				if (ex.getHeaders().getContentType().startsWith("application/json")) {
					GenericData error = new JsonObjectParser.Builder(jsonFactory)
							.setWrapperKeys(Collections.singleton("Error")).build()
							.parseAndClose(new StringReader(ex.getContent()), GenericData.class);
					return new ErrorResponseException(ex.getStatusCode(), ((Number)error.get("code")).intValue(), (String)error.get("message"), ex);
				} else if (ex.getStatusCode() == HttpStatusCodes.STATUS_CODE_UNAUTHORIZED) {
					return new AuthenticationFailureException(ex);
				}
			} catch (Exception e) {
				return new BrokerCommunicationException("Exception parsing error response: " + e.toString(), cause);
			}
		}
		return new BrokerCommunicationException(cause);
	}

}
