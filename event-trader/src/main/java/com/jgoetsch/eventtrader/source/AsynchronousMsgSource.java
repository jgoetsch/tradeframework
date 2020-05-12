package com.jgoetsch.eventtrader.source;

public abstract class AsynchronousMsgSource extends MsgSource {
	private boolean shouldShutdown = false;

	protected synchronized final void enterWaitingLoop() {
		while (!shouldShutdown) {
			try {
				wait();
			} catch (InterruptedException e) {
				shouldShutdown = true;
			}
		}
	}

	public synchronized final void shutdown() {
		shouldShutdown = true;
		notifyAll();
	}
}
