package com.edd.knug.events

import com.edd.knug.util.messaging.Event
import org.eclipse.jetty.websocket.api.Session

data class DisconnectEvent(val session: Session) : Event {

    override fun compareTo(other: Event) = 1
}