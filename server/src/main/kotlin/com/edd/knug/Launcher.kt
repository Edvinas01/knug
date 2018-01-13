package com.edd.knug

import com.edd.knug.messaging.events.ConnectEvent
import com.edd.knug.messaging.events.DisconnectEvent
import com.edd.knug.messaging.Messaging
import com.edd.knug.game.player.PlayerManager
import com.edd.knug.game.player.PlayerRegistry
import com.edd.knug.messaging.events.InputEvent
import com.edd.knug.util.*
import com.edd.knug.websocket.GameWebSocketHandler
import com.edd.knug.util.loop.FixedTimeStepLoop
import org.dyn4j.dynamics.World
import org.dyn4j.geometry.Vector2
import spark.Spark.*

fun main(args: Array<String>) {
    val world = World().apply {
        gravity = Vector2(0.0, 0.0)
    }

    val messaging = Messaging()
    val json = Json()

    val playerManager = PlayerManager(world, json)
    val playerRegistry = PlayerRegistry(playerManager)

    // Initialize event listeners.
    messaging.listen<ConnectEvent> { (session, remote) ->
        val player = playerRegistry.add(session)

        val body = player.body
        val transform = body.transform

        // TODO: remove test world sending, use concrete class.
        remote.sendString(json.write {
            "type" to "setup"
            "world" to json {
                "size" to json {
                    "width" to WORLD_WIDTH
                    "height" to WORLD_HEIGHT
                }
            }
            "entities" to json {
                "players" to arrayOf(json {
                    "controlled" to true
                    "name" to player.name
                    "position" to json {
                        "x" to transform.translationX.visualUnits
                        "y" to transform.translationY.visualUnits
                    }
                    "size" to json {
                        "width" to PLAYER_WIDTH
                        "height" to PLAYER_HEIGHT
                    }
                }, json {
                    "name" to "Jeff"
                    "position" to json {
                        "x" to 500
                        "y" to 500
                    }
                    "size" to json {
                        "width" to 128
                        "height" to 128
                    }
                })
                "polygons" to arrayOf(json {
                    "points" to arrayOf(json {
                        "x" to 100
                        "y" to 100
                    }, json {
                        "x" to 300
                        "y" to 100
                    }, json {
                        "x" to 200
                        "y" to 200
                    }, json {
                        "x" to 300
                        "y" to 300
                    }, json {
                        "x" to 100
                        "y" to 300
                    })
                })
            }
        })
    }

    messaging.listen<DisconnectEvent> { (session) ->
        playerRegistry.remove(session)
    }

    messaging.listen<InputEvent> {
        playerRegistry[it.session]?.also { player ->
            player.movingUp = it.up
            player.movingDown = it.down
            player.movingLeft = it.left
            player.movingRight = it.right
        }
    }

    // Main game handler thread.
    Thread(FixedTimeStepLoop(1, {

        // World simulation must be ran first, as other steps depend on its results.
        world.update(it)

        // Each player might produce an event, so gotta handle each player before message
        // processing.
        playerRegistry.sessions.forEach {
            playerManager.update(it.key.remote, playerRegistry.sessions.values, it.value)
        }

        // Lastly, all events produced by players or world can be handled.
        messaging.process()

    }), "game-loop").start()

    webSocket(WEB_SOCKET_ENDPOINT, GameWebSocketHandler(messaging, json))

    port(10000)
    init()
}