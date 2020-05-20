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
package com.jgoetsch.ib.handlers;

import com.ib.client.Bar;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.TickAttrib;

/**
 * Base EWrapper implementation that is created with an id and handles events with
 * a request/order/ticker id by forwarding those calls with the id matching that
 * of this handler to overridable handler methods named on[eventName].
 * 
 * @author jgoetsch
 *
 */
public class BaseIdHandler extends BaseHandler {

	private int id;
	
	public BaseIdHandler(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}

	@Override
	public final void bondContractDetails(int reqId, ContractDetails contractDetails) {
		if (id == reqId)
			onBondContractDetails(contractDetails);
	}

	@Override
	public final void contractDetails(int reqId, ContractDetails contractDetails) {
		if (id == reqId)
			onContractDetails(contractDetails);
	}

	@Override
	public final void contractDetailsEnd(int reqId) {
		if (id == reqId)
			onContractDetailsEnd();
	}

	@Override
	public final void execDetails(int orderId, Contract contract, Execution execution) {
		if (id == orderId)
			onExecDetails(contract, execution);
	}

	@Override
	public final void fundamentalData(int reqId, String data) {
		if (id == reqId)
			onFundamentalData(data);
	}

	@Override
	public final void historicalData(int reqId, Bar bar)
	{
		if (id == reqId)
			onHistoricalData(bar);
	}

	@Override
	public final void historicalDataEnd(int reqId, String startDateStr, String endDateStr)
	{
		if (id == reqId)
			onHistoricalDataEnd(startDateStr, endDateStr);
	}

	@Override
	public final void openOrder(int orderId, Contract contract, Order order,
			OrderState orderState)
	{
		if (id == orderId)
			onOpenOrder(contract, order, orderState);
	}

	@Override
	public final void orderStatus(int orderId, String status, double filled,
			double remaining, double avgFillPrice, int permId, int parentId,
			double lastFillPrice, int clientId, String whyHeld, double mktCapPrice)
	{
		if (id == orderId)
			onOrderStatus(status, Double.valueOf(filled).intValue(), Double.valueOf(remaining).intValue(), avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld);
	}

	@Override
	public final void realtimeBar(int reqId, long time, double open, double high,
			double low, double close, long volume, double wap, int count)
	{
		if (id == reqId)
			onRealtimeBar(time, open, high, low, close, volume, wap, count);
	}

	@Override
	public final void scannerData(int reqId, int rank,
			ContractDetails contractDetails, String distance, String benchmark,
			String projection, String legsStr)
	{
		if (id == reqId)
			onScannerData(rank, contractDetails, distance, benchmark, projection, legsStr);
	}

	@Override
	public final void scannerDataEnd(int reqId) {
		if (id == reqId)
			onScannerDataEnd();
	}

	@Override
	public final void tickEFP(int tickerId, int tickType, double basisPoints,
			String formattedBasisPoints, double impliedFuture, int holdDays,
			String futureExpiry, double dividendImpact, double dividendsToExpiry)
	{
		if (id == tickerId)
			onTickEFP(tickType, basisPoints, formattedBasisPoints, impliedFuture, holdDays, futureExpiry, dividendImpact, dividendsToExpiry);
	}

	@Override
	public final void tickGeneric(int tickerId, int tickType, double value) {
		if (id == tickerId)
			onTickGeneric(tickType, value);
	}

	@Override
	public final void tickOptionComputation(int tickerId, int field, double impliedVol, double delta, double optPrice,
			double pvDividend, double gamma, double vega, double theta, double undPrice)
	{
		if (id == tickerId)
			onTickOptionComputation(field, impliedVol, delta, optPrice, pvDividend, gamma, vega, theta, undPrice);
	}

	@Override
	public final void tickPrice(int tickerId, int field, double price, TickAttrib attrib)
	{
		if (id == tickerId)
			onTickPrice(field, price, attrib);
	}

	@Override
	public final void tickSize(int tickerId, int field, int size) {
		if (id == tickerId)
			onTickSize(field, size);
	}

	@Override
	public final void tickString(int tickerId, int tickType, String value) {
		if (id == tickerId)
			onTickString(tickType, value);
	}

	@Override
	public void tickSnapshotEnd(int reqId) {
		if (id == reqId)
			onTickSnapshotEnd();
	}

	@Override
	public final void updateMktDepth(int tickerId, int position, int operation,
			int side, double price, int size) 
	{
		if (id == tickerId)
			onUpdateMktDepth(position, operation, side, price, size);
	}

	@Override
	public final void updateMktDepthL2(int tickerId, int position,
			String marketMaker, int operation, int side, double price, int size, boolean isSmartDepth)
	
	{
		if (id == tickerId)
			onUpdateMktDepthL2(position, marketMaker, operation, side, price, size);
	}


	/**
	 * Override to implement bondContractDetails event for this request id.
	 * @param contractDetails ContractDetails object from bondContractDetails call.
	 */
	protected void onBondContractDetails(ContractDetails contractDetails) {
	}

	/**
	 * Override to implement contractDetails event for this request id.
	 * @param contractDetails ContractDetails object from contractDetails call.
	 */
	protected void onContractDetails(ContractDetails contractDetails) {
	}

	/**
	 * Override to implement contractDetailsEnd event for this request id.
	 */
	protected void onContractDetailsEnd() {
	}

	/**
	 * Override to implement execDetails event for this order id.
	 * @param contract Contract object from execDetails call.
	 * @param execution Execution object from execDetails call.
	 */
	protected void onExecDetails(Contract contract, Execution execution) {
	}

	protected void onFundamentalData(String data) {
	}

	protected void onHistoricalData(Bar bar) {
	}

	protected void onHistoricalDataEnd(String startDateStr, String endDateStr) {
	}

	protected void onOpenOrder(Contract contract, Order order,
			OrderState orderState) {
	}

	protected void onOrderStatus(String status, int filled,
			int remaining, double avgFillPrice, int permId, int parentId,
			double lastFillPrice, int clientId, String whyHeld) {
	}

	protected void onRealtimeBar(long time, double open, double high,
			double low, double close, long volume, double wap, int count) {
	}

	protected void onScannerData(int rank,
			ContractDetails contractDetails, String distance, String benchmark,
			String projection, String legsStr) {
	}

	protected void onScannerDataEnd() {
	}

	protected void onTickEFP(int tickType, double basisPoints,
			String formattedBasisPoints, double impliedFuture, int holdDays,
			String futureExpiry, double dividendImpact, double dividendsToExpiry) {
	}

	/**
	 * Override to handle tickGeneric event for the tickerId matching the id of this handler.
	 * @param tickType int param from tickGeneric call
	 * @param value double param from tickGeneric call
	 */
	protected void onTickGeneric(int tickType, double value) {
	}

	protected void onTickOptionComputation(int field, double impliedVol, double delta, double optPrice,
			double pvDividend, double gamma, double vega, double theta, double undPrice) {
	}

	/**
	 * Override to handle tickPrice event for the tickerId matching the id of this handler.
	 * @param field int param from tickPrice call
	 * @param price double param from tickPrice call
	 * @param canAutoExecute param from tickPrice call
	 */
	protected void onTickPrice(int field, double price,
			TickAttrib attrib) {
	}

	/**
	 * Override to handle tickSize event for the tickerId matching the id of this handler.
	 * @param field int param from tickSize call
	 * @param size int param from tickSize call
	 */
	protected void onTickSize(int field, int size) {
	}

	/**
	 * Override to handle tickString event for the tickerId matching the id of this handler.
	 * @param field int param from tickString call
	 * @param value String param from tickString call
	 */
	protected void onTickString(int tickType, String value) {
	}

	/**
	 * Override to handle tickSnapshotEnd event for the tickerId matching the id of this handler.
	 */
	protected void onTickSnapshotEnd() {
	}
	
	/**
	 * Override to handle updateMktDepth event for the tickerId matching the id of this handler.
	 * @param position
	 * @param operation
	 * @param side
	 * @param price
	 * @param size
	 */
	protected void onUpdateMktDepth(int position, int operation,
			int side, double price, int size) {
	}

	/**
	 * Override to handle updateMktDepthL2 event for the tickerId matching the id of this handler.
	 * @param position
	 * @param marketMaker
	 * @param operation
	 * @param side
	 * @param price
	 * @param size
	 */
	protected void onUpdateMktDepthL2(int position,
			String marketMaker, int operation, int side, double price, int size) {
	}

}
