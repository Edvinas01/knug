package com.edd.knug.messages

import com.edd.knug.websockets.Message

data class GameStateMessage(
        val players: Collection<PlayerState>
) : Message {

    override val type: String = "state"
}

data class PlayerState(
        val position: Position,
        val rotation: Double,
        val id: String
)