package com.jgoetsch.eventtrader.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assume.assumeNotNull;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.eventtrader.TradeType;
import com.jgoetsch.eventtrader.filter.PusherPresenceFilter;
import com.jgoetsch.eventtrader.processor.Processor;
import com.jgoetsch.eventtrader.processor.ProcessorContext;
import com.jgoetsch.eventtrader.processor.PusherForwardingProcessor;
import com.jgoetsch.eventtrader.source.PusherMsgSource;
import com.jgoetsch.eventtrader.source.PusherSecretAuthorizer;
import com.jgoetsch.eventtrader.source.parser.JsonMsgParser;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;

public class PusherForwardingTest {

	private String appId = System.getProperty("pusherAppId");
	private String apiKey = System.getProperty("pusherApiKey");
	private String apiSecret = System.getProperty("pusherApiSecret");
	private String channel = "presence-forwarding";

	private List<Msg> messages = Arrays.asList(
			new Msg("system", "Test message #1"),
			new Msg("jgoetsch", "Test message #2"),
			new Msg(ZonedDateTime.of(2016, 6, 10, 9, 30, 0, 0, ZoneId.of("America/New_York")).toInstant(), "test_user1", "Test message #3"),
			new TradeSignal(TradeType.BUY, "ZXYW", new Msg("test_user2", "Testing \"14^#%52234"))
	);

	@Before
	public void checkCredentials() {
		assumeNotNull(appId);
		assumeNotNull(apiKey);
		assumeNotNull(apiSecret);
	}

	@Test
	public void testPusherForwarding() throws Exception {
		PusherOptions options = new PusherOptions();
		options.setAuthorizer(new PusherSecretAuthorizer(appId, apiKey, apiSecret));

		PusherMsgSource listener = new PusherMsgSource(new Pusher(apiKey, options));
		listener.setChannels(Collections.singletonList(channel));
		listener.setMsgParser(new JsonMsgParser(Msg.class));
		listener.setNumEvents(messages.size());

		PusherPresenceFilter<Msg> presenceFilter = new PusherPresenceFilter<Msg>(appId, apiKey, apiSecret);
		presenceFilter.setChannel(channel);
		AssertFilter.shouldNotProcess(presenceFilter, new Msg("system", "test"));

		final List<Msg> received = Collections.synchronizedList(new ArrayList<Msg>());
		listener.setProcessors(Collections.singleton(new Processor<Msg>() {
			public void process(Msg msg, ProcessorContext context) throws Exception {
				received.add(msg);
			}
		}));

		Thread th = new Thread(listener);
		th.start();
		Thread.sleep(2000);

		AssertFilter.shouldProcess(presenceFilter, new Msg("system", "test"));

		PusherForwardingProcessor processor = new PusherForwardingProcessor(appId, apiKey, apiSecret);
		processor.setChannels(Collections.singletonList(channel));

		for (Msg msg : messages) {
			processor.process(msg, new ProcessorContext());
			Thread.sleep(100);
		}
		th.join(10000L);
		assertFalse(th.isAlive());
		assertEquals("Received messages", messages, received);
	}
}
