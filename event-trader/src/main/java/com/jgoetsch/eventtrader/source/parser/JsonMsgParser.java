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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
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
		return parseContent(handler, null, input);
	}

	@Override
	public boolean parseContent(String content, String contentType, MsgHandler handler) throws MsgParseException
	{
		return parseContent(handler, content, null);
	}

	private boolean parseContent(MsgHandler handler, String content, InputStream input) throws MsgParseException
	{
		try {
			MsgMappable rawObject = content != null ? reader.readValue(content) : reader.readValue(input);
			Set<ConstraintViolation<MsgMappable>> errors = validator.validate(rawObject);
			if (!errors.isEmpty()) {
				throw new MsgParseException(buildExceptionMsg("Missing or invalid fields in parsed input: " +
						errors.stream().map(v -> v.getPropertyPath() + " " + v.getMessage())
						.collect(Collectors.joining(", ")), content, null)
						);
			}
			return handler.newMsg(rawObject.toMsg());
		}
		catch (InvalidTypeIdException ex) {
			throw new UnrecognizedMsgTypeException(buildExceptionMsg(ex.getOriginalMessage(), content, ex), ex.getTypeId(), ex);
		}
		catch (JsonProcessingException ex) {
			throw new MsgParseException(buildExceptionMsg(ex.getOriginalMessage(), content, ex), ex);
		}
		catch (IOException ex) {
			throw new MsgParseException(ex.getMessage(), ex);
		}
	}

	private String buildExceptionMsg(String msg, String content, JsonProcessingException ex) {
		StringBuilder builder = new StringBuilder(msg);
		if (ex != null && ex.getLocation() != null) {
			builder.append("\n at line: ").append(ex.getLocation().getLineNr());
			builder.append(" col: ").append(ex.getLocation().getColumnNr());
			builder.append(" of source: ");
		}
		else {
			builder.append("\n in source: ");
		}
		builder.append(content != null ? content : "(InputStream)");
		return builder.toString();
	}
}
