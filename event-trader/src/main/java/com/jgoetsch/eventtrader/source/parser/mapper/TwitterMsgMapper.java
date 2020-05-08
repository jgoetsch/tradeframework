package com.jgoetsch.eventtrader.source.parser.mapper;

import javax.validation.constraints.NotNull;

import com.jgoetsch.eventtrader.Msg;

public class TwitterMsgMapper implements MsgMappable {

	@NotNull private String text;
	@NotNull private User user;

	private static class User {
		private String screenName;
		private String profileImageUrl;
	}
	
	@Override
	public Msg toMsg() {
		Msg msg = new Msg(user.screenName, text);
		msg.setImageUrl(user.profileImageUrl);
		return msg;
	}

}