package com.edd.knug.messaging.events

import com.edd.knug.messaging.Event
import org.eclipse.jetty.websocket.api.RemoteEndpoint
import org.eclipse.jetty.websocket.api.Session

data class ConnectEvent(
        val session: Session,
        val remote: RemoteEndpoint
) : Event