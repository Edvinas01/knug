package com.edd.knug.messaging.events

import com.edd.knug.messaging.Event
import org.eclipse.jetty.websocket.api.Session

data class InputEvent(
        val session: Session,
        val up: Boolean,
        val down: Boolean,
        val left: Boolean,
        val right: Boolean
) : Event