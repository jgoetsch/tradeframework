package com.jgoetsch.eventtrader.filter;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.SerializationUtil;
import com.pusher.rest.Pusher;
import com.pusher.rest.data.Result;
import com.pusher.rest.data.Result.Status;

public class PusherPresenceFilter<M extends Msg> extends FilterProcessor<M> {
	private Logger log = LoggerFactory.getLogger(PusherPresenceFilter.class);

	private String appId;
	private String apiKey;
	private String apiSecret;
	private String channel;
	private Pusher pusher;
	private Gson gson = new Gson();

	private static class ChannelInfo {
		boolean occupied;
	}

	@PostConstruct
	public void initialize() {
		pusher = new Pusher(appId, apiKey, apiSecret);
		pusher.setGsonSerialiser(SerializationUtil.createGson());
	}

	@Override
	protected boolean handleProcessing(M msg, Map<Object, Object> context) throws Exception {
		if (pusher == null)
			initialize();

		Result result = pusher.get("/channels/" + channel);
		if (result.getStatus() == Status.SUCCESS) {
			log.debug(result.getMessage());
			ChannelInfo info = gson.fromJson(result.getMessage(), ChannelInfo.class);
			if (info.occupied)
				return true;
		}
		else
			log.warn("Failed to get channel presence info");
		return false;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getApiSecret() {
		return apiSecret;
	}

	public void setApiSecret(String apiSecret) {
		this.apiSecret = apiSecret;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

}
