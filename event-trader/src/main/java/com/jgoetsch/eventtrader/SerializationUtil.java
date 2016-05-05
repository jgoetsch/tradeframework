package com.jgoetsch.eventtrader;

import java.lang.reflect.Type;
import java.util.Date;

import org.joda.time.DateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class SerializationUtil {

	private static class DateTimeTypeConverter implements JsonSerializer<DateTime>, JsonDeserializer<DateTime> {

		public JsonElement serialize(DateTime src, Type srcType, JsonSerializationContext context) {
			return new JsonPrimitive(src.toString());
		}

		public DateTime deserialize(JsonElement json, Type type, JsonDeserializationContext context)
				throws JsonParseException {
			try {
				return new DateTime(json.getAsString());
			} catch (IllegalArgumentException e) {
				// May be it came in formatted as a java.util.Date, so try that
				Date date = context.deserialize(json, Date.class);
				return new DateTime(date);
			}
		}
	}

	/**
	 * @return a Gson that properly serializes object we use, such as JodaTime
	 */
	public static Gson createGson() {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(DateTime.class, new DateTimeTypeConverter());
		return builder.create();
	}
}
