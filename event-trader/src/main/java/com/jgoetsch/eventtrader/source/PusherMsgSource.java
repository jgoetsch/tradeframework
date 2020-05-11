package com.jgoetsch.eventtrader.source;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.source.parser.BufferedMsgParser;
import com.jgoetsch.eventtrader.source.parser.MsgParseException;
import com.pusher.client.Pusher;
import com.pusher.client.channel.PresenceChannelEventListener;
import com.pusher.client.channel.PusherEvent;
import com.pusher.client.channel.User;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

public class PusherMsgSource extends MsgSource {

	private Logger log = LoggerFactory.getLogger(getClass());
	private BufferedMsgParser msgParser;
	private Collection<String> channels;
	private String[] eventNames = { "message" };

	private final Pusher pusher;

	public PusherMsgSource(Pusher pusher) {
		this.pusher = pusher;
	}

	@Override
	protected void receiveMsgs() {
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

		PresenceChannelEventListener messageListener = new PresenceChannelEventListener() {
		    public void onEvent(PusherEvent event) {
		        log.debug("[{}:{}] {}", event.getChannelName(), event.getEventName(), event.getData());

				try {
					msgParser.parseContent(event.getData(), event.getEventName(), PusherMsgSource.this);
				} catch (MsgParseException e) {
					log.error("Error parsing message [{}:{}] {}", event.getChannelName(), event.getEventName(), event.getData(), e);
				}
		    }

			public void onSubscriptionSucceeded(String message) {
				log.info("Subscribed to channel " + message);
			}

			public void onAuthenticationFailure(String message, Exception e) {
				log.error("Failed to subscribe to channel " + message, e);
			}

			public void onUsersInformationReceived(String channelName, Set<User> users) {
			}

			public void userSubscribed(String channelName, User user) {
			}

			public void userUnsubscribed(String channelName, User user) {
			}
		};

		for (String channelName : channels) {
			if(channelName.startsWith("private-"))
				pusher.subscribePrivate(channelName, messageListener, eventNames);
			else if (channelName.startsWith("presence-"))
				pusher.subscribePresence(channelName, messageListener, eventNames);
			else
				pusher.subscribe(channelName, messageListener, eventNames);
		}

		for (;;) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
			}
		}
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

	public Collection<String> getEventNames() {
		return Arrays.asList(eventNames);
	}

	public void setEventNames(Collection<String> eventNames) {
		this.eventNames = eventNames.toArray(new String[0]);
	}

}
