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
package com.jgoetsch.tradeframework.order;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import com.jgoetsch.tradeframework.InvalidContractException;
import com.jgoetsch.tradeframework.Order;

public interface TradingService extends Closeable {

	public CompletableFuture<Order> previewOrder(Order order) throws InvalidContractException, OrderException, IOException;

	public CompletableFuture<Order> placeOrder(Order order) throws InvalidContractException, OrderException, IOException;

	public void subscribeExecutions(ExecutionListener listener);

	public void cancelExecutionSubscription(ExecutionListener listener);

}
