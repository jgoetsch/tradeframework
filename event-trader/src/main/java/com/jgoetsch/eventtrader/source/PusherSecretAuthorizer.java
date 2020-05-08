package com.jgoetsch.eventtrader.source;

import com.pusher.client.AuthorizationFailureException;
import com.pusher.client.Authorizer;
import com.pusher.rest.Pusher;
import com.pusher.rest.data.PresenceUser;

public class PusherSecretAuthorizer implements Authorizer {

	private Integer userId = Integer.valueOf(1);
	private Object userInfo;
	private final Pusher pusher;

	public PusherSecretAuthorizer(String appId, String apiKey, String apiSecret) {
		pusher = new Pusher(appId, apiKey, apiSecret);
	}

	public String authorize(String channelName, String socketId) throws AuthorizationFailureException {
		if (channelName.startsWith("presence-"))
			return pusher.authenticate(socketId, channelName, new PresenceUser(userId, userInfo));
		else
			return pusher.authenticate(socketId, channelName);
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Object getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(Object userInfo) {
		this.userInfo = userInfo;
	}

}
