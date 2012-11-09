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
package com.jgoetsch.eventtrader.source;

import java.net.SocketTimeoutException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.eventtrader.source.parser.MsgParser;

public class HttpStreamingMsgSource extends AbstractHttpMsgSource {

	private Logger log = LoggerFactory.getLogger(HttpStreamingMsgSource.class);
	private MsgParser msgParser;

	public void receiveMsgs(HttpClient client) {
		log.info("receiveMsgss");
		while (executeRequest(client, createRequest(), getMsgParser()) && afterDisconnect()) {}
	}

	protected boolean executeRequest(HttpClient client, HttpUriRequest request, MsgParser msgParser) {
		HttpEntity entity = null;
		try {
			HttpResponse rsp = client.execute(request);
			entity = rsp.getEntity();
			if (rsp.getStatusLine().getStatusCode() >= 400 || entity == null) {
				log.warn("HTTP request to " + request.getURI() + " failed with status " + rsp.getStatusLine());

				// if 400 level error don't keep trying
				return (rsp.getStatusLine().getStatusCode() >= 500);
			}
			else {
				log.info("Connected to streaming source " + request.getURI().getHost() + ", waiting for messages");
				return msgParser.parseContent(entity.getContent(), -1, entity.getContentType().getValue(), this);
			}
		} catch (SocketTimeoutException e) {
			return true;
		} catch (Exception e) {
			log.warn("Exception reading or parsing message source", e);
			return false;
		}
		finally {
			request.abort();
		}
	}

	protected boolean afterDisconnect() {
		log.info("Disconnected from streaming source, waiting to reconnect...");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) { }
		return true;
	}

	public void setMsgParser(MsgParser msgParser) {
		this.msgParser = msgParser;
	}

	public MsgParser getMsgParser() {
		return msgParser;
	}

}
