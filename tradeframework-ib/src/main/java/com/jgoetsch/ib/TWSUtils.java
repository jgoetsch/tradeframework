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
package com.jgoetsch.ib;

import com.jgoetsch.tradeframework.Contract;
import com.jgoetsch.tradeframework.ContractDetails;
import com.jgoetsch.tradeframework.Order;
import com.jgoetsch.tradeframework.Execution;

/**
 * Provides static methods to convert Interactive Brokers TWS specific data
 * objects to and from the equivilent TradeFramework data objects.
 * 
 * @author jgoetsch
 * 
 */
public class TWSUtils {

	private TWSUtils() {
	}

	/**
	 * Convert a TradeFramework contract object into an IB contract object.
	 * @param contract <code>com.jgoetsch.tradeframework.Contract</code> object
	 * @return equivilent object of type <code>com.ib.client.Contract</code>
	 */
	public static com.ib.client.Contract toTWSContract(Contract contract) {
		com.ib.client.Contract twsContract = new com.ib.client.Contract();

		if ("Stock".equalsIgnoreCase(contract.getType()))
			twsContract.m_secType = Contract.STOCK;
		else if ("Futures".equalsIgnoreCase(contract.getType()))
			twsContract.m_secType = Contract.FUTURES;
		else if ("Option".equalsIgnoreCase(contract.getType()))
			twsContract.m_secType = Contract.OPTIONS;
		else
			twsContract.m_secType = contract.getType();

		twsContract.m_symbol = contract.getSymbol();
		twsContract.m_exchange = contract.getExchange();
		twsContract.m_currency = contract.getCurrency();
		twsContract.m_expiry = contract.getExpiry();
		if (contract.getMultiplier() > 1)
			twsContract.m_multiplier = Long.toString(contract.getMultiplier());
		return twsContract;
	}

	/**
	 * Convert a IB contract object into a TradeFramework contract object.
	 * @param twsContract <code>com.ib.client.Contract</code> object
	 * @return equivalent object of type <code>com.jgoetsch.tradeframework.Contract</code>
	 */
	public static Contract fromTWSContract(com.ib.client.Contract twsContract) {
		Contract contract = new com.jgoetsch.tradeframework.Contract();
		contract.setType(twsContract.m_secType);
		contract.setSymbol(twsContract.m_symbol);
		contract.setExchange(twsContract.m_exchange);
		contract.setCurrency(twsContract.m_currency);
		contract.setExpiry(twsContract.m_expiry);
		if (twsContract.m_multiplier != null && twsContract.m_multiplier.length() > 0)
			contract.setMultiplier(Long.parseLong(twsContract.m_multiplier));
		return contract;
	}

	/**
	 * Convert a IB contract details object into a TradeFramework contract details object.
	 * @param twsContract <code>com.ib.client.ContractDetails</code> object
	 * @return equivalent object of type <code>com.jgoetsch.tradeframework.ContractDetails</code>
	 */
	public static ContractDetails fromTWSContractDetails(com.ib.client.ContractDetails twsContractDetails) {
		ContractDetails contractDetails = new com.jgoetsch.tradeframework.ContractDetails(fromTWSContract(twsContractDetails.m_summary));
		contractDetails.setMarketName(twsContractDetails.m_marketName);
		contractDetails.setTradingClass(twsContractDetails.m_tradingClass);
		contractDetails.setMinTick(twsContractDetails.m_minTick);
		contractDetails.setPriceMagnifier(twsContractDetails.m_priceMagnifier);
		contractDetails.setOrderTypes(twsContractDetails.m_orderTypes);
		contractDetails.setValidExchanges(twsContractDetails.m_validExchanges);
		contractDetails.setUnderConId(twsContractDetails.m_underConId);
		contractDetails.setLongName(twsContractDetails.m_longName);
		return contractDetails;
	}

	/**
	 * Convert a TradeFramework order object into an IB order object.
	 * @param order <code>com.jgoetsch.tradeframework.Order</code> object
	 * @return equivilent object of type <code>com.ib.client.Order</code>
	 */
	public static com.ib.client.Order toTWSOrder(Order order) {
		com.ib.client.Order twsOrder = new com.ib.client.Order();
		twsOrder.m_action = order.getQuantity() > 0 ? "BUY" : "SELL";
		twsOrder.m_totalQuantity = Math.abs(order.getQuantity());
		twsOrder.m_orderType = order.getType();
		twsOrder.m_tif = order.getTimeInForce();
		twsOrder.m_lmtPrice = order.getLimitPrice();
		twsOrder.m_auxPrice = order.getAuxPrice();
		twsOrder.m_trailStopPrice = order.getTrailStopPrice();
		twsOrder.m_transmit = order.isTransmit();
		twsOrder.m_account = order.getAccount();
		return twsOrder;
	}

	/**
	 * Convert a IB order object into a TradeFramework order object.
	 * @param twsOrder <code>com.ib.client.Order</code> object
	 * @return equivalent object of type <code>com.jgoetsch.tradeframework.Order</code>
	 */
	public static Order fromTWSOrder(com.ib.client.Order twsOrder) {
		Order order = new Order();
		order.setType(twsOrder.m_orderType);
		order.setQuantity("SELL".equalsIgnoreCase(twsOrder.m_action) ? -twsOrder.m_totalQuantity : twsOrder.m_totalQuantity);
		order.setTimeInForce(twsOrder.m_tif);
		order.setLimitPrice(twsOrder.m_lmtPrice);
		order.setAuxPrice(twsOrder.m_auxPrice);
		order.setTrailStopPrice(twsOrder.m_trailStopPrice);
		order.setAccount(twsOrder.m_account);
		order.setTransmit(twsOrder.m_transmit);
		return order;
	}

	/**
	 * Convert a IB execution object into a TradeFramework execution object.
	 * @param twsExecution <code>com.ib.client.Execution</code> object
	 * @return equivilent object of type <code>com.jgoetsch.tradeframework.Execution</code>
	 */
	public static Execution fromTWSExecution(com.ib.client.Execution twsExecution) {
		Execution execution = new Execution();
		execution.setQuantity("SLD".equalsIgnoreCase(twsExecution.m_side) ? -twsExecution.m_shares : twsExecution.m_shares);
		execution.setPrice(twsExecution.m_price);
		return execution;
	}

}
