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
package com.jgoetsch.tradeframework.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import com.jgoetsch.tradeframework.Contract;

public interface AccountData {
	
	public BigDecimal getNetLiquidationValue();
	
	public BigDecimal getCashBalance();

	public BigDecimal getValue(String valueType);

	public Map<Contract, ? extends Position> getPositions();

	public String getSummaryText();
	
	public Instant getTimestamp();

}
