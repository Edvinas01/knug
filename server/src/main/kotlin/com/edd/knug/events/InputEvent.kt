package com.edd.knug.events

import com.edd.knug.util.messaging.Event
import org.eclipse.jetty.websocket.api.Session

data class InputEvent(
        val session: Session,
        val up: Boolean,
        val down: Boolean,
        val left: Boolean,
        val right: Boolean
) : Event