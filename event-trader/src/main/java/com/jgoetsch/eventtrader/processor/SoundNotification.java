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

import java.applet.Applet;
import java.applet.AudioClip;
import java.net.MalformedURLException;
import java.net.URL;

import com.jgoetsch.eventtrader.Msg;

/**
 * Processor that plays an audio file on receiving a message.
 * 
 * @author jgoetsch
 *
 */
public class SoundNotification<M extends Msg> implements Processor<M> {

	private URL url;

	public void process(M msg, ProcessorContext context) {
		AudioClip clip = Applet.newAudioClip(url);
		clip.play();
	}

	public void initialize() {
		if (url == null)
			throw new IllegalStateException("Required property \"audioUrl\" not set");
		/*process(null);
		try {
			InputStream i = url.openStream();
			log.info(i);
			i.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}

	public void setAudioURL(String audioURL) throws MalformedURLException {
		this.url = new URL(audioURL);
	}

	public String getAudioURL() {
		return url.toExternalForm();
	}

}
