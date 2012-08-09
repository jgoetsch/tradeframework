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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.jgoetsch.eventtrader.Msg;

/**
 * Base class for a processor that can propagate incoming messages onto one or
 * more child processors. Used to filter messages or parse Msg objects into
 * TradeSignals.
 * 
 * @author jgoetsch
 * 
 * @param <M>
 *            incoming message class
 * @param <P>
 *            outgoing message class (child processors receive messages of this
 *            class)
 */
public abstract class PropagatingProcessor<M extends Msg, P extends Msg> implements Processor<M> {

	private Collection<Processor<P>> processors;

	public void process(M msg, Map<Object,Object> context) throws Exception {
		if (processors != null) {
			for (Processor<P> p : processors)
				p.process((P)msg, context);
		}
	}

	public final void setProcessors(Collection<Processor<P>> processors) {
		this.processors = processors;
	}

	public final Collection<Processor<P>> getProcessors() {
		return processors;
	}

	public final void setProcessor(Processor<P> processor) {
		this.processors = Collections.singletonList(processor);
	}

	public final Processor<P> getProcessor() {
		return processors == null || processors.size() == 0 ? null : processors.iterator().next();
	}

}
