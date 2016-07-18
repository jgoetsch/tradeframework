package com.jgoetsch.eventtrader.source;

import com.pusher.client.AuthorizationFailureException;
import com.pusher.client.Authorizer;
import com.pusher.rest.Pusher;
import com.pusher.rest.data.PresenceUser;

public class PusherSecretAuthorizer implements Authorizer {
	private String appId;
	private String apiKey;
	private String apiSecret;

	private Integer userId = new Integer(1);
	private Object userInfo;

	public String authorize(String channelName, String socketId) throws AuthorizationFailureException {
		Pusher pusher = new Pusher(appId, apiKey, apiSecret);
		if (channelName.startsWith("presence-"))
			return pusher.authenticate(socketId, channelName, new PresenceUser(userId, userInfo));
		else
			return pusher.authenticate(socketId, channelName);
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
