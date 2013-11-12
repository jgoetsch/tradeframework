package com.jgoetsch.eventtrader.source;

import java.io.IOException;
import java.io.Reader;

import org.atmosphere.wasync.Client;
import org.atmosphere.wasync.ClientFactory;
import org.atmosphere.wasync.Function;
import org.atmosphere.wasync.Request;
import org.atmosphere.wasync.RequestBuilder;
import org.atmosphere.wasync.Socket;
import org.atmosphere.wasync.impl.AtmosphereClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketMsgSource extends UrlBasedMsgSource {
	private Logger log = LoggerFactory.getLogger(getClass());
	private String initialMessage;

	@Override
	protected void receiveMsgs() {
		Client client = ClientFactory.getDefault().newClient();

		RequestBuilder request = client.newRequestBuilder()
				.method(Request.METHOD.GET)
				.uri(getUrl())
				.transport(Request.TRANSPORT.WEBSOCKET);

		try {
	        Socket socket = client.create();
	        socket.on(new Function<String>() {
	            public void on(String r) {

					log.info("Received: " + r);

	            }
	        }).on(new Function<IOException>() {
	
	            public void on(IOException t) {
	            	log.error("IO error in websocket connection", t);
	            }
	
	        }).open(request.build());
	        
	        if (getInitialMessage() != null)
	            socket.fire(getInitialMessage());
	
			for (;;) {
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {}
			}

		} catch (IOException e) {
			log.error("Error connecting to websocket server", e);
		}
	}

	public String getInitialMessage() {
		return initialMessage;
	}

	public void setInitialMessage(String initialMessage) {
		this.initialMessage = initialMessage;
	}

}
