package com.jgoetsch.tradeframework.etrade;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.tradeframework.BrokerCommunicationException;
import com.jgoetsch.tradeframework.Contract;
import com.jgoetsch.tradeframework.InvalidContractException;
import com.jgoetsch.tradeframework.Order;
import com.jgoetsch.tradeframework.StandardOrder;
import com.jgoetsch.tradeframework.data.DataUnavailableException;
import com.jgoetsch.tradeframework.etrade.dto.AccountListResponse;
import com.jgoetsch.tradeframework.etrade.dto.MessageList;
import com.jgoetsch.tradeframework.etrade.dto.MessageList.Message;
import com.jgoetsch.tradeframework.etrade.dto.PlaceOrderResponse;
import com.jgoetsch.tradeframework.etrade.dto.PreviewOrderResponse;
import com.jgoetsch.tradeframework.etrade.dto.QuoteResponse;
import com.jgoetsch.tradeframework.etrade.mapper.OrderMapper;
import com.jgoetsch.tradeframework.etrade.mapper.QuoteMapper;
import com.jgoetsch.tradeframework.marketdata.MarketData;
import com.jgoetsch.tradeframework.marketdata.MarketDataListener;
import com.jgoetsch.tradeframework.marketdata.MarketDataSource;
import com.jgoetsch.tradeframework.order.ExecutionListener;
import com.jgoetsch.tradeframework.order.TradingService;

public class EtradeService implements MarketDataSource, TradingService {
	private Logger log = LoggerFactory.getLogger(EtradeService.class);

	private AuthenticatedClient client;
	private OrderMapper orderMapper = OrderMapper.INSTANCE;
	private QuoteMapper quoteMapper = QuoteMapper.INSTANCE;

	public EtradeService(AuthenticatedClient client) {
		this.client = client;
	}

	public List<Map<String, Object>> getAccounts() throws IOException {
		return client.doGet(AccountListResponse.class, "v1/accounts/list.json", null)
				.thenApply(r -> r.accounts.account).join();
	}

	@Override
	public CompletableFuture<MarketData> getMktDataSnapshot(Contract contract) throws IOException {
		return client.doGet(QuoteResponse.class, String.format("v1/market/quote/%s.json", contract.getSymbol()),
				Map.of("detailFlag", "ALL", "skipMiniOptionsCheck", "true"))
				.whenComplete((r, ex) -> handleResponseMessages(r != null ? r.messages : null))
				.thenApply(r -> {
					if (r.getQuoteData() == null)
						throw new DataUnavailableException(r.messages.toString());
					return quoteMapper.marketDataFromQuoteResponse(r, r.getQuoteData().All);
				});
	}

	@Override
	public void subscribeMarketData(Contract contract, MarketDataListener marketDataListener) throws IOException {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void cancelMarketData(Contract contract, MarketDataListener marketDataListener) throws IOException {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public MarketData getDataSnapshot(Contract contract) throws IOException {
		return getMktDataSnapshot(contract).join();
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public CompletableFuture<Order> previewOrder(Order order) throws InvalidContractException {
		return client.doPost(orderMapper.createPreviewOrderRequest(order), PreviewOrderResponse.class,
				String.format("v1/accounts/%s/orders/preview.json", order.getAccount()), null)
				.whenComplete((o, ex) -> handleResponseMessages(o != null ? o.getOrder().messages : null))
				.thenApply(orderMapper::fromResponse);
	}

	@Override
	public CompletableFuture<Order> placeOrder(Order order) throws InvalidContractException {
		if (order.getExternalId() == null)
			return previewOrder(order)
					.thenApply(o -> createLiveOrder(order, o))
					.thenCompose(this::placeOrder);
		else
			return client.doPost(orderMapper.createPlaceOrderRequest(order), PlaceOrderResponse.class,
					String.format("v1/accounts/%s/orders/place.json", order.getAccount()), null)
					.whenComplete((o, ex) -> handleResponseMessages(o != null ? o.getOrder().messages : null))
					.thenApply(orderMapper::fromResponse);
	}

	/**
	 * @return a copy of sourceOrder with the preview id set to that of previewedOrder
	 */
	public Order createLiveOrder(Order sourceOrder, Order previewedOrder) {
		if (previewedOrder.getExternalId() == null)
			throw new BrokerCommunicationException("Attempt to create live order from previewed order, but previewed order is missing previewId");

		StandardOrder newOrder = new StandardOrder(sourceOrder);
		newOrder.setExternalId(previewedOrder.getExternalId());
		return newOrder;
	}

	private void handleResponseMessages(MessageList messages) {
		if (messages != null)
			messages.stream().forEach(this::handleResponseMessage);
	}

	protected void handleResponseMessage(Message message) {
		switch (message.getType()) {
		case INFO:
		case INFO_HOLD:
			log.info("code:{} {}", message.getCode(), message.getDescription());
			break;
		case WARNING:
			log.warn("code:{} {}", message.getCode(), message.getDescription());
			break;
		case ERROR:
			log.error("code:{} {}", message.getCode(), message.getDescription());
			break;
		}
	}

	@Override
	public void subscribeExecutions(ExecutionListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void cancelExecutionSubscription(ExecutionListener listener) {
		throw new UnsupportedOperationException();
	}

}
