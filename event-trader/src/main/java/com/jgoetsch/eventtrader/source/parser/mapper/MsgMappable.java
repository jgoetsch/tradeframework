package com.jgoetsch.eventtrader.source.parser.mapper;

import com.jgoetsch.eventtrader.Msg;

public interface MsgMappable {

	default boolean hasMsg() {
		return true;
	}

	Msg toMsg();

}