package com.jgoetsch.eventtrader.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.eventtrader.TradeType;
import com.jgoetsch.eventtrader.filter.ContractTypeFilter;
import com.jgoetsch.eventtrader.filter.FilterChainProcessor;
import com.jgoetsch.eventtrader.filter.FilterProcessor;
import com.jgoetsch.eventtrader.filter.PartialTradeFilter;
import com.jgoetsch.eventtrader.filter.PriceFilter;
import com.jgoetsch.eventtrader.filter.SymbolBlacklistFilter;
import com.jgoetsch.eventtrader.filter.TimeOfDayFilter;
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

	private static final DateTimeZone tz = DateTimeZone.forID("America/New_York");

	public void testTimeOfDayFilter() throws Exception {
		TimeOfDayFilter<Msg> afternoonFilter = new TimeOfDayFilter<Msg>();
		afternoonFilter.setAfter("13:00");
		Msg morningAlert = new Msg(new DateTime(2017, 10, 11, 9, 47, 0, 0, tz), "Test", "Morning alert");
		Msg middayAlert = new Msg(new DateTime(2017, 10, 11, 12, 05, 0, 0, tz), "Test", "Midday alert");
		Msg afternoonAlert = new Msg(new DateTime(2017, 10, 11, 15, 25, 0, 0, tz), "Test", "Afternoon alert");
		AssertFilter.shouldNotProcess(afternoonFilter, morningAlert);
		AssertFilter.shouldNotProcess(afternoonFilter, middayAlert);
		AssertFilter.shouldProcess(afternoonFilter, afternoonAlert);
		
		afternoonFilter.setAfter("15:25");
		AssertFilter.shouldProcess(afternoonFilter, afternoonAlert);
		afternoonFilter.setAfter("15:26");
		AssertFilter.shouldNotProcess(afternoonFilter, afternoonAlert);

		TimeOfDayFilter<Msg> morningFilter = new TimeOfDayFilter<Msg>();
		morningFilter.setBefore("10:30");
		AssertFilter.shouldProcess(morningFilter, morningAlert);
		AssertFilter.shouldNotProcess(morningFilter, middayAlert);
		AssertFilter.shouldNotProcess(morningFilter, afternoonAlert);

		TimeOfDayFilter<Msg> middayFilter = new TimeOfDayFilter<Msg>();
		middayFilter.setBefore("13:20");
		middayFilter.setAfter("11:00");
		AssertFilter.shouldNotProcess(middayFilter, morningAlert);
		AssertFilter.shouldProcess(middayFilter, middayAlert);
		AssertFilter.shouldNotProcess(middayFilter, afternoonAlert);
	}

	public void testPriceFilter() throws Exception {
		PriceFilter priceFilter = new PriceFilter();
		priceFilter.setMin(2.0);
		AssertFilter.shouldProcess(priceFilter, new TradeSignal(TradeType.BUY, Contract.stock("AAPL"), 100, 180.00));
		AssertFilter.shouldNotProcess(priceFilter, new TradeSignal(TradeType.BUY, Contract.stock("LQMT"), 10000, 0.26));
		priceFilter.setMax(20.0);
		AssertFilter.shouldNotProcess(priceFilter, new TradeSignal(TradeType.BUY, Contract.stock("AAPL"), 100, 180.00));
	}
}
