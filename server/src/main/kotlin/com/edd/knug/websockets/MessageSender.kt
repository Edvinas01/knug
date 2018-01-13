package com.edd.knug.websockets

import com.edd.knug.player.Player
import com.edd.knug.player.PlayerRegistry
import com.edd.knug.util.Json

class MessageSender(
        private val playerRegistry: PlayerRegistry,
        private val json: Json
) {

    /**
     * Send a formed message to provided player.
     */
    fun send(player: Player, message: String) {
        try {
            playerRegistry.getRemote(player)?.sendString(message)
        } catch (e: Exception) {
            e.printStackTrace() // TODO: use loggers.
        }
    }

    /**
     * Send a raw message to provided player.
     */
    fun send(player: Player, obj: Any) {
        send(player, json.write(obj))
    }
}