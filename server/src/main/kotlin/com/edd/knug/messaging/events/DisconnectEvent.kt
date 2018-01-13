package com.edd.knug.messaging.events

import com.edd.knug.messaging.Event
import org.eclipse.jetty.websocket.api.Session

data class DisconnectEvent(val session: Session) : Event