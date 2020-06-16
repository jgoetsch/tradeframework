package com.jgoetsch.ib;

import java.math.BigDecimal;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.ValueMapping;
import org.mapstruct.factory.Mappers;

import com.ib.client.Types.Action;
import com.ib.client.Types.SecType;
import com.jgoetsch.tradeframework.Contract;
import com.jgoetsch.tradeframework.ContractDetails;
import com.jgoetsch.tradeframework.Execution;
import com.jgoetsch.tradeframework.Order;
import com.jgoetsch.tradeframework.Order.OrderType;
import com.jgoetsch.tradeframework.Order.TimeInForce;
import com.jgoetsch.tradeframework.StandardOrder;
import com.jgoetsch.tradeframework.Contract.SecurityType;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TWSMapper {
	public TWSMapper INSTANCE = Mappers.getMapper(TWSMapper.class);

	/**
	 * Convert a TradeFramework contract object into an IB contract object.
	 * @param contract <code>com.jgoetsch.tradeframework.Contract</code> object
	 * @return equivalent object of type <code>com.ib.client.Contract</code>
	 */
	@Mapping(target = "secType", source = "type")
	@Mapping(target = "lastTradeDateOrContractMonth", source = "expiry")
	com.ib.client.Contract toTWSContract(Contract contract);

	/**
	 * Convert a IB contract object into a TradeFramework contract object.
	 * @param twsContract <code>com.ib.client.Contract</code> object
	 * @return equivalent object of type <code>com.jgoetsch.tradeframework.Contract</code>
	 */
	@InheritInverseConfiguration
	Contract fromTWSContract(com.ib.client.Contract twsContract);

	@ValueMapping(source = "STOCK", target = "STK")
	@ValueMapping(source = "FUTURES", target = "FUT")
	@ValueMapping(source = "OPTIONS", target = "OPT")
	SecType toTWSSecType(SecurityType securityType);

	@InheritInverseConfiguration
	@ValueMapping(source = MappingConstants.ANY_REMAINING, target = MappingConstants.NULL)
	SecurityType fromTWSSecType(SecType secType);

	/**
	 * Convert a TradeFramework order object into an IB order object.
	 * @param order <code>com.jgoetsch.tradeframework.Order</code> object
	 * @return equivalent object of type <code>com.ib.client.Order</code>
	 */
	@Mapping(target = "action", source = "quantity")
	@Mapping(target = "totalQuantity", source = "quantity", qualifiedByName = "AbsoluteQuantity")
	@Mapping(target = "orderType", source = "type")
	@Mapping(target = "tif", source = "timeInForce")
	@Mapping(target = "outsideRth", source = "allowOutsideRth")
	@Mapping(target = "lmtPrice", source = "limitPrice")
	com.ib.client.Order toTWSOrder(Order order);

	/**
	 * Convert a IB order object into a TradeFramework order object.
	 * @param twsOrder <code>com.ib.client.Order</code> object
	 * @return equivalent object of type <code>com.jgoetsch.tradeframework.Order</code>
	 */
	@InheritInverseConfiguration()
	@Mapping(target = "quantity", expression = "java(quantityToDecimal(twsOrder.action(), twsOrder.totalQuantity()))")
	StandardOrder fromTWSOrder(com.ib.client.Order twsOrder);

	com.ib.client.OrderType toTWSOrderType(OrderType orderType);

	@InheritInverseConfiguration
	@ValueMapping(source = MappingConstants.ANY_REMAINING, target = MappingConstants.NULL)
	OrderType fromTWSOrderType(com.ib.client.OrderType orderType);

	@ValueMapping(source = "IMMEDIATE_OR_CANCEL", target = "IOC")
	@ValueMapping(source = "GOOD_TILL_DATE", target = "GTD")
	@ValueMapping(source = "FILL_OR_KILL", target = "FOK")
	com.ib.client.Types.TimeInForce toTWSTimeInForce(TimeInForce timeInForce);

	@InheritInverseConfiguration
	@ValueMapping(source = MappingConstants.ANY_REMAINING, target = MappingConstants.NULL)
	TimeInForce fromTWSTimeInForce(com.ib.client.Types.TimeInForce timeInForce);

	/**
	 * Convert a IB contract details object into a TradeFramework contract details object.
	 * @param twsContract <code>com.ib.client.ContractDetails</code> object
	 * @return equivalent object of type <code>com.jgoetsch.tradeframework.ContractDetails</code>
	 */
	@Mapping(target = "type", source = "contract.secType")
	@Mapping(target = "expiry", source = "contract.lastTradeDateOrContractMonth")
	@Mapping(target = ".", source = "contract")
	ContractDetails fromTWSContractDetails(com.ib.client.ContractDetails contractDetails);

	/**
	 * Convert a IB execution object into a TradeFramework execution object.
	 * @param twsExecution <code>com.ib.client.Execution</code> object
	 * @return equivalent object of type <code>com.jgoetsch.tradeframework.Execution</code>
	 */
	@Mapping(target = "quantity", expression = "java(quantityToDecimal(fromExecutionSide(execution.side()), execution.shares()))")
	Execution fromTWSExecution(com.ib.client.Execution execution);

	Execution fromTWSCommissionReport(com.ib.client.CommissionReport commReport);

	default Action fromExecutionSide(String side) {
		return "SLD".equalsIgnoreCase(side) ? Action.SELL : Action.BUY;
	}

	default Action actionFromQuantity(BigDecimal quantity) {
		return quantity != null && quantity.signum() < 0 ? Action.SELL : Action.BUY;
	}

	@Named("AbsoluteQuantity")
	default double absQuantity(BigDecimal quantity) {
		return quantity == null ? 0 : quantity.abs().doubleValue();
	}

	default BigDecimal quantityToDecimal(Action action, double totalQuantity) {
		BigDecimal qty = BigDecimal.valueOf(totalQuantity).stripTrailingZeros();
		if (qty.scale() < 0)
			qty = qty.setScale(0);
		return Action.BUY.equals(action) ? qty : qty.negate();
	}

}
