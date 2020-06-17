package com.jgoetsch.tradeframework.etrade;

import java.awt.Desktop;
import java.io.Console;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthGetAccessToken;
import com.google.api.client.auth.oauth.OAuthGetTemporaryToken;
import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.auth.oauth.OAuthSigner;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.jgoetsch.tradeframework.BrokerCommunicationException;

public class EtradeOAuthClient implements AuthenticatedClient {
	private Logger log = LoggerFactory.getLogger(EtradeOAuthClient.class);

	private String consumerKey;
	private String consumerSecret;
	private String baseUrl = EtradeApiConstants.SANDBOX_BASE;
	private String authorizationUrl = EtradeApiConstants.AUTHORIZATION_URL;

	private String accessToken;
	private String tokenSecret;

	private final HttpTransport transport;
	private JsonFactory jsonFactory = new JacksonFactory();

	public EtradeOAuthClient() {
		this.transport = new ApacheHttpTransport();
	}

	public EtradeOAuthClient(HttpTransport transport) {
		this.transport = transport;
	}

	public EtradeOAuthClient(String consumerKey, String consumerSecret) {
		this();
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
	}

	@Override
	public void executeAuthenticationFlow() {
		try {
			OAuthCredentialsResponse requestCredential = getRequestToken();
			Desktop.getDesktop().browse(URI.create(String.format("%s?key=%s&token=%s", authorizationUrl, consumerKey, requestCredential.token)));
			OAuthCredentialsResponse accessCredential = getAccessToken(requestCredential.token, requestCredential.tokenSecret, inputVerificationCode());
			setAccessTokens(accessCredential.token, accessCredential.tokenSecret);
		} catch (IOException e) {
			log.error("I/O operation failed during authentication flow", e);
			throw new RuntimeException("Failed to execute OAuth authentication flow", e);
		}
	}

	public void renewAccessToken() throws IOException {
		try {
			String response = getRequestFactory()
					.buildGetRequest(new GenericUrl(getBaseUrl() + "oauth/renew_access_token"))
					.execute().parseAsString();
			log.debug("Renew token: {}", response);
		}
		catch (HttpResponseException e) {
			log.debug("Renew token failed", e);
			throw ErrorResponseException.create(jsonFactory, e);
		}
	}

	public <T> CompletableFuture<T> doGet(Class<T> responseType, String path, Map<String, Object> params) {
		return doRequest(responseType, "GET", path, params, null);
	}

	public <T> CompletableFuture<T> doPost(Object body, Class<T> responseType, String path, Map<String, Object> params) {
		return doRequest(responseType, "POST", path, params, body);
	}

	protected <T> CompletableFuture<T> doRequest(Class<T> responseType, String method, String path, Map<String, Object> params, Object body) {
		GenericUrl url = new GenericUrl(getBaseUrl() + path);
		if (params != null)
			url.putAll(params);
		log.debug("{} {}", method, url.build());

		HttpRequest request;
		try {
			request = getRequestFactory().buildRequest(method, url, null);
		} catch (IOException ex) {
			throw new BrokerCommunicationException(ex);
		}
		request.getHeaders().setAccept("application/json");
		request.setParser(new JsonObjectParser.Builder(jsonFactory)
				.setWrapperKeys(Collections.singleton(responseType.getSimpleName()))
				.build());
		if (body != null) {
			JsonHttpContent content = new JsonHttpContent(jsonFactory, body);
			content.setWrapperKey(body.getClass().getSimpleName());
			request.setContent(content);
		}

		return CompletableFuture.supplyAsync(() -> {
			try {
				return request.execute().parseAs(responseType);
			}
			catch (HttpResponseException e) {
				throw ErrorResponseException.create(jsonFactory, e);
			}
			catch (IOException e) {
				throw new BrokerCommunicationException("Network i/o failure", e);
			}
		});
	}

	protected void setAccessTokens(String token, String tokenSecret) {
		this.accessToken = token;
		this.tokenSecret = tokenSecret;
		if (log.isDebugEnabled())
			log.debug("Using access token {}, secret: {}", scrubToken(this.accessToken), scrubToken(this.tokenSecret));
	}

	protected HttpRequestFactory getRequestFactory() {
		OAuthParameters params = new OAuthParameters();
		params.consumerKey = consumerKey;
		if (accessToken != null && tokenSecret != null) {
			params.token = accessToken;
			params.signer = createOauthSigner(tokenSecret);
		}
		else
			throw new IllegalStateException("Access token and secret not available, must authenticate first");

		return transport.createRequestFactory(params);
	}

	protected OAuthSigner createOauthSigner(String tokenSecret) {
		OAuthHmacSigner oauthSigner = new OAuthHmacSigner();
		oauthSigner.clientSharedSecret = consumerSecret;
		oauthSigner.tokenSharedSecret = tokenSecret;
		return oauthSigner;
	}

	protected String inputVerificationCode() {
		Console console = System.console();
		if (console == null)
			throw new UnsupportedOperationException("Console input is not available to receive Oauth verification code");
		return new String(console.readPassword("Enter verification code: "));
	}

	private OAuthCredentialsResponse getRequestToken() throws IOException {
		OAuthGetTemporaryToken tempTokenRequest = new OAuthGetTemporaryToken(getBaseUrl() + "oauth/request_token");
		tempTokenRequest.consumerKey = consumerKey;
		tempTokenRequest.transport = transport;
		tempTokenRequest.signer = createOauthSigner(null);
		tempTokenRequest.callback = "oob";
		OAuthCredentialsResponse requestCredential = tempTokenRequest.execute();
		if (log.isDebugEnabled())
			log.debug("Got temp request token {}, secret: {}", scrubToken(requestCredential.token), scrubToken(requestCredential.tokenSecret));
		return requestCredential;
	}
	
	protected String scrubToken(String token) {
		int len = Math.min(4, token.length());
		return token.substring(0, len) + "*".repeat(token.length() - len);
	}

	private OAuthCredentialsResponse getAccessToken(String requestToken, String tokenSecret, String verificationCode) throws IOException {
		OAuthGetAccessToken accessTokenRequest = new OAuthGetAccessToken(getBaseUrl() + "oauth/access_token");
		accessTokenRequest.signer = createOauthSigner(tokenSecret);
		accessTokenRequest.consumerKey = consumerKey;
		accessTokenRequest.transport = transport;
		accessTokenRequest.temporaryToken = requestToken;
		accessTokenRequest.verifier = verificationCode;
		return accessTokenRequest.execute();
	}

	public String getConsumerKey() {
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public String getConsumerSecret() {
		return consumerSecret;
	}

	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getAuthorizationUrl() {
		return authorizationUrl;
	}

	public void setAuthorizationUrl(String authorizationUrl) {
		this.authorizationUrl = authorizationUrl;
	}

}
