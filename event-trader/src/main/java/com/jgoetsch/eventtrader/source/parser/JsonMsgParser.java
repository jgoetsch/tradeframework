package com.jgoetsch.eventtrader.source.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jgoetsch.eventtrader.source.MsgHandler;
import com.jgoetsch.eventtrader.source.parser.mapper.MsgMappable;

public class JsonMsgParser implements MsgParser, BufferedMsgParser {

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	private final ObjectReader reader;

	public JsonMsgParser(Class<? extends MsgMappable> mapperClass) {
		this.reader = new ObjectMapper()
		.setVisibility(PropertyAccessor.ALL, Visibility.NONE)
		.setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
		.registerModule(new JavaTimeModule())
		.readerFor(mapperClass);
	}

	@Override
	public boolean parseContent(InputStream input, long length, String contentType, MsgHandler handler) throws MsgParseException
	{
		return parseContent(handler, () -> reader.readValue(input));
	}

	@Override
	public boolean parseContent(String content, String contentType, MsgHandler handler) throws MsgParseException
	{
		return parseContent(handler, () -> reader.readValue(content));
	}

	private boolean parseContent(MsgHandler handler, JsonParsingSupplier<MsgMappable> parser) throws MsgParseException
	{
		try {
			MsgMappable rawObject = parser.get();
			if (rawObject.hasMsg()) {
				Set<ConstraintViolation<MsgMappable>> errors = validator.validate(rawObject);
				if (!errors.isEmpty()) {
					throw new MsgParseException("Problem with received data: " +
							errors.stream().map(v -> v.getPropertyPath() + " " + v.getMessage())
							.collect(Collectors.joining(", ")));
				}
				return handler.newMsg(rawObject.toMsg());
			}
			else {
				return false;
			}
		} catch (IOException ex) {
			throw new MsgParseException(ex);
		}
	}

	@FunctionalInterface
	private interface JsonParsingSupplier<T> {
		T get() throws IOException;
	}

}
