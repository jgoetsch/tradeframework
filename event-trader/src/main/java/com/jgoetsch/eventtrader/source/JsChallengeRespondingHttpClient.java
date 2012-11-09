package com.jgoetsch.eventtrader.source;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsChallengeRespondingHttpClient implements HttpClient {
	private Logger log = LoggerFactory.getLogger(JsChallengeRespondingHttpClient.class);

    private final HttpClient backend;

    public JsChallengeRespondingHttpClient() {
    	this(null);
    }

    public JsChallengeRespondingHttpClient(HttpClient client) {
		super();
        if (client == null) {
            this.backend = new DefaultHttpClient();
            ((DefaultHttpClient)backend).setRedirectStrategy(new LaxRedirectStrategy());
        }
        else
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
        HttpResponse response = backend.execute(target, request, context);
        if (response.getStatusLine().getStatusCode() == 503) {
        	String content = EntityUtils.toString(response.getEntity());
        	Matcher tokenMatch = Pattern.compile("name=\"jschl_vc\" value=\"(\\w*)\"").matcher(content);
        	if (tokenMatch.find()) {
        		String jschl = tokenMatch.group(1);
        		Matcher exprMatch = Pattern.compile("\\$\\('#jschl_answer'\\).val\\((\\d+)\\+(\\d+)\\*(\\d+)\\);").matcher(content);
        		if (exprMatch.find()) {
        			log.debug("Received challenge expression {}, jschl_vc={}", exprMatch.group(0), jschl);
        			try {
						Thread.sleep(5850);
					} catch (InterruptedException e) {}

        			long answer = Long.valueOf(exprMatch.group(1)) + Long.valueOf(exprMatch.group(2)) * Long.valueOf(exprMatch.group(3));
        			HttpPost chlResponseReq = new HttpPost(target.toURI());
    				try {
    					List<NameValuePair> params = new ArrayList<NameValuePair>();
    					params.add(new BasicNameValuePair("act", "jschl"));
    					params.add(new BasicNameValuePair("jschl_vc", jschl));
    					params.add(new BasicNameValuePair("jschl_answer", String.valueOf(answer)));
    					UrlEncodedFormEntity reqEntity = new UrlEncodedFormEntity(params, "UTF-8");
    					chlResponseReq.setEntity(reqEntity);
    				} catch (UnsupportedEncodingException e) {
    					log.error("Error encoding request entity", e);
    				}
    				response = backend.execute(target, chlResponseReq, context);
        		}
        	}
        }
        return response;
    }

    public ClientConnectionManager getConnectionManager() {
        return backend.getConnectionManager();
    }

    public HttpParams getParams() {
        return backend.getParams();
    }

}
