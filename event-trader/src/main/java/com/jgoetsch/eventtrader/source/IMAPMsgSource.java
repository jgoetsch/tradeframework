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

import java.io.IOException;
import java.util.Properties;

import javax.annotation.PreDestroy;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.InternetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.eventtrader.Msg;
import com.sun.mail.imap.IMAPFolder;

public class IMAPMsgSource extends MsgSource {

	private Logger log = LoggerFactory.getLogger(IMAPMsgSource.class);
	private String host;
	private String username;
	private String password;
	private String folderName;
	private volatile boolean bShutdown = false;
	private IMAPFolder folder;
	private Thread mainThread;

	public IMAPMsgSource() {
	}

	public IMAPMsgSource(String host, String username, String password, String folderName) {
		this();
		this.host = host;
		this.username = username;
		this.password = password;
		this.folderName = folderName;
	}

	public void receiveMsgs() {
		mainThread = Thread.currentThread();
		MessageCountListener listener = new MessageCountListener() {
			public void messagesAdded(MessageCountEvent event) {
				for (Message mailMsg : event.getMessages()) {
					try {
						String content = mailMsg.getSubject();
						if (mailMsg.getContent() instanceof String) {
							content = content.concat("\n" + ((String)mailMsg.getContent()));
						}
						else if (mailMsg.getContent() instanceof Multipart) {
							Multipart multipart = (Multipart)mailMsg.getContent();
							for (int i = 0; i < multipart.getCount(); i++) {
								if (multipart.getBodyPart(i).getContentType().toLowerCase().startsWith("text/plain"))
									content = content.concat("\n" + (String)multipart.getBodyPart(i).getContent());
							}
						}
						if (content.length() > 512)
							content = content.substring(0, 512);

						Msg msg = new Msg(null, ((InternetAddress)mailMsg.getFrom()[0]).getPersonal(), content);
						msg.setSourceType(mailMsg.getFolder().getFullName());
						newMsg(msg);
						log.debug("New email received: " + msg);
					} catch (MessagingException e) {
						log.error("Error receiving mail message", e);
					} catch (IOException e) {
						log.error("Error receiving mail message", e);
					}
				}
			}
			public void messagesRemoved(MessageCountEvent event) {
			}
		};

		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		Session session = Session.getDefaultInstance(props);
		Store store;
		try {
			store = session.getStore("imaps");
		} catch (NoSuchProviderException e) {
			throw new RuntimeException(e);
		}

		try {
			store.connect(host, username, password);
			folder = (IMAPFolder)store.getFolder(folderName);
			folder.addMessageCountListener(listener);
			log.info("Connnected to " + host + ", waiting for new messages");
		}
		catch (MessagingException e) {
			log.error("Error connecting to {}", host, e);
			return;
		}

		try {
			while (!bShutdown) {
				if (!store.isConnected()) {
					store.connect(host, username, password);
					folder = (IMAPFolder)store.getFolder(folderName);
					folder.addMessageCountListener(listener);
					log.info("Reconnnected to " + host + ", waiting for new messages");
				}
				if (!folder.isOpen())
					folder.open(Folder.READ_ONLY);
				folder.idle();
			}
			log.info("Disconnecting from " + host);
		}
		catch (MessagingException e) {
			log.warn("Error waiting for messages on {}", host, e);
			return;
		}
	}

	@PreDestroy
	public void shutdown() {
		bShutdown = true;
		if (folder != null) {
			try {
				folder.close(true);
			} catch (MessagingException e) {}
		}
		joinMainThread(3000);
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

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

}
