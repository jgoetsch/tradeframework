package com.jgoetsch.eventtrader.processor;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.SerializationUtil;
import com.pusher.rest.Pusher;

public class PusherForwardingProcessor implements Processor<Msg> {

	private String appId;
	private String apiKey;
	private String apiSecret;
	private Pusher pusher;
	private List<String> channels;

	@PostConstruct
	public void initialize() {
		pusher = new Pusher(appId, apiKey, apiSecret);
		pusher.setGsonSerialiser(SerializationUtil.createGson());
	}

	public void process(Msg msg, Map<Object, Object> context) throws Exception {
		if (pusher == null)
			initialize();

		pusher.trigger(channels, msg.getClass().getSimpleName(), msg);
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

	public List<String> getChannels() {
		return channels;
	}

	public void setChannels(List<String> channels) {
		this.channels = channels;
	}

}
