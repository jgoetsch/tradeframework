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
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.ib.client.Types.SecType;
import com.jgoetsch.tradeframework.Contract;
import com.jgoetsch.tradeframework.Contract.SecurityType;
import com.jgoetsch.tradeframework.ContractDetails;
import com.jgoetsch.tradeframework.Execution;
import com.jgoetsch.tradeframework.Order;
import com.jgoetsch.tradeframework.Order.OrderType;
import com.jgoetsch.tradeframework.Order.TimeInForce;
import com.jgoetsch.tradeframework.StandardOrder;

/**
 * Provides static methods to convert Interactive Brokers TWS specific data
 * objects to and from the equivalent TradeFramework data objects.
 * 
 * @author jgoetsch
 * 
 */
public class TWSUtils {

	private static <K extends Enum<K>, V, U> Map<K, V> inverseEnumMap(Collection<U> values, Function<U, K> keyMapper, Function<U, V> valueMapper) {
		Class<K> keyClass = values.stream().map(keyMapper).filter(k -> k != null).findAny().get().getDeclaringClass();
		return values.stream().filter(v -> keyMapper.apply(v) != null).collect(
				Collectors.toMap(keyMapper, valueMapper, TWSUtils::handleMerge,
				() -> new EnumMap<K, V>(keyClass)));
	}

	private static <V> V handleMerge(V value1, V value2) {
		throw new IllegalStateException("Duplicate mappings " + value1 + " and " + value2);
	}

	private TWSUtils() {}

	private static Map<SecurityType, SecType> toTWSSecType = new EnumMap<SecurityType, SecType>(SecurityType.class) {
		private static final long serialVersionUID = 1L;
		{
			put(SecurityType.STOCK, SecType.STK);
			put(SecurityType.FUTURES, SecType.FUT);
			put(SecurityType.OPTIONS, SecType.OPT);
		}
	};
	private static Map<SecType, SecurityType> fromTWSSecType =
			inverseEnumMap(toTWSSecType.entrySet(), Map.Entry::getValue, Map.Entry::getKey);

	/**
	 * Convert a TradeFramework contract object into an IB contract object.
	 * @param contract <code>com.jgoetsch.tradeframework.Contract</code> object
	 * @return equivalent object of type <code>com.ib.client.Contract</code>
	 */
	public static com.ib.client.Contract toTWSContract(Contract contract) {
		com.ib.client.Contract twsContract = new com.ib.client.Contract();
		if (!toTWSSecType.containsKey(contract.getType()))
			throw new IllegalArgumentException("Invalid security type " + contract.getType());
		twsContract.secType(toTWSSecType.get(contract.getType()));
		twsContract.symbol(contract.getSymbol());
		twsContract.exchange(contract.getExchange());
		twsContract.currency(contract.getCurrency());
		twsContract.lastTradeDateOrContractMonth(contract.getExpiry());
		if (contract.getMultiplier() != null && contract.getMultiplier() > 1)
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
		contract.setType(fromTWSSecType.get(twsContract.secType()));
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
		contractDetails.setUnderConid(twsContractDetails.underConid());
		contractDetails.setLongName(twsContractDetails.longName());
		contractDetails.setIndustry(twsContractDetails.industry());
		contractDetails.setCategory(twsContractDetails.category());
		contractDetails.setSubcategory(twsContractDetails.subcategory());
		return contractDetails;
	}

	/**
	 * Convert a TradeFramework order object into an IB order object.
	 * @param order <code>com.jgoetsch.tradeframework.Order</code> object
	 * @return equivalent object of type <code>com.ib.client.Order</code>
	 */
	public static com.ib.client.Order toTWSOrder(Order order) {
		com.ib.client.Order twsOrder = new com.ib.client.Order();
		twsOrder.action(order.getQuantity().signum() > 0 ? "BUY" : "SELL");
		twsOrder.totalQuantity(toDouble(order.getQuantity().abs()));
		twsOrder.orderType(com.ib.client.OrderType.get(order.getType().name()));
		twsOrder.tif(com.ib.client.Types.TimeInForce.get(order.getTimeInForce().name()));
		twsOrder.outsideRth(order.getAllowOutsideRth());
		twsOrder.lmtPrice(toDouble(order.getLimitPrice()));
		twsOrder.auxPrice(toDouble(order.getAuxPrice()));
		twsOrder.trailStopPrice(toDouble(order.getTrailStopPrice()));
		twsOrder.account(order.getAccount());
		return twsOrder;
	}

	/**
	 * Convert a IB order object into a TradeFramework order object.
	 * @param twsOrder <code>com.ib.client.Order</code> object
	 * @return equivalent object of type <code>com.jgoetsch.tradeframework.Order</code>
	 */
	public static Order fromTWSOrder(com.ib.client.Order twsOrder) {
		StandardOrder order = new StandardOrder();
		order.setType(OrderType.valueOf(twsOrder.orderType().name()));
		order.setQuantity(quantityToDecimal("SELL".equalsIgnoreCase(twsOrder.getAction()), twsOrder.totalQuantity()));
		order.setTimeInForce(TimeInForce.valueOf(twsOrder.tif().name()));
		order.setLimitPrice(priceToDecimal(twsOrder.lmtPrice()));
		order.setAuxPrice(priceToDecimal(twsOrder.auxPrice()));
		order.setTrailStopPrice(priceToDecimal(twsOrder.trailStopPrice()));
		order.setAccount(twsOrder.account());
		return order;
	}

	/**
	 * Convert a IB execution object into a TradeFramework execution object.
	 * @param twsExecution <code>com.ib.client.Execution</code> object
	 * @return equivalent object of type <code>com.jgoetsch.tradeframework.Execution</code>
	 */
	public static Execution fromTWSExecution(com.ib.client.Execution twsExecution) {
		Execution execution = new Execution();
		execution.setQuantity(quantityToDecimal("SLD".equalsIgnoreCase(twsExecution.side()), twsExecution.shares()));
		execution.setPrice(priceToDecimal(twsExecution.price()));
		return execution;
	}

	public static String fromTWSCommissionReport(com.ib.client.CommissionReport commReport) {
		return String.format("{%.2f, realizedPnL=%.2f", commReport.commission(),
				commReport.realizedPNL() == Double.MAX_VALUE ? 0 : commReport.realizedPNL());
	}

	private static double toDouble(BigDecimal value) {
		return value == null ? 0 : value.doubleValue();
	}
	
	private static BigDecimal priceToDecimal(double value) {
		return BigDecimal.valueOf(value);
	}

	private static BigDecimal quantityToDecimal(boolean isSell, double value) {
		BigDecimal qty = BigDecimal.valueOf(value).stripTrailingZeros();
		if (qty.scale() < 0)
			qty = qty.setScale(0);
		return isSell ? qty.negate() : qty;
	}
}
