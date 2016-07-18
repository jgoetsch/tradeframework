package com.jgoetsch.eventtrader.filter;

import java.util.List;
import java.util.Map;

import com.jgoetsch.eventtrader.Msg;

public class FilterChainProcessor<M extends Msg> extends FilterProcessor<M> {

	List<FilterProcessor<M>> filters;

	@Override
	protected boolean handleProcessing(M msg, Map<Object, Object> context) throws Exception {
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
