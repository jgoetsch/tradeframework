package com.jgoetsch.eventtrader.filter;

import java.util.List;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.processor.ProcessorContext;

public class FilterChainProcessor<M extends Msg> extends FilterProcessor<M> {

	List<FilterProcessor<M>> filters;

	@Override
	protected boolean handleProcessing(M msg, ProcessorContext context) throws Exception {
		if (filters != null) {
			for (FilterProcessor<M> filter : filters) {
				if (!filter.handleProcessing(msg, context))
					return false;
			}
		}
		return true;
	}

	public List<FilterProcessor<M>> getFilters() {
		return filters;
	}

	public void setFilters(List<FilterProcessor<M>> filters) {
		this.filters = filters;
	}

}
