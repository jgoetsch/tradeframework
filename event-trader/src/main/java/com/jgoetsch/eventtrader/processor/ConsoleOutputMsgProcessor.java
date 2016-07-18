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

import java.io.PrintStream;
import java.util.Map;

import com.jgoetsch.eventtrader.Msg;

public class ConsoleOutputMsgProcessor<M extends Msg> implements Processor<M> {

	private final PrintStream out;
	private final String format = null;

	public ConsoleOutputMsgProcessor(PrintStream out) {
		this.out = out;
	}

	public void process(M msg, Map<Object,Object> context) throws Exception {
		if (format == null)
			out.println(msg);
	}

}
