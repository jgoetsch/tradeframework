package com.jgoetsch.eventtrader.source;

import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.source.parser.BufferedMsgParser;
import com.jgoetsch.eventtrader.source.parser.MsgParseException;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.PresenceChannelEventListener;
import com.pusher.client.channel.PusherEvent;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.channel.User;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

public class PusherMsgSource extends MsgSource {

	private Logger log = LoggerFactory.getLogger(getClass());
	private String appKey;
	private PusherOptions pusherOptions;
	private BufferedMsgParser msgParser;
	private Collection<String> channels;

	@Override
	protected void receiveMsgs() {
		Pusher pusher = pusherOptions != null ? new Pusher(appKey, pusherOptions) : new Pusher(appKey);

		pusher.connect(new ConnectionEventListener() {
		    public void onConnectionStateChange(ConnectionStateChange change) {
			    	log.info("State changed to " + change.getCurrentState() + " from " + change.getPreviousState());
			    	if (change.getCurrentState().equals(ConnectionState.DISCONNECTED)
			    			/*&& (change.getPreviousState().equals(ConnectionState.CONNECTED) || change.getPreviousState().equals(ConnectionState.CONNECTING))*/)
			    	{
			    		newMsg(new Msg("system", "PusherMsgSource is " + change.getCurrentState()));
			    	}
		    }

		    public void onError(String message, String code, Exception e) {
		        log.info("There was a problem connecting!", e);
		    }
		}, ConnectionState.ALL);

		SubscriptionEventListener messageListener = new PresenceChannelEventListener() {
		    public void onEvent(PusherEvent event) {
		        log.debug(event.getData());

				//JSONObject json = (JSONObject)JSONValue.parse(data);
				try {
					//msgParser.parseData((String)json.get("command"), (JSONObject)json.get("message"), PusherMsgSource.this);
					msgParser.parseContent(event.getData(), event.getEventName(), PusherMsgSource.this);
				} catch (MsgParseException e) {
					log.error("Message parse error, content was:\n" + event.getData(), e);
				}
		    }

			public void onSubscriptionSucceeded(String message) {
				log.info("Subscribed: " + message);
			}

			public void onAuthenticationFailure(String message, Exception e) {
				log.error("Failed to subscribe: " + message, e);
			}

			public void onUsersInformationReceived(String channelName, Set<User> users) {
			}

			public void userSubscribed(String channelName, User user) {
			}

			public void userUnsubscribed(String channelName, User user) {
			}
		};

		for (String channelName : channels) {
			Channel channel;
			if(channelName.startsWith("private-"))
				channel = pusher.subscribePrivate(channelName);
			else if (channelName.startsWith("presence-"))
				channel = pusher.subscribePresence(channelName);
			else
				channel = pusher.subscribe(channelName);
			channel.bind("message", messageListener);
			channel.bind("Msg", messageListener);
			channel.bind("TradeSignal", messageListener);
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

	public Collection<String> getChannels() {
		return channels;
	}

	public void setChannels(Collection<String> channels) {
		this.channels = channels;
	}

}
