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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PreDestroy;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.jboss.netty.util.ThreadNameDeterminer;
import org.jboss.netty.util.ThreadRenamingRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoetsch.eventtrader.source.parser.BufferedMsgParser;

/**
 * Source for streaming messages from a Socket.IO server
 * using WebSockets as the transport.
 * 
 */

@Deprecated
public class SocketIOWebSocketMsgSource extends UrlBasedMsgSource {
	private Logger log = LoggerFactory.getLogger(getClass());
	private BufferedMsgParser msgParser;

	private ClientBootstrap bootstrap;
	private Channel ch;

	protected String getTokenUrl() throws IOException, URISyntaxException {
		URI base = new URI(getUrl());
		BufferedReader tokenReader = new BufferedReader(new InputStreamReader(new URI("http",
				base.getUserInfo(), base.getHost(), base.getPort(), base.getPath(),
				base.getQuery(), base.getFragment()).toURL().openStream()));
		String token = tokenReader.readLine();
		tokenReader.close();

		String r[] = token.split(":");
		String comp[] = getUrl().split("\\?");
		if (!comp[0].endsWith("/"))
			comp[0] += '/';
		return comp[0] + "websocket/" + r[0] + (comp.length > 0 ? "?" + comp[1] : "");
	}

	@Override
	protected void receiveMsgs() {
		final String baseThreadName = Thread.currentThread().getName();
		ThreadRenamingRunnable.setThreadNameDeterminer(ThreadNameDeterminer.CURRENT);
		ThreadFactory threadFactory = new ThreadFactory() {
			private AtomicInteger n = new AtomicInteger();
			public Thread newThread(Runnable r) {
				return new Thread(r, baseThreadName + "-w-" + n.incrementAndGet());
			}
		};
		bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(threadFactory), Executors.newCachedThreadPool(threadFactory)));

		try {
			URI url = new URI(getTokenUrl());

			final WebSocketClientHandshaker handshaker = new WebSocketClientHandshakerFactory().newHandshaker(
					url, WebSocketVersion.V13, null, false, null);

			bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
				public ChannelPipeline getPipeline() throws Exception {
					ChannelPipeline pipeline = Channels.pipeline();
					pipeline.addLast("decoder", new HttpResponseDecoder());
					pipeline.addLast("encoder", new HttpRequestEncoder());
					pipeline.addLast("ws-handler", new WebSocketClientHandler(handshaker));
					pipeline.addLast("sio-handler", new SocketIOClientHandler());
					return pipeline;
				}
			});

			ChannelFuture future = bootstrap.connect(new InetSocketAddress(url.getHost(), url.getPort() == -1 ? 80 : url.getPort()));
			future.syncUninterruptibly();
			ch = future.getChannel();
			handshaker.handshake(ch).syncUninterruptibly();
			ch.getCloseFuture().awaitUninterruptibly();
		} catch (URISyntaxException use) {
			log.error("Invalid URL: {}", getUrl(), use);
		} catch (Exception e) {
			log.error("Error getting token", e);
		} finally {
			if (ch != null)
				ch.close();
			bootstrap.releaseExternalResources();
		}
	}

	@PreDestroy
	public void shutdown() {
		if (ch != null)
			ch.close();
		joinMainThread(2000);
	}

	private class WebSocketClientHandler extends SimpleChannelUpstreamHandler {

		private final WebSocketClientHandshaker handshaker;

		public WebSocketClientHandler(WebSocketClientHandshaker handshaker) {
			this.handshaker = handshaker;
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			if (!handshaker.isHandshakeComplete()) {
				handshaker.finishHandshake(ctx.getChannel(), (HttpResponse)e.getMessage());
				log.info("Connected to websocket host {}", ctx.getChannel().getRemoteAddress());
				return;
			}
			super.messageReceived(ctx, e);
		}

		@Override
		public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
			log.info("Websocket connection to {} closed", ctx.getChannel().getRemoteAddress());
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
			log.error("Websocket exception", e.getCause());
			e.getChannel().close();
		}
	}

	private class SocketIOClientHandler extends SimpleChannelUpstreamHandler {
		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			if (e.getMessage() instanceof TextWebSocketFrame) {
				TextWebSocketFrame message = (TextWebSocketFrame) e.getMessage();
				if (message.getText().equals("2::")) {
					log.trace("Heartbeat received");
					ch.write(new TextWebSocketFrame("2::"));
				} else {
					log.debug("Received: {}", message.getText());
					try {
						int idx = message.getText().indexOf('{');
						if (idx >= 0) {
							// { args: [ ... ] }
							if (!msgParser.parseContent(message.getText().substring(idx), null, SocketIOWebSocketMsgSource.this)) {
								ctx.getChannel().close();
							}
						}
					} catch (Exception ex) {
						log.error("Exception occured parsing message: " + message.getText(), ex);
					}
				}
			}
			super.messageReceived(ctx, e);
		}
	}

	public void setMsgParser(BufferedMsgParser msgParser) {
		this.msgParser = msgParser;
	}

	public BufferedMsgParser getMsgParser() {
		return msgParser;
	}
}
