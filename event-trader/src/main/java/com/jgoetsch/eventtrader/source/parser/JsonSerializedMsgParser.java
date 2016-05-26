package com.jgoetsch.eventtrader.source.parser;

import com.google.gson.Gson;
import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.SerializationUtil;
import com.jgoetsch.eventtrader.TradeSignal;
import com.jgoetsch.eventtrader.source.MsgHandler;

public class JsonSerializedMsgParser implements BufferedMsgParser {

	public boolean parseContent(String content, String contentType, MsgHandler handler) throws MsgParseException {
		Gson gson = SerializationUtil.createGson();

		if ("TradeSignal".equals(contentType))
			return handler.newMsg(gson.fromJson(content, TradeSignal.class));
		else if ("Msg".equals(contentType))
			return handler.newMsg(gson.fromJson(content, Msg.class));
		else
			return true;
	}

}
