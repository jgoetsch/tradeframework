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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.keyvalue.MultiKey;
import org.apache.commons.collections15.map.HashedMap;
import org.apache.commons.collections15.map.MultiKeyMap;
import org.apache.commons.collections15.multimap.MultiHashMap;

import com.ib.client.EWrapper;

/**
 * Handler manager implementation that supports only a single handler per event/objectId combo.
 * 
 * @author jgoetsch
 *
 */
public class SingleHandlerManager implements HandlerManager {

	private MultiKeyMap<Object, EWrapper> handlerMap;
	private MultiMap<EWrapper, MultiKey<Object>> handlerReverseMap;

	public SingleHandlerManager() {
		handlerMap = MultiKeyMap.decorate(new HashedMap<MultiKey<Object>, EWrapper>());
		handlerReverseMap = new MultiHashMap<EWrapper, MultiKey<Object>>();
	}

	public synchronized void addHandler(String eventName, int objectId, EWrapper handler) {
		if (handler == null)
			handlerMap.remove(eventName, objectId);
		else {
			handlerMap.put(eventName, objectId, handler);
			handlerReverseMap.put(handler, new MultiKey<Object>(eventName, objectId));
		}
	}

	public void addHandler(String eventName, EWrapper handler) {
		addHandler(eventName, -1, handler);
	}

	public void addHandler(String eventNames[], int objectId, EWrapper handler) {
		for (String eventName : eventNames) {
			addHandler(eventName, objectId, handler);
		}
	}

	public void addHandler(String eventNames[], EWrapper handler) {
		addHandler(eventNames, -1, handler);
	}

	public void addHandler(EWrapper handler) {
		throw new UnsupportedOperationException();
	}

	public synchronized void removeHandler(EWrapper handler) {
		Collection<MultiKey<Object>> events = handlerReverseMap.get(handler);
		for (MultiKey<Object> event : events) {
			handlerMap.remove(event);
		}
		handlerReverseMap.remove(handler);
	}

	public Collection<EWrapper> getHandlers(String eventName, int objectId) {
		EWrapper handler, noObjectHandler;
		synchronized (this) {
			handler = handlerMap.get(eventName, objectId);
			noObjectHandler = handlerMap.get(eventName, 0);
		}
		if (handler != null && noObjectHandler != null && handler != noObjectHandler)
			return Arrays.asList(handler, noObjectHandler);
		else if (handler != null)
			return Collections.singleton(handler);
		else if (noObjectHandler != null)
			return Collections.singleton(noObjectHandler);
		else
			return Collections.emptySet();
	}

	public Collection<EWrapper> getHandlers(String eventName) {
		return getHandlers(eventName, 0);
	}

	@SuppressWarnings("unchecked")
	public synchronized Collection<EWrapper> getHandlers() {
		return new HashSet<EWrapper>(handlerMap.values());
	}

}
