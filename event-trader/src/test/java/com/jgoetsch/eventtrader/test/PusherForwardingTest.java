package com.jgoetsch.eventtrader.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.joda.time.DateTime;
import org.junit.Assert;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.eventtrader.filter.FilterProcessor;
import com.jgoetsch.eventtrader.filter.PusherPresenceFilter;
import com.jgoetsch.eventtrader.processor.PusherForwardingProcessor;
import com.jgoetsch.eventtrader.processor.Processor;
import com.jgoetsch.eventtrader.source.PusherMsgSource;
import com.jgoetsch.eventtrader.source.PusherSecretAuthorizer;
import com.jgoetsch.eventtrader.source.parser.JsonSerializedMsgParser;
import com.pusher.client.PusherOptions;

import junit.framework.TestCase;

public class PusherForwardingTest extends TestCase {

	private String appId = "226927";
	private String apiKey = "659ad8bfe96a36e54369";
	private String apiSecret = "cd282d6f02bf9e437899";
	private String channel = "presence-forwarding";

	public void testPusherForwarding() throws Exception {
		PusherSecretAuthorizer authorizer = new PusherSecretAuthorizer();
		authorizer.setAppId(appId);
		authorizer.setApiKey(apiKey);
		authorizer.setApiSecret(apiSecret);

		PusherOptions options = new PusherOptions();
		options.setAuthorizer(authorizer);

		PusherMsgSource listener = new PusherMsgSource();
		listener.setAppKey(apiKey);
		listener.setChannels(Collections.singletonList(channel));
		listener.setPusherOptions(options);
		listener.setMsgParser(new JsonSerializedMsgParser());

		PusherPresenceFilter<Msg> presenceFilter = new PusherPresenceFilter<Msg>();
		presenceFilter.setAppId(appId);
		presenceFilter.setApiKey(apiKey);
		presenceFilter.setApiSecret(apiSecret);
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

		PusherForwardingProcessor processor = new PusherForwardingProcessor();
		processor.setAppId(appId);
		processor.setApiKey(apiKey);
		processor.setApiSecret(apiSecret);
		processor.setChannels(Collections.singletonList(channel));

		List<Msg> messages = Arrays.asList(
				new Msg("system", "Test message #1"),
				new Msg("jgoetsch", "Test message #2"),
				new Msg(new DateTime(2016, 6, 10, 9, 30, 0, 0), "timothysykes", "Test message #3")
		);
		for (Msg msg : messages) {
			processor.process(msg, new HashMap<Object,Object>());
			Thread.sleep(1000);
		}

		Assert.assertEquals("Received messages", messages, received);
	}
}
