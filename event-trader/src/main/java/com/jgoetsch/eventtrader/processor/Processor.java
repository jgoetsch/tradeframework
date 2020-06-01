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
package com.jgoetsch.eventtrader.processor;

import com.jgoetsch.eventtrader.Msg;

public interface Processor<M extends Msg> {

	/**
	 * Implemented to perform processing tasks for a message.
	 * 
	 * @param msg
	 *            The message to process.
	 * @param context
	 *            A map object that can be used to store context data for the
	 *            processing of a message. The same map object will be passed to
	 *            every processor in the chain through the processing lifetime
	 *            of a message, so this object can be used to cache the results
	 *            of data requests so that multiple processors need not make
	 *            redundant requests.
	 * @throws Exception
	 */
	public void process(M msg, ProcessorContext context) throws Exception;

}
