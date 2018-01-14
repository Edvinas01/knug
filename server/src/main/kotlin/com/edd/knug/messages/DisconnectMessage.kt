package com.edd.knug.messages

import com.edd.knug.websockets.Message

data class DisconnectMessage(
        val id: String
) : Message {

    override val type: String = "disconnect"
}