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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.processor.Processor;

public abstract class MsgSource implements Runnable, MsgHandler {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Collection<? extends Processor<Msg>> processors;
	private int numEvents = -1;
	private String defaultImageURL;
	private Thread mainThread;

	private final BlockingQueue<Msg> msgQueue = new ArrayBlockingQueue<Msg>(16);

	private class MsgProcessingConsumer implements Runnable {
		public void run() {
			for (;;) {
				try {
					Msg msg = msgQueue.take();
					if (msg instanceof ShutdownMsg)
						return;

					log.info("{}", msg);
					if (getProcessors() != null) {
						Map<Object, Object> context = new HashMap<Object, Object>();
						for (Processor<Msg> p : getProcessors()) {
							try {
								p.process(msg, context);
							} catch (Exception e) {
								LoggerFactory.getLogger(p.getClass()).error("Error processing", e);
							}
						}
					}
				} catch (InterruptedException e) {
					return;
				}
			}
		}
	}

	private static class ShutdownMsg extends Msg {
		private static final long serialVersionUID = 1L;
		@Override
		public String toString() {
			return "<shutdown msg>";
		}
	};

	public void run() {
		mainThread = Thread.currentThread();
		Thread t = new Thread(new MsgProcessingConsumer(), Thread.currentThread().getName() + "-p");
		t.start();
		receiveMsgs();
		try {
			msgQueue.put(new ShutdownMsg());
			t.join();
		} catch (InterruptedException e) { }
		log.info("Message source is shutting down...");
	}

	public boolean newMsg(Msg msg) {
		if (msg != null) {
			if (msg.getImageUrl() == null)
				msg.setImageUrl(getDefaultImageURL());
			try {
				msgQueue.put(msg);
			} catch (InterruptedException e) { }
			return (numEvents < 0 || --numEvents > 0);
		}
		else
			return true;
	}

	protected abstract void receiveMsgs();

	/**
	 * Waits for the primary thread of this message source (the thread from which it
	 * was run) to die. Called from shutdown hooks to delay shutdown until resources
	 * have been cleaned up.
	 * 
	 * @param millis - the time to wait in milliseconds
	 */
	protected void joinMainThread(long millis) {
		if (mainThread != null) {
			try {
				mainThread.join(millis);
			} catch (InterruptedException e) {}
		}
	}

	public void setProcessors(Collection<? extends Processor<Msg>> processors) {
		this.processors = processors;
	}

	public Collection<? extends Processor<Msg>> getProcessors() {
		return processors;
	}

	public void setNumEvents(int numEvents) {
		this.numEvents = numEvents;
	}

	public int getNumEvents() {
		return numEvents;
	}

	public void setDefaultImageURL(String defaultImageURL) {
		this.defaultImageURL = defaultImageURL;
	}

	public String getDefaultImageURL() {
		return defaultImageURL;
	}

}
