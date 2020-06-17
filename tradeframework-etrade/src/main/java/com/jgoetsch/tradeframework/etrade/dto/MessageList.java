package com.jgoetsch.tradeframework.etrade.dto;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.api.client.util.Key;
import com.google.api.client.util.Value;

public class MessageList {
	public static class Message {
		public enum Type { @Value INFO, @Value INFO_HOLD, @Value WARNING, @Value ERROR }
		@Key String description;
		@Key int code;
		@Key Type type;

		public Type getType() {
			return type;
		}
		public int getCode() {
			return code;
		}
		public String getDescription() {
			return description;
		}
	}
	@Key("Message") public List<Message> message;

	public Stream<Message> stream() {
		return Stream.ofNullable(message).flatMap(List::stream);
	}

	@Override
	public String toString() {
		return stream().map(Message::getDescription).collect(Collectors.joining(", "));
	}

	public boolean isInvalidContract() {
		return stream().anyMatch(m -> m.type == Message.Type.ERROR && m.code == 1002);
	}
}
