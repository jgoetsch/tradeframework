package com.jgoetsch.eventtrader.processor;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jgoetsch.eventtrader.Msg;
import com.pusher.rest.Pusher;

public class PusherForwardingProcessor implements Processor<Msg> {

	private List<String> channels;

	private final Pusher pusher;

	private final ObjectMapper mapper = new ObjectMapper()
			.setVisibility(PropertyAccessor.ALL, Visibility.NONE)
			.setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
			.registerModule(new JavaTimeModule());

	public PusherForwardingProcessor(String appId, String apiKey, String apiSecret) {
		this.pusher = new Pusher(appId, apiKey, apiSecret);
		pusher.setDataMarshaller(object -> {
			try {
				return mapper.writeValueAsString(object);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public void process(Msg msg, Map<Object, Object> context) throws Exception {
		pusher.trigger(channels, msg.getClass().getSimpleName(), msg);
	}

	public List<String> getChannels() {
		return channels;
	}

	public void setChannels(List<String> channels) {
		this.channels = channels;
	}

}
