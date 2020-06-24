package com.jgoetsch.tradeframework.etrade.mapper;

import java.math.BigDecimal;

import org.mapstruct.DecoratedWith;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ValueMapping;
import org.mapstruct.factory.Mappers;

import com.jgoetsch.tradeframework.Contract;
import com.jgoetsch.tradeframework.Contract.SecurityType;
import com.jgoetsch.tradeframework.Order;
import com.jgoetsch.tradeframework.Order.OrderType;
import com.jgoetsch.tradeframework.Order.TimeInForce;
import com.jgoetsch.tradeframework.StandardOrder;
import com.jgoetsch.tradeframework.etrade.dto.OrderDetail;
import com.jgoetsch.tradeframework.etrade.dto.OrderDetail.Instrument;
import com.jgoetsch.tradeframework.etrade.dto.OrderDetail.Instrument.OrderAction;
import com.jgoetsch.tradeframework.etrade.dto.OrderDetail.Instrument.Product;
import com.jgoetsch.tradeframework.etrade.dto.OrderDetail.MarketSession;
import com.jgoetsch.tradeframework.etrade.dto.OrderDetail.OrderTerm;
import com.jgoetsch.tradeframework.etrade.dto.OrderDetail.PriceType;
import com.jgoetsch.tradeframework.etrade.dto.PlaceOrderRequest;
import com.jgoetsch.tradeframework.etrade.dto.PreviewOrderRequest;
import com.jgoetsch.tradeframework.etrade.dto.PreviewOrderResponse;

@Mapper
@DecoratedWith(ClientOrderIdDecorator.class)
public interface OrderMapper {
	OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

	/*
	 * Mapping to Etrade request
	 */
	@Mapping(target = "clientOrderId", ignore = true)
	@Mapping(target = "orderType", expression = "java(toProductType(order.getContract().getType()))")
	@Mapping(target = "order.orderTerm", source="timeInForce")
	@Mapping(target = "order.priceType", source="type")
	@Mapping(target = "order.limitPrice", source = "limitPrice")
	@Mapping(target = "order.stopPrice", source = "auxPrice")
	@Mapping(target = "order.trailPrice", source = "trailStopPrice")
	@Mapping(target = "order.marketSession", source = "allowOutsideRth")
	@Mapping(target = "order.instrument.product", source = "contract")
	@Mapping(target = "order.instrument.quantity", expression = "java(order.getQuantity().abs().intValueExact())")
	@Mapping(target = "order.instrument.orderAction", expression = "java(toOrderAction(order.getQuantity(), order.isShort()))")
	@Mapping(target = "order.instrument.quantityType", constant = "QUANTITY")
	PreviewOrderRequest createPreviewOrderRequest(Order order);

	@InheritConfiguration
	@Mapping(target = "accountId", ignore = true)
	PlaceOrderRequest createPlaceOrderRequest(Order order);

	@ValueMapping(source = "STOCK", target = "EQ")
	@ValueMapping(source = "OPTIONS", target = "OPTN")
	@ValueMapping(source = "FUTURES", target = MappingConstants.NULL)
	OrderDetail.Type toProductType(SecurityType orderType);

	@Mapping(target = "securityType", source = "type")
	Product contractToProduct(Contract contract);

	@ValueMapping(source = "MKT", target = "MARKET")
	@ValueMapping(source = "LMT", target = "LIMIT")
	@ValueMapping(source = "STP", target = "STOP")
	@ValueMapping(source = "STP_LMT", target = "STOP_LIMIT")
	@ValueMapping(source = "TRAIL", target = "TRAILING_STOP_CNST")
	@ValueMapping(source = "TRAIL_LIMIT", target = "INVALID")
	PriceType toPriceType(OrderType orderType);

	@ValueMapping(source = "DAY", target = "GOOD_FOR_DAY")
	@ValueMapping(source = "GTC", target = "GOOD_UNTIL_CANCEL")
	OrderTerm toOrderTerm(TimeInForce timeInForce);

	default OrderAction toOrderAction(BigDecimal quantity, boolean isShort) {
		return OrderAction.values()[(isShort ? 1 : 0) + quantity.signum() + 1];
	}

	default MarketSession toMarketSession(boolean allowOutsideRth) {
		return allowOutsideRth ? MarketSession.EXTENDED : MarketSession.REGULAR;
	}

	/*
	 * Mapping from Etrade response
	 */

	@InheritInverseConfiguration
	@Mapping(target = "account", source = "accountId")
	@Mapping(target = "quantity", source = "order.instrument")
	@Mapping(target = "short", source = "order.instrument.orderAction")
	@Mapping(target = "tags", ignore = true)
	StandardOrder fromResponse(PreviewOrderResponse response);

	@InheritInverseConfiguration
	@Mapping(target = "currency", ignore = true)
	@Mapping(target = "expiry", ignore = true)
	@Mapping(target = "exchange", ignore = true)
	@Mapping(target = "multiplier", ignore = true)
	Contract productToContract(Product contract);

	@InheritInverseConfiguration
	SecurityType fromEtradeType(OrderDetail.Type orderType);

	@InheritInverseConfiguration
	TimeInForce fromOrderTerm(OrderTerm orderTerm);

	@InheritInverseConfiguration
	@ValueMapping(source = "INVALID", target = MappingConstants.NULL)
	OrderType fromPriceType(PriceType priceType);

	default BigDecimal quantityFromInstrument(Instrument instrument) {
		BigDecimal quantity = new BigDecimal(instrument.quantity);
		return OrderAction.BUY.compareTo(instrument.orderAction) < 0
				? quantity.negate() : quantity;
	}

	default boolean actionIsShort(OrderAction orderAction) {
		return (orderAction.ordinal() & 1) == 1;
	}

	default boolean isOutsideRth(MarketSession marketSession) {
		return MarketSession.EXTENDED.equals(marketSession);
	}
}
