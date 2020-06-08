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

import java.math.BigDecimal;

import com.jgoetsch.tradeframework.Contract;
import com.jgoetsch.tradeframework.ContractDetails;
import com.jgoetsch.tradeframework.StandardOrder;
import com.jgoetsch.tradeframework.Execution;

/**
 * Provides static methods to convert Interactive Brokers TWS specific data
 * objects to and from the equivalent TradeFramework data objects.
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
	 * @return equivalent object of type <code>com.ib.client.Contract</code>
	 */
	public static com.ib.client.Contract toTWSContract(Contract contract) {
		com.ib.client.Contract twsContract = new com.ib.client.Contract();

		if ("Stock".equalsIgnoreCase(contract.getType()))
			twsContract.secType(Contract.STOCK);
		else if ("Futures".equalsIgnoreCase(contract.getType()))
			twsContract.secType(Contract.FUTURES);
		else if ("Option".equalsIgnoreCase(contract.getType()))
			twsContract.secType(Contract.OPTIONS);
		else
			twsContract.secType(contract.getType());

		twsContract.symbol(contract.getSymbol());
		twsContract.exchange(contract.getExchange());
		twsContract.currency(contract.getCurrency());
		twsContract.lastTradeDateOrContractMonth(contract.getExpiry());
		if (contract.getMultiplier() > 1)
			twsContract.multiplier(Long.toString(contract.getMultiplier()));
		return twsContract;
	}

	/**
	 * Convert a IB contract object into a TradeFramework contract object.
	 * @param twsContract <code>com.ib.client.Contract</code> object
	 * @return equivalent object of type <code>com.jgoetsch.tradeframework.Contract</code>
	 */
	public static Contract fromTWSContract(com.ib.client.Contract twsContract) {
		Contract contract = new com.jgoetsch.tradeframework.Contract();
		contract.setType(twsContract.getSecType());
		contract.setSymbol(twsContract.symbol());
		contract.setExchange(twsContract.exchange());
		contract.setCurrency(twsContract.currency());
		contract.setExpiry(twsContract.lastTradeDateOrContractMonth());
		if (twsContract.multiplier() != null && twsContract.multiplier().length() > 0)
			contract.setMultiplier(Long.parseLong(twsContract.multiplier()));
		return contract;
	}

	/**
	 * Convert a IB contract details object into a TradeFramework contract details object.
	 * @param twsContract <code>com.ib.client.ContractDetails</code> object
	 * @return equivalent object of type <code>com.jgoetsch.tradeframework.ContractDetails</code>
	 */
	public static ContractDetails fromTWSContractDetails(com.ib.client.ContractDetails twsContractDetails) {
		ContractDetails contractDetails = new com.jgoetsch.tradeframework.ContractDetails(fromTWSContract(twsContractDetails.contract()));
		contractDetails.setMarketName(twsContractDetails.marketName());
		contractDetails.setMinTick(twsContractDetails.minTick());
		contractDetails.setPriceMagnifier(twsContractDetails.priceMagnifier());
		contractDetails.setOrderTypes(twsContractDetails.orderTypes());
		contractDetails.setValidExchanges(twsContractDetails.validExchanges());
		contractDetails.setUnderConId(twsContractDetails.underConid());
		contractDetails.setLongName(twsContractDetails.longName());
		contractDetails.setIndustry(twsContractDetails.industry());
		contractDetails.setCategory(twsContractDetails.category());
		contractDetails.setSubcategory(twsContractDetails.subcategory());
		return contractDetails;
	}

	/**
	 * Convert a TradeFramework order object into an IB order object.
	 * @param order <code>com.jgoetsch.tradeframework.Order</code> object
	 * @return equivilent object of type <code>com.ib.client.Order</code>
	 */
	public static com.ib.client.Order toTWSOrder(StandardOrder order) {
		com.ib.client.Order twsOrder = new com.ib.client.Order();
		twsOrder.action(order.getQuantity().signum() > 0 ? "BUY" : "SELL");
		twsOrder.totalQuantity(order.getQuantity().abs().doubleValue());
		twsOrder.orderType(order.getType());
		twsOrder.tif(order.getTimeInForce());
		twsOrder.outsideRth(order.getAllowOutsideRth());
		twsOrder.lmtPrice(order.getLimitPrice().doubleValue());
		twsOrder.auxPrice(order.getAuxPrice().doubleValue());
		twsOrder.trailStopPrice(order.getTrailStopPrice().doubleValue());
		twsOrder.transmit(order.isTransmit());
		twsOrder.account(order.getAccount());
		return twsOrder;
	}

	/**
	 * Convert a IB order object into a TradeFramework order object.
	 * @param twsOrder <code>com.ib.client.Order</code> object
	 * @return equivalent object of type <code>com.jgoetsch.tradeframework.Order</code>
	 */
	public static StandardOrder fromTWSOrder(com.ib.client.Order twsOrder) {
		StandardOrder order = new StandardOrder();
		order.setType(twsOrder.getOrderType());
		order.setQuantity(quantityToDecimal("SELL".equalsIgnoreCase(twsOrder.getAction()), twsOrder.totalQuantity()));
		order.setTimeInForce(twsOrder.getTif());
		order.setLimitPrice(priceToDecimal(twsOrder.lmtPrice()));
		order.setAuxPrice(priceToDecimal(twsOrder.auxPrice()));
		order.setTrailStopPrice(priceToDecimal(twsOrder.trailStopPrice()));
		order.setAccount(twsOrder.account());
		order.setTransmit(twsOrder.transmit());
		return order;
	}

	/**
	 * Convert a IB execution object into a TradeFramework execution object.
	 * @param twsExecution <code>com.ib.client.Execution</code> object
	 * @return equivilent object of type <code>com.jgoetsch.tradeframework.Execution</code>
	 */
	public static Execution fromTWSExecution(com.ib.client.Execution twsExecution) {
		Execution execution = new Execution();
		execution.setQuantity(quantityToDecimal("SLD".equalsIgnoreCase(twsExecution.side()), twsExecution.shares()));
		execution.setPrice(priceToDecimal(twsExecution.price()));
		return execution;
	}

	private static BigDecimal priceToDecimal(double value) {
		return BigDecimal.valueOf(value);
	}

	private static BigDecimal quantityToDecimal(boolean isSell, double value) {
		BigDecimal qty = BigDecimal.valueOf(value);
		return isSell ? qty.negate() : qty;
	}
}
