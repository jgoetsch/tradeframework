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

import com.jgoetsch.eventtrader.TradeSignal;

/**
 * Filters TradeSignal messages the given contract types.
 * 
 * @author jgoetsch
 *
 */
public class ContractTypeFilter extends FilterProcessor<TradeSignal> {

	private Set<String> contractTypes;

	@Override
	protected boolean handleProcessing(TradeSignal trade, Map<Object,Object> context) {
		return trade.getContract() != null && contractTypes.contains(trade.getContract().getType());
	}

	/**
	 * Sets the contract types allowed to pass through to the underlying
	 * processors.
	 * 
	 * @param contractTypes
	 *            Collection of strings representing contract types to allow.
	 */
	public void setContractTypes(Collection<String> contractTypes) {
		this.contractTypes = new HashSet<String>(contractTypes.size());
		for (String type : contractTypes)
			this.contractTypes.add(type.trim().toUpperCase());
	}

	/**
	 * Gets the contract types allowed to pass through to the underlying
	 * processors.
	 * 
	 * @return Collection of strings representing contract types to allow.
	 */
	public Collection<String> getContractTypes() {
		return contractTypes;
	}

}
