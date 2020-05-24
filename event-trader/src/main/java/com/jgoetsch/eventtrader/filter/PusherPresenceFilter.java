package com.jgoetsch.eventtrader.filter;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.jgoetsch.eventtrader.Msg;
import com.pusher.rest.Pusher;
import com.pusher.rest.data.Result;
import com.pusher.rest.data.Result.Status;

public class PusherPresenceFilter<M extends Msg> extends FilterProcessor<M> {
	private Logger log = LoggerFactory.getLogger(PusherPresenceFilter.class);

	private String channel;
	private final Pusher pusher;
	private final Gson gson = new Gson();

	private static class ChannelInfo {
		boolean occupied;
	}

	public PusherPresenceFilter(String appId, String apiKey, String apiSecret) {
		pusher = new Pusher(appId, apiKey, apiSecret);
	}

	@Override
	protected boolean handleProcessing(M msg, Map<Object, Object> context) throws Exception {

		Result result = pusher.get("/channels/" + channel);
		if (result.getStatus() == Status.SUCCESS) {
			log.debug(result.getMessage());
			ChannelInfo info = gson.fromJson(result.getMessage(), ChannelInfo.class);
			if (info.occupied)
				return true;
		}
		else
			log.warn("Channel presence check failed with status {}", result.getStatus());
		return false;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

}
