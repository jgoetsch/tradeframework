package com.jgoetsch.eventtrader.source;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.eventtrader.source.parser.BufferedMsgParser;
import com.jgoetsch.eventtrader.source.parser.MsgParseException;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.PrivateChannelEventListener;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

public class PusherMsgSource extends MsgSource {

	private Logger log = LoggerFactory.getLogger(getClass());
	private String appKey;
	private PusherOptions pusherOptions;
	private BufferedMsgParser msgParser;
	private Set<String> channels;

	@Override
	protected void receiveMsgs() {
		Pusher pusher = new Pusher(appKey, pusherOptions);

		pusher.connect(new ConnectionEventListener() {
		    public void onConnectionStateChange(ConnectionStateChange change) {
		    	log.info("State changed to " + change.getCurrentState() + " from " + change.getPreviousState());
		    }

		    public void onError(String message, String code, Exception e) {
		        log.info("There was a problem connecting!");
		    }
		}, ConnectionState.ALL);

		SubscriptionEventListener messageListener = new PrivateChannelEventListener() {
		    public void onEvent(String channel, String event, String data) {
		        log.debug(data);

				//JSONObject json = (JSONObject)JSONValue.parse(data);
				try {
					//msgParser.parseData((String)json.get("command"), (JSONObject)json.get("message"), PusherMsgSource.this);
					msgParser.parseContent(data, event, PusherMsgSource.this);
				} catch (MsgParseException e) {
					log.error("Message parse error, content was:\n" + data, e);
				}
		    }

			public void onSubscriptionSucceeded(String message) {
				log.info("Subscribed: " + message);
			}

			public void onAuthenticationFailure(String message, Exception e) {
				log.error("Failed to subscribe: " + message, e);
			}
		};

		for (String channelName : channels) {
			Channel channel = pusher.subscribePrivate(channelName);
			channel.bind("message", messageListener);
		}

		for (;;) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
			}
		}
	}

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public PusherOptions getPusherOptions() {
		return pusherOptions;
	}

	public void setPusherOptions(PusherOptions pusherOptions) {
		this.pusherOptions = pusherOptions;
	}

	public BufferedMsgParser getMsgParser() {
		return msgParser;
	}

	public void setMsgParser(BufferedMsgParser msgParser) {
		this.msgParser = msgParser;
	}

	public Set<String> getChannels() {
		return channels;
	}

	public void setChannels(Set<String> channels) {
		this.channels = channels;
	}

}
