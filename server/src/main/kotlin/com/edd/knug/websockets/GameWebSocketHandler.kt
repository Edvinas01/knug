package com.edd.knug.websockets

import com.edd.knug.messaging.events.ConnectEvent
import com.edd.knug.messaging.events.DisconnectEvent
import com.edd.knug.messaging.Messaging
import com.edd.knug.messaging.events.InputEvent
import com.edd.knug.util.Json
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.WebSocketAdapter

class GameWebSocketHandler(
        private val messaging: Messaging,
        private val json: Json
) : WebSocketAdapter() {

    override fun onWebSocketConnect(session: Session) {
        super.onWebSocketConnect(session)
        messaging.send(ConnectEvent(session, remote))
    }

    override fun onWebSocketText(message: String) {
        if (session == null) {
            return
        }

        // TODO: better type handling should be done, also find a way to avoid using copy.
        if (message.contains("input")) {
            messaging.send(json.read<InputEvent>(message).copy(session = session))
        } else {

            // TODO: replace with logger.
            println(message)
        }
    }

    override fun onWebSocketClose(statusCode: Int, reason: String?) {
        messaging.send(DisconnectEvent(session))

        // super, will nullify the session so gotta call it last.
        super.onWebSocketClose(statusCode, reason)
    }

    override fun onWebSocketError(cause: Throwable?) {
        super.onWebSocketError(cause)
    }
}