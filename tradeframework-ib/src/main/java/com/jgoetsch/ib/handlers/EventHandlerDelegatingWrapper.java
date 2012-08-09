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
package com.jgoetsch.ib.handlers;

import com.ib.client.EWrapper;

/**
 * Delegating wrapper that routes events only to the handlers registered for the specific
 * event and request/order id using a HandlerManager.
 * 
 * @author jgoetsch
 *
 */
public class EventHandlerDelegatingWrapper extends HandlerDelegatingWrapper {

	private HandlerManager handlerManager;
	
	public EventHandlerDelegatingWrapper(HandlerManager handlerManager) {
		this.handlerManager = handlerManager;
	}

	@Override
	protected void callHandlers(String eventName, int objId, HandlerCallback callback)
	{
		for (EWrapper handler : handlerManager.getHandlers(eventName, objId))
			callback.callHandler(handler);
	}

	/**
	 * Connection closed event gets sent to all registered handlers
	 */
	public void connectionClosed() {
		for (EWrapper handler : handlerManager.getHandlers()) {
			handler.connectionClosed();
		}
	}

	/**
	 * Errors are routed based on errorCode and id
	 */
	public void error(int id, int errorCode, String errorMsg) {
		for (EWrapper handler : handlerManager.getHandlers("error"))
			handler.error(id, errorCode, errorMsg);
		if (errorCode >= 501 && errorCode <= 503)
			for (EWrapper handler : handlerManager.getHandlers("nextValidId"))
				handler.error(id, errorCode, errorMsg);
	}

}
