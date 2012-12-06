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

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.source.parser.MsgParser;

public class HttpPollingMsgSource extends AbstractHttpMsgSource {

	private Logger log = LoggerFactory.getLogger(HttpPollingMsgSource.class);
	private long pollingInterval = 60000;
	private long freshnessThreshold = 30000;
	private MsgParser msgParser;
	private boolean useIfModifiedSince = false;
	private boolean alertInitial = false;

	private String lastModifiedDate;

	private class NewMsgHandler implements MsgHandler {
		private Set<Msg> lastMsgs;
		private Set<Msg> msgs = new HashSet<Msg>();
		private Date lastCheckAt;

		public boolean newMsg(Msg msg) {
			msgs.add(msg);
			if (lastCheckAt != null && new Date().getTime() - lastCheckAt.getTime() > pollingInterval + freshnessThreshold) {
				if (lastMsgs != null)
					lastMsgs.clear();
				lastMsgs = null;
			}
			if (lastMsgs == null) {
				log.debug("Previous message: {}", msg);
			}
			if ((lastMsgs != null && !lastMsgs.contains(msg)) || (lastMsgs == null && alertInitial)) {
				if (!HttpPollingMsgSource.this.newMsg(msg))
					return false;
			}
			return true;
		}

		public void nextPass() {
			if (lastMsgs != null)
				lastMsgs.clear();
			lastMsgs = msgs;
			msgs = new HashSet<Msg>(lastMsgs.size() + 4);
			lastCheckAt = new Date();
		}
	};

	public void receiveMsgs(HttpClient client)
	{
		NewMsgHandler msgHandler = new NewMsgHandler();
		HttpUriRequest req = createRequest();
		for(;;) {
			HttpEntity entity = null;
			try {
				if (isUseIfModifiedSince() && lastModifiedDate != null)
					req.setHeader("If-Modified-Since", lastModifiedDate);

				long startTime = System.currentTimeMillis();
				HttpResponse rsp = client.execute(req);
				if (rsp.containsHeader("Last-Modified")) {
					lastModifiedDate = rsp.getFirstHeader("Last-Modified").getValue();
					//log.debug("Resource last modified: " + lastModifiedDate);
				}
				entity = rsp.getEntity();
				if (rsp.getStatusLine().getStatusCode() >= 400) {
					log.warn("HTTP request to " + req.getURI().getHost() + " failed ["
							+ rsp.getStatusLine().getStatusCode() + " " + rsp.getStatusLine().getReasonPhrase() + ", "
							+ (System.currentTimeMillis() - startTime) + " ms]");

					// 400 level error should be unrecoverable so just quit out
					if (rsp.getStatusLine().getStatusCode() < 500)
						return;
					else {
						// give server some more time to recover before retrying if it returned 500 level error
						// probably means site crashed and continuing to hit it will only make things worse
						try {
							Thread.sleep(pollingInterval * 6);
						} catch (InterruptedException e) { }
					}
				}
				else {
					boolean bContinue = true;
					if (entity != null && rsp.getStatusLine().getStatusCode() != 304) {	// 304 = not modified
						bContinue = getMsgParser().parseContent(entity.getContent(), entity.getContentLength(), entity.getContentType() == null ? null : entity.getContentType().getValue(), msgHandler);
						msgHandler.nextPass();
					}
					if (log.isDebugEnabled()) {
						log.debug("Checked site at " + req.getURI().getHost() + " ["
								+ rsp.getStatusLine().getStatusCode() + " " + rsp.getStatusLine().getReasonPhrase() + ", "
								+ (entity != null ? (entity.getContentLength() != -1 ? entity.getContentLength() + " bytes, " : "unknown length, ") : "")
								+ (System.currentTimeMillis() - startTime) + " ms]");
					}
					if (!bContinue)
						return;
				}
			}
			catch (IOException e) {
				log.warn(e.getClass() + ": " + e.getMessage());
			}
			catch (Exception e) {
				log.warn(e.getClass() + ": " + e.getMessage(), e);
			}
			finally {
				if (entity != null) {
					 // release connection gracefully
					try {
						entity.consumeContent();
					} catch (IOException e) { }
				}
			}

			delay();
		}
	}

	protected void delay() {
		try {
			Thread.sleep(pollingInterval);
		} catch (InterruptedException e) { }
	}

	protected void doLoginAction(HttpClient client) {
	}

	/*
	private LocalTime parseLocalTime(String time) {
		String tok[] = time.split("[\\:\\s]");
		if (tok.length < 3 || tok.length > 4)
			throw new IllegalArgumentException("Time must be in format \"HH:MM:SS [timezone]\"");
		return new LocalTime(Integer.parseInt(tok[0]), Integer.parseInt(tok[1]), Integer.parseInt(tok[2]));
	}
	*/

	public void setPollingInterval(long milliseconds) {
		this.pollingInterval = milliseconds;
	}

	public long getPollingInterval() {
		return pollingInterval;
	}

	public void setPollingIntervalSeconds(long seconds) {
		this.pollingInterval = seconds * 1000;
	}

	public void setPollingIntervalMinutes(long minutes) {
		this.pollingInterval = minutes * 60000;
	}

	public void setMsgParser(MsgParser msgParser) {
		this.msgParser = msgParser;
	}

	public MsgParser getMsgParser() {
		return msgParser;
	}

	public void setUseIfModifiedSince(boolean useIfModifiedSince) {
		this.useIfModifiedSince = useIfModifiedSince;
	}

	public boolean isUseIfModifiedSince() {
		return useIfModifiedSince;
	}

	public void setAlertInitial(boolean alertInitial) {
		this.alertInitial = alertInitial;
	}

	public boolean isAlertInitial() {
		return alertInitial;
	}

	public long getFreshnessThreshold() {
		return freshnessThreshold;
	}

	public void setFreshnessThreshold(long freshnessThreshold) {
		this.freshnessThreshold = freshnessThreshold;
	}

}
