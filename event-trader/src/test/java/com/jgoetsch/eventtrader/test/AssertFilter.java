package com.jgoetsch.eventtrader.test;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.filter.FilterProcessor;
import com.jgoetsch.eventtrader.processor.Processor;
import com.jgoetsch.eventtrader.processor.ProcessorContext;

public class AssertFilter {

	public static <M extends Msg> void shouldProcess(FilterProcessor<M> filter, M msg) throws Exception {
		final AtomicInteger count = new AtomicInteger();
		filter.setProcessor(new Processor<M>() {
			public void process(M msg, ProcessorContext context) throws Exception {
				count.incrementAndGet();
			}
		});
		filter.process(msg, new ProcessorContext());
		Assert.assertEquals(msg.getClass().getSimpleName() + " \"" + msg + "\" " + (count.get() == 0 ? "was not processed." : "processed " + count.get() + " times."), 1, count.get());
	}

	public static <M extends Msg> void shouldNotProcess(FilterProcessor<M> filter, M msg) throws Exception {
		filter.setProcessor(new Processor<M>() {
			public void process(M msg, ProcessorContext context) throws Exception {
				Assert.fail(msg.getClass().getSimpleName() + " \"" + msg + "\" was processed.");
			}
		});
		filter.process(msg, new ProcessorContext());
	}

}
