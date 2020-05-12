package com.jgoetsch.eventtrader

import spock.lang.Specification
import spock.lang.Timeout
import com.jgoetsch.eventtrader.processor.Processor
import com.jgoetsch.eventtrader.source.PusherMsgSource
import com.jgoetsch.eventtrader.source.parser.JsonMsgParser
import com.jgoetsch.eventtrader.source.parser.mapper.PdMsgMapper
import com.pusher.client.Client;
import com.pusher.client.channel.ChannelEventListener
import com.pusher.client.channel.PusherEvent

@Timeout(10)
class PusherMsgSourceSpec extends Specification {

	def pusher = Mock(Client)
	def processor = Mock(Processor)
	def pusherMsgSource = new PusherMsgSource(pusher)
	def channel = 'test_channel'

	def setup() {
		pusherMsgSource.setMsgParser(new JsonMsgParser(PdMsgMapper))
		pusherMsgSource.setChannels([channel])
		pusherMsgSource.setNumEvents(1)
		pusherMsgSource.setProcessors([processor])
	}

	def "Can run and accept valid message"() {
		when:
		pusherMsgSource.run()

		then:
		1 * pusher.connect(_, _)

		then:
		1 * pusher.subscribe(channel, _, 'message') >> { String ch, ChannelEventListener l, String[] e ->
			l.onSubscriptionSucceeded(ch)
			l.onEvent(new PusherEvent([event: 'message', data: new File("src/test/resources/messages/commentary_1.json").getText()]))
		}

		then:
		1 * processor.process(_, _)
		1 * pusher.disconnect()
	}

	def "Handles invalid messages"() {
		when:
		pusherMsgSource.run()

		then:
		1 * pusher.subscribe(channel, _, 'message') >> { String ch, ChannelEventListener l, String[] e ->
			l.onSubscriptionSucceeded(ch)
			l.onEvent(new PusherEvent([event: 'message', data: new File("src/test/resources/messages/unrecognized_command_1.json").getText()]))
			l.onEvent(new PusherEvent([event: 'message', data: new File("src/test/resources/messages/missing_field_1.json").getText()]))
			l.onEvent(new PusherEvent([event: 'message', data: new File("src/test/resources/messages/malformed.json").getText()]))
			// valid message to stop the message source
			l.onEvent(new PusherEvent([event: 'message', data: new File("src/test/resources/messages/commentary_1.json").getText()]))
		}

		then:
		1 * processor.process(_, _)
		1 * pusher.disconnect()
	}
}
