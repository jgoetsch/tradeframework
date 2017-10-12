package com.jgoetsch.eventtrader.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.eventtrader.TradeType;
import com.jgoetsch.eventtrader.filter.ContractTypeFilter;
import com.jgoetsch.eventtrader.filter.FilterChainProcessor;
import com.jgoetsch.eventtrader.filter.FilterProcessor;
import com.jgoetsch.eventtrader.filter.PartialTradeFilter;
import com.jgoetsch.eventtrader.filter.SymbolBlacklistFilter;
import com.jgoetsch.eventtrader.filter.UsernameFilter;
import com.jgoetsch.tradeframework.Contract;

import junit.framework.TestCase;

public class FilterProcessorTest extends TestCase {

	public void testFilterChain() throws Exception {
		List<FilterProcessor<TradeSignal>> filters = new ArrayList<FilterProcessor<TradeSignal>>();
		
		ContractTypeFilter contractTypeFilter = new ContractTypeFilter();
		contractTypeFilter.setContractTypes(Collections.singleton("STK"));
		filters.add(contractTypeFilter);
		filters.add(new PartialTradeFilter());

		SymbolBlacklistFilter symbolFilter = new SymbolBlacklistFilter();
		symbolFilter.setSymbols(Arrays.asList("ISNS", "DGLY"));
		filters.add(symbolFilter);

		FilterChainProcessor<TradeSignal> filterChain = new FilterChainProcessor<TradeSignal>();
		filterChain.setFilters(filters);

		AssertFilter.shouldProcess(filterChain, new TradeSignal(TradeType.BUY, "MSFT", null));
		AssertFilter.shouldNotProcess(filterChain, new TradeSignal(TradeType.BUY, "ISNS", null));
		AssertFilter.shouldNotProcess(filterChain, new TradeSignal(TradeType.BUY, Contract.futures("HE", "102016", "GLOBEX"), null));
		AssertFilter.shouldProcess(filterChain, new TradeSignal(TradeType.SELL, Contract.stock("TASR"), 5000, 1.23, null));

		TradeSignal partialTrade = new TradeSignal(TradeType.BUY, Contract.stock("WATT"), 1000, 8.86, null);
		AssertFilter.shouldProcess(filterChain, partialTrade);
		partialTrade.setPartial(true);
		AssertFilter.shouldNotProcess(filterChain, partialTrade);

		UsernameFilter<TradeSignal> usernameFilter = new UsernameFilter<TradeSignal>();
		usernameFilter.setUsernames(Collections.singleton("timothysykes"));
		filters.add(usernameFilter);
		AssertFilter.shouldProcess(filterChain, new TradeSignal(TradeType.BUY, "GLUU", new Msg("timothysykes", "Bought some GLUU")));
		AssertFilter.shouldNotProcess(filterChain, new TradeSignal(TradeType.BUY, "GLUU", new Msg("someotherguy", "Bought some GLUU")));
		AssertFilter.shouldNotProcess(filterChain, new TradeSignal(TradeType.BUY, "DGLY", new Msg("timothysykes", "Buying banned stock")));
	}

}
