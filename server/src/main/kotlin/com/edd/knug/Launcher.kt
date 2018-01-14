package com.edd.knug

import com.edd.knug.player.PlayerManager
import com.edd.knug.player.PlayerRegistry
import com.edd.knug.util.TICK_RATE
import com.edd.knug.util.WEB_SOCKET_ENDPOINT
import com.edd.knug.util.loop.FixedTimeStepLoop
import com.edd.knug.util.messaging.EventMessaging
import com.edd.knug.websockets.ConvertingWebSocketHandler
import com.edd.knug.websockets.PlayerMessaging
import com.google.gson.Gson
import com.google.gson.JsonParser
import org.dyn4j.dynamics.World
import org.dyn4j.geometry.Vector2
import spark.Spark.*

fun main(args: Array<String>) {
    val world = World().apply {
        gravity = Vector2(0.0, 0.0)
    }

    val jsonParser = JsonParser()
    val gson = Gson()

    val eventMessaging = EventMessaging()
    val playerRegistry = PlayerRegistry()

    val playerMessaging = PlayerMessaging(
            playerRegistry,
            gson
    )

    val playerManager = PlayerManager(
            playerRegistry,
            playerMessaging,
            eventMessaging,
            world
    )

    Thread(FixedTimeStepLoop(TICK_RATE, {
        eventMessaging.process()
        world.update(it)
        playerManager.update()
    }), "game-loop").start()

    webSocket(WEB_SOCKET_ENDPOINT, ConvertingWebSocketHandler(
            eventMessaging, jsonParser
    ))

    port(10000)
    init()
}