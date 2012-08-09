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


/**
 * Handler that waits for a nextValidId callback and stores the order id
 * passed in. To be used after first connecting to TWS to get the order id
 * to start with.
 * 
 * @author jgoetsch
 *
 */
public class NextValidIdHandler extends BaseHandler {
	
	private int id = -1;
	
	public int getId() {
		return id;
	}

	/**
	 * Returns success when the order id has been received.
	 */
	@Override
	public int getStatus() {
		if (id != -1)
			return STATUS_SUCCESS;
		else
			return super.getStatus();
	}

	@Override
	public synchronized void nextValidId(int orderId) {
		this.id = orderId;
		this.notifyAll();
	}

}
