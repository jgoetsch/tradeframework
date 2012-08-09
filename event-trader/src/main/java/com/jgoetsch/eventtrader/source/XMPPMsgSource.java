/*
 * Copyright (c) 2012 Jeremy Goetsch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jgoetsch.eventtrader.source;

import javax.annotation.PreDestroy;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.eventtrader.Msg;

public class XMPPMsgSource extends MsgSource {

	private Logger log = LoggerFactory.getLogger(XMPPMsgSource.class);
	private String host;
	private int port = 5222;
	private String serviceName;
	private String username;
	private String password;
	private String remoteUser;
	private boolean bShutdown = false;

	@Override
	protected void receiveMsgs() {
		ConnectionConfiguration connConfig;
		if (serviceName != null)
			connConfig = new ConnectionConfiguration(host, port, serviceName);
		else
			connConfig = new ConnectionConfiguration(host, port);

		connConfig.setSendPresence(false);
		XMPPConnection connection = new XMPPConnection(connConfig);
		try {
			connection.connect();
			connection.login(username, password);
			Presence presence = new Presence(Presence.Type.available, null, 127, Presence.Mode.available);
			connection.sendPacket(presence);

			Chat chat = connection.getChatManager().createChat(
					remoteUser, new MessageListener() {
						public void processMessage(Chat chat, Message message) {
							log.debug("Received message: " + message.getType() + " - " + message.getFrom() + ": " + message.getBody());
							if (message.getBody() != null) {
								String src = message.getFrom().indexOf('@') != -1 ?
										message.getFrom().substring(0, message.getFrom().indexOf('@'))
										: message.getFrom();
								newMsg(new Msg(src, message.getBody()));
							}
						}
					});

			log.info("Connected to XMPP server {}, waiting for messages from {}", host, remoteUser);
			synchronized (this) {
				while (!bShutdown) {
					try {
						wait();
					} catch (InterruptedException e) { }
				}
			}
			log.info("Disconnecting from XMPP server {}", host);
			connection.disconnect();
		} catch (XMPPException e) {
			log.error("Error connecting to {}", host, e);
		}
	}

	@PreDestroy
	public void shutdown() {
		synchronized (this) {
			bShutdown = true;
			notify();
		}
		joinMainThread(2500);
	}
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getPort() {
		return port;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setRemoteUser(String remoteUser) {
		this.remoteUser = remoteUser;
	}

	public String getRemoteUser() {
		return remoteUser;
	}

}
