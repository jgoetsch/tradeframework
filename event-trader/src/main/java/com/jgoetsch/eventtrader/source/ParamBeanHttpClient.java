package com.jgoetsch.eventtrader.source;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpConnectionParamBean;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParamBean;
import org.apache.http.protocol.HttpContext;

public class ParamBeanHttpClient implements HttpClient {
	private final HttpClient backend;

    public ParamBeanHttpClient(HttpClient client) {
		super();
        if (client == null)
            throw new IllegalArgumentException("client must not be null");
        this.backend = client;
	}

	public HttpResponse execute(HttpHost target, HttpRequest request)
            throws IOException {
        HttpContext defaultContext = null;
        return execute(target, request, defaultContext);
    }

    public <T> T execute(HttpHost target, HttpRequest request,
            ResponseHandler<? extends T> responseHandler) throws IOException {
        return execute(target, request, responseHandler, null);
    }

    public <T> T execute(HttpHost target, HttpRequest request,
            ResponseHandler<? extends T> responseHandler, HttpContext context)
            throws IOException {
        HttpResponse resp = execute(target, request, context);
        return responseHandler.handleResponse(resp);
    }

    public HttpResponse execute(HttpUriRequest request) throws IOException {
        HttpContext context = null;
        return execute(request, context);
    }

    public HttpResponse execute(HttpUriRequest request, HttpContext context)
            throws IOException {
        URI uri = request.getURI();
        HttpHost httpHost = new HttpHost(uri.getHost(), uri.getPort(),
                uri.getScheme());
        return execute(httpHost, request, context);
    }

    public <T> T execute(HttpUriRequest request,
            ResponseHandler<? extends T> responseHandler) throws IOException {
        return execute(request, responseHandler, null);
    }

    public <T> T execute(HttpUriRequest request,
            ResponseHandler<? extends T> responseHandler, HttpContext context)
            throws IOException {
        HttpResponse resp = execute(request, context);
        return responseHandler.handleResponse(resp);
    }

    public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) 
    		throws IOException
    {
        return backend.execute(target, request, context);
    }


    public ClientConnectionManager getConnectionManager() {
        return backend.getConnectionManager();
    }

    public HttpParams getParams() {
        return backend.getParams();
    }

	public HttpProtocolParamBean getProtocolParams() {
		return new HttpProtocolParamBean(getParams());
	}

	public HttpConnectionParamBean getConnectionParams() {
		return new HttpConnectionParamBean(getParams());
	}

	public HttpClient getBackend() {
		return backend;
	}

}
