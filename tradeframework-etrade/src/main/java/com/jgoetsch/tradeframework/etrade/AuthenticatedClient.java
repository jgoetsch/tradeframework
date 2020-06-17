package com.jgoetsch.tradeframework.etrade;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface AuthenticatedClient {

	void executeAuthenticationFlow();

	<T> CompletableFuture<T> doGet(Class<T> responseType, String path, Map<String, Object> params);

	<T> CompletableFuture<T> doPost(Object body, Class<T> responseType, String path, Map<String, Object> params);

	String getBaseUrl();
}