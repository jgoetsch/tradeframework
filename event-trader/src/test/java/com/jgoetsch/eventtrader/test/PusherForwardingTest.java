package com.jgoetsch.eventtrader.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeNotNull;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.eventtrader.TradeType;
import com.jgoetsch.eventtrader.filter.PusherPresenceFilter;
import com.jgoetsch.eventtrader.processor.Processor;
import com.jgoetsch.eventtrader.processor.PusherForwardingProcessor;
import com.jgoetsch.eventtrader.source.PusherMsgSource;
import com.jgoetsch.eventtrader.source.PusherSecretAuthorizer;
import com.jgoetsch.eventtrader.source.parser.JsonMsgParser;
import com.pusher.client.PusherOptions;

public class PusherForwardingTest {

	private String appId = System.getProperty("pusherAppId");
	private String apiKey = System.getProperty("pusherApiKey");
	private String apiSecret = System.getProperty("pusherApiSecret");
	private String channel = "presence-forwarding";

	@Before
	public void checkCredentials() {
		assumeNotNull(appId);
		assumeNotNull(apiKey);
		assumeNotNull(apiSecret);
	}

	@Test
	public void testPusherForwarding() throws Exception {
		PusherSecretAuthorizer authorizer = new PusherSecretAuthorizer(appId, apiKey, apiSecret);

		PusherOptions options = new PusherOptions();
		options.setAuthorizer(authorizer);

		PusherMsgSource listener = new PusherMsgSource();
		listener.setAppKey(apiKey);
		listener.setChannels(Collections.singletonList(channel));
		listener.setPusherOptions(options);
		listener.setMsgParser(new JsonMsgParser(Msg.class));

		PusherPresenceFilter<Msg> presenceFilter = new PusherPresenceFilter<Msg>(appId, apiKey, apiSecret);
		presenceFilter.setChannel(channel);
		AssertFilter.shouldNotProcess(presenceFilter, new Msg("system", "test"));

		final List<Msg> received = Collections.synchronizedList(new ArrayList<Msg>());
		listener.setProcessors(Collections.singleton(new Processor<Msg>() {
			public void process(Msg msg, Map<Object, Object> context) throws Exception {
				received.add(msg);
			}
		}));
		new Thread(listener).start();
		Thread.sleep(2000);

		AssertFilter.shouldProcess(presenceFilter, new Msg("system", "test"));

		PusherForwardingProcessor processor = new PusherForwardingProcessor(appId, apiKey, apiSecret);
		processor.setChannels(Collections.singletonList(channel));

		List<Msg> messages = Arrays.asList(
				new Msg("system", "Test message #1"),
				new Msg("jgoetsch", "Test message #2"),
				new Msg(ZonedDateTime.of(2016, 6, 10, 9, 30, 0, 0, ZoneId.of("America/New_York")).toInstant(), "test_user1", "Test message #3"),
				new TradeSignal(TradeType.BUY, "ZXYW", new Msg("test_user2", "Testing \"14^#%52234"))
		);
		for (Msg msg : messages) {
			processor.process(msg, new HashMap<Object,Object>());
			Thread.sleep(1000);
		}

		assertEquals("Received messages", messages, received);
	}
}
