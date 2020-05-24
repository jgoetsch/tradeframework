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

import java.util.Collection;

import com.ib.client.EWrapper;

public interface HandlerManager {

	public void addHandler(String eventName, int objectId, EWrapper handler);

	public void addHandler(String eventName, EWrapper handler);

	public void addHandler(String eventNames[], int objectId, EWrapper handler);

	public void addHandler(String eventNames[], EWrapper handler);
	
	public void addHandler(EWrapper handler);

	public void removeHandler(EWrapper handler);

	public void removeAllHandlers();

	public Collection<EWrapper> getHandlers(String eventName, int objectId);

	public Collection<EWrapper> getHandlers(String eventName);
	
	public Collection<EWrapper> getHandlers();

}