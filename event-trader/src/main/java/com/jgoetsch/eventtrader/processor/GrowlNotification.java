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
package com.jgoetsch.eventtrader.processor;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.jgntp.Gntp;
import com.google.code.jgntp.GntpApplicationInfo;
import com.google.code.jgntp.GntpClient;
import com.google.code.jgntp.GntpNotificationInfo;
import com.jgoetsch.eventtrader.Msg;

/**
 * Processor that triggers a Growl notification for an incoming message. By
 * default <code>sourceName</code> and <code>message</code> fields of the
 * incoming message are used for the title and message body respectively of the
 * notification. This can be changed by overriding the <code>getTitle</code> and
 * <code>getMessageBody</code> methods.
 * 
 * @author jgoetsch
 * 
 */
public class GrowlNotification implements Processor<Msg> {

	public static class GntpClientBean {
		private String appName;
		private String appIcon;
		private Set<String> notificationTypes = new LinkedHashSet<String>();
		private long timeout = 3000;
		private int port = Gntp.WINDOWS_TCP_PORT;

		private Map<String, GntpNotificationInfo> notificationInfos;
		private GntpClient client;
		private Logger log = LoggerFactory.getLogger(GntpClientBean.class);

		@PostConstruct
		public void initialize() {
			GntpApplicationInfo info = Gntp.appInfo(appName).build();
			notificationInfos = new HashMap<String, GntpNotificationInfo>();
			for (String notificationType : notificationTypes) {
				notificationInfos.put(notificationType, Gntp.notificationInfo(info, notificationType).build());
				log.debug("Registering notification type: " + notificationType);
			}
			client = Gntp.client(info).onPort(port).build();
			client.register();
		}
	
		public void sendNotification(String notificationType, String title, String msg, RenderedImage image) {
			if (client == null)
				initialize();

			GntpNotificationInfo notificationInfo = notificationInfos.get(notificationType);
			if (notificationInfo != null) {
				try {
					client.notify(Gntp.notification(notificationInfo, title).text(msg).icon(image).build(), timeout, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					log.warn("GNTP notification timed out", e);
				}
			} else {
				throw new IllegalArgumentException("Attempt to notify with unregistered notification type " + notificationType);
			}
		}

		public String getAppName() {
			return appName;
		}
	
		public void setAppName(String appName) {
			this.appName = appName;
		}
	
		public String getAppIcon() {
			return appIcon;
		}
	
		public void setAppIcon(String appIcon) {
			this.appIcon = appIcon;
		}
	
		public Set<String> getNotificationTypes() {
			return notificationTypes;
		}
	
		public void setNotificationTypes(Set<String> notificationTypes) {
			this.notificationTypes = notificationTypes;
		}

		public long getTimeout() {
			return timeout;
		}

		public void setTimeout(long timeout) {
			this.timeout = timeout;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}
	}

	private Logger log = LoggerFactory.getLogger(GrowlNotification.class);
	private GntpClientBean client;
	private String appName = "EventTrader";
	private String notificationType;
	private URL imageURL;
	private boolean sticky;

	private BufferedImage bufferedImage;

	private static Map<String, GntpClientBean> autoRegisterNotifications = new HashMap<String, GntpClientBean>();

	public GrowlNotification() {
	}

	/**
	 * @param alertType
	 * @param nextProcessor
	 * @throws IOException 
	 */
	public GrowlNotification(String appName, String notificationType) throws IOException {
		this.appName = appName;
		this.notificationType = notificationType;
		initialize();
	}

	@PostConstruct
	public void initialize() throws IOException {
		if (notificationType == null)
			throw new IllegalStateException("Required property \"alertType\" not set");

		if (client == null) {
			if (appName != null) {
				synchronized (autoRegisterNotifications) {
					client = autoRegisterNotifications.get(appName);
					if (client == null) {
						client = new GntpClientBean();
						client.setAppName(appName);
						autoRegisterNotifications.put(appName, client);
					}
					client.getNotificationTypes().add(notificationType);
				}
			} else {
				throw new IllegalStateException("Autoregistered notification must specify appName property");
			}
		}
		if (imageURL != null) {
			bufferedImage = ImageIO.read(imageURL);
		}
	}

	/**
	 * Autoregisters all notifications without a client reference explicitly set.
	 * Calling this is not necessarily required as each client will be initialized
	 * anyway the first time one of its notifications is fired.
	 */
	public static void autoRegister() {
		for (GntpClientBean client : autoRegisterNotifications.values()) {
			client.initialize();
		}
	}

	public void process(Msg msg, Map<Object,Object> context) {
		if (client != null) {
			try {
				BufferedImage image = null;
				if (msg.getImageUrl() != null) {
					try {
						image = ImageIO.read(new URL(msg.getImageUrl()));
					} catch (Exception e) {
						log.debug("Failed to load Msg imageUrl", e);
					}
				}
				else {
					image = bufferedImage;
				}
				client.sendNotification(getNotificationType(), getTitle(msg), getMessageBody(msg), image);
			} catch (Exception e) {
				log.warn("Growl notification failed", e);
			}
		}
		else
			log.error("initialize method on GrowlNotification not called, Growl notification cannot be sent");
	}

	protected String getTitle(Msg msg) {
		return msg.getSourceName() != null ? msg.getSourceName() : notificationType;
	}

	protected String getMessageBody(Msg msg) {
		return msg.getMessage() != null ? msg.getMessage() : "(no content)";
	}

	public void setSticky(boolean sticky) {
		this.sticky = sticky;
	}

	public boolean isSticky() {
		return sticky;
	}

	public void setNotificationType(String notificationType) {
		this.notificationType = notificationType;
	}

	public String getNotificationType() {
		return notificationType;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppName() {
		return appName;
	}

	public void setImageURL(URL imageURL) {
		this.imageURL = imageURL;
	}

	public URL getImageURL() {
		return imageURL;
	}

	public void setClient(GntpClientBean client) {
		this.client = client;
	}

	public GntpClientBean getClient() {
		return client;
	}

}
