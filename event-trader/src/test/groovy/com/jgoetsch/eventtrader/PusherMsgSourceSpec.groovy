package com.jgoetsch.eventtrader

import spock.lang.Specification
import com.jgoetsch.eventtrader.source.PusherMsgSource
import com.pusher.client.Pusher;


class PusherMsgSourceSpec extends Specification {

	def pusher = Mock(Pusher)
	def pusherMsgSource = new PusherMsgSource(pusher)

}
