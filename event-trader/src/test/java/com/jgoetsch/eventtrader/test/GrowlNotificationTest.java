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
package com.jgoetsch.eventtrader.test;

import java.io.IOException;
import java.util.Collections;

import com.jgoetsch.eventtrader.Msg;
import com.jgoetsch.eventtrader.processor.GrowlNotification;

import junit.framework.TestCase;

public class GrowlNotificationTest extends TestCase {

	public void testSingleAlert() throws IOException {
		GrowlNotification<Msg> growl = new GrowlNotification<Msg>();
		growl.setAppName("GrowlNotificationTest");
		growl.setNotificationType("Growl Alert");
		growl.initialize();

		GrowlNotification<Msg> growl2 = new GrowlNotification<Msg>("GrowlNotificationTest", "Secondary alert");
		GrowlNotification<Msg> growl3 = new GrowlNotification<Msg>("GrowlNotificationTest2", "Second app alert");

		GrowlNotification.autoRegister();

		growl.process(new Msg("GrowlNotificationTest", "Testing auto registered growl notification"), null);
		growl2.process(new Msg("GrowlNotificationTest", "Testing auto registered growl notification"), null);
		growl3.process(new Msg("GrowlNotificationTest", "Testing auto registered growl notification from second app"), null);
	}

	public void testRegisteredClient() throws IOException {
		GrowlNotification.GntpClientBean growlClient = new GrowlNotification.GntpClientBean();
		growlClient.setAppName("GrowlNotificationTest");
		growlClient.setNotificationTypes(Collections.singleton("Growl Alert"));
		growlClient.initialize();

		GrowlNotification<Msg> growl = new GrowlNotification<Msg>();
		growl.setClient(growlClient);
		growl.setNotificationType("Growl Alert");
		growl.initialize();

		growl.process(new Msg("GrowlNotificationTest", "Testing registered client growl notifications"), null);
	}

}
