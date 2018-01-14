package com.edd.knug.events

import com.edd.knug.util.messaging.Event
import org.eclipse.jetty.websocket.api.RemoteEndpoint
import org.eclipse.jetty.websocket.api.Session

data class ConnectEvent(
        val session: Session,
        val remote: RemoteEndpoint
) : Event {

    override fun compareTo(other: Event) = 1
}