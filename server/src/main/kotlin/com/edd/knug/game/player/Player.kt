package com.edd.knug.game.player

import org.dyn4j.dynamics.Body

data class Player(
        val name: String,
        val id: String,
        val body : Body,
        var movingUp: Boolean,
        var movingDown: Boolean,
        var movingLeft: Boolean,
        var movingRight: Boolean
)