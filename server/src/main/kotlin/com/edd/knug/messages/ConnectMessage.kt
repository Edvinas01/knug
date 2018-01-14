package com.edd.knug.messages

import com.edd.knug.websockets.Message

data class ConnectMessage(
        val position: Position,
        val size: Size,
        val name: String,
        val id: String
) : Message {

    override val type: String = "connect"
}