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
package com.jgoetsch.eventtrader.filter;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.processor.PropagatingProcessor;

/**
 * Base class to implement filtering processors, that is processors that
 * conditionally block or pass through incoming messages to one or more
 * child processors.
 * 
 * @author jgoetsch
 *
 * @param <M> incoming message class
 */
public abstract class FilterProcessor<M extends Msg> extends PropagatingProcessor<M, M> {

	private Logger log = LoggerFactory.getLogger(getClass());
	private boolean disabled = false;
	private boolean inverse = false;

	public final void process(M msg, Map<Object,Object> context) throws Exception {
		if (disabled || handleProcessing(msg, context) != inverse) {
			super.process(msg, context);
		}
		else if (log.isDebugEnabled()) {
			log.debug("Execution blocked of trade " + msg);
		}
	}
	
	protected abstract boolean handleProcessing(M msg, Map<Object,Object> context) throws Exception;

	/**
	 * Disabled or enables the filter. If disabled, the filter will pass all
	 * messages through to its child processors (as if it were not there). This
	 * can be useful for debugging.
	 * 
	 * @param disabled true to disable filter, false to enable
	 */
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	/**
	 * Returns the disabled status of the filter. If disabled, the filter will
	 * pass all messages through to its child processors (as if it were not
	 * there). This can be useful for debugging.
	 * 
	 * @return true if disabled, false if enabled
	 */
	public boolean isDisabled() {
		return disabled;
	}

	/**
	 * Sets whether the filter should be inverted. If the filter is inverted,
	 * it will do the opposite of what it would normally do.
	 * 
	 * @param inverse true to invert filter, false for normal operation
	 */
	public void setInverse(boolean inverse) {
		this.inverse = inverse;
	}

	/**
	 * Returns the inverted status of the filter. If the filter is inverted,
	 * it will do the opposite of what it would normally do.
	 * 
	 * @return true if inverted, false if not inverted
	 */
	public boolean isInverse() {
		return inverse;
	}

}
