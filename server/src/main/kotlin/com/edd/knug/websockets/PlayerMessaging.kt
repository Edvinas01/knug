package com.edd.knug.websockets

import com.edd.knug.player.Player
import com.edd.knug.player.PlayerRegistry
import com.google.gson.Gson
import mu.KLogging
import java.io.IOException

class PlayerMessaging(
        private val playerRegistry: PlayerRegistry,
        private val gson: Gson
) {

    private companion object : KLogging()

    /**
     * Send a raw message to provided player.
     */
    fun send(player: Player, message: Any) {
        send(player, gson.toJson(message))
    }

    /**
     * Send a formed message to provided player and handle any IO errors.
     */
    private fun send(player: Player, message: String) {
        playerRegistry.getSession(player)?.also { (session, remote) ->
            try {
                remote.sendString(message)
            } catch (e : IOException) {
                logger.error(e) { "Could not send message to player with id: ${player.id}" }
                session.close(1000, "Errors when sending message to player")
            }
        }
    }
}