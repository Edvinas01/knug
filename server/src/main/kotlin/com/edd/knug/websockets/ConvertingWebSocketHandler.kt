package com.edd.knug.websockets

import com.edd.knug.events.ConnectEvent
import com.edd.knug.events.DisconnectEvent
import com.edd.knug.events.InputEvent
import com.edd.knug.util.SESSION_IDLE_TIMEOUT
import com.edd.knug.util.messaging.Event
import com.edd.knug.util.messaging.EventMessaging
import com.google.gson.JsonParser
import mu.KLogging
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.WebSocketAdapter

class ConvertingWebSocketHandler(
        private val eventMessaging: EventMessaging,
        private val jsonParser: JsonParser
) : WebSocketAdapter() {

    private companion object : KLogging()

    override fun onWebSocketConnect(session: Session) {
        super.onWebSocketConnect(session)

        session.idleTimeout = SESSION_IDLE_TIMEOUT
        eventMessaging.send(ConnectEvent(session, remote))
    }

    override fun onWebSocketText(message: String) {
        convertAndSend(message)
    }

    override fun onWebSocketClose(statusCode: Int, reason: String?) {
        eventMessaging.send(DisconnectEvent(session))

        // super, will nullify the session so gotta call it last.
        super.onWebSocketClose(statusCode, reason)
    }

    override fun onWebSocketError(cause: Throwable) {
        logger.error(cause) { "Unhandled web socket error" }
    }

    /**
     * Convert raw message to internal event.
     */
    private fun convert(message: String): Event? {
        val tree = jsonParser.parse(message)
        if (!tree.isJsonObject) {
            return null
        }

        val obj = tree.asJsonObject
        return when (obj["type"]?.asString) {
            "input" -> InputEvent(
                    session,
                    obj["up"].asBoolean,
                    obj["down"].asBoolean,
                    obj["left"].asBoolean,
                    obj["right"].asBoolean
            )
            else -> {
                logger.debug { "Unknown message: $message" }
                null
            }
        }
    }

    /**
     * Convert raw message to internal event and forward it to event chain.
     */
    private fun convertAndSend(message: String) {
        if (isConnected) {
            convert(message)?.also {
                eventMessaging.send(it)
            }
        }
    }
}