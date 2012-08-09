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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.jgoetsch.eventtrader.Msg;

/**
 * Filters out all Msgs not from the given usernames.
 * 
 * @author jgoetsch
 *
 */
public class UsernameFilter extends FilterProcessor<Msg> {

	private Set<String> usernames;

	@Override
	protected boolean handleProcessing(Msg trade, Map<Object,Object> context) {
		return usernames.contains(trade.getSourceName().toUpperCase());
	}

	public void setUsernames(Collection<String> usernames) {
		this.usernames = new HashSet<String>(usernames.size());
		for (String username : usernames)
			this.usernames.add(username.trim().toUpperCase());
	}

	public Collection<String> getUsernames() {
		return usernames;
	}

}
