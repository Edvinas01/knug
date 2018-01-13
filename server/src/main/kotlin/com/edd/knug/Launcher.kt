package com.edd.knug

import com.edd.knug.messaging.events.ConnectEvent
import com.edd.knug.messaging.events.DisconnectEvent
import com.edd.knug.messaging.Messaging
import com.edd.knug.player.PlayerManager
import com.edd.knug.player.PlayerRegistry
import com.edd.knug.messaging.events.InputEvent
import com.edd.knug.util.*
import com.edd.knug.websockets.GameWebSocketHandler
import com.edd.knug.util.loop.FixedTimeStepLoop
import com.edd.knug.websockets.MessageSender
import org.dyn4j.dynamics.World
import org.dyn4j.geometry.Vector2
import spark.Spark.*

fun main(args: Array<String>) {
    val world = World().apply {
        gravity = Vector2(0.0, 0.0)
    }

    val messaging = Messaging()
    val json = Json()

    val playerManager = PlayerManager(world)
    val playerRegistry = PlayerRegistry(playerManager)
    val sender = MessageSender(playerRegistry, json)

    // Initialize event listeners.
    messaging.listen<ConnectEvent> { (session, remote) ->
        val player = playerRegistry.add(session, remote)

        // TODO: remove test world sending, use concrete class.
        sender.send(player, json.write {
            "type" to "setup"
            "world" to json {
                "size" to json {
                    "width" to WORLD_WIDTH
                    "height" to WORLD_HEIGHT
                }
            }
            "entities" to json {
                "players" to playerRegistry.getPlayers().map {
                    val body = it.body
                    val transform = body.transform
                    val controlled = it.id == player.id

                    json {

                        // Are we controlling this player?
                        "controlled" to controlled
                        "name" to it.name
                        "id" to it.id
                        "position" to json {
                            "x" to transform.translationX.visualUnits
                            "y" to transform.translationY.visualUnits
                        }
                        "size" to json {
                            "width" to PLAYER_WIDTH
                            "height" to PLAYER_HEIGHT
                        }
                    }
                }
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
        playerRegistry.getPlayer(it.session)?.also { player ->
            player.movingUp = it.up
            player.movingDown = it.down
            player.movingLeft = it.left
            player.movingRight = it.right
        }
    }

    // Main game handler thread.
    Thread(FixedTimeStepLoop(20, {
        world.update(it)

        playerManager.update(playerRegistry.getPlayers())

        messaging.process()

        // TODO: move this else where, use concrete class.
        playerRegistry.getPlayers().forEach {
            val transform = it.body.transform

            sender.send(it, json.write {
                "type" to "state"
                "players" to arrayOf(json {
                    "name" to it.name
                    "id" to it.id
                    "position" to json {
                        "x" to transform.translationX.visualUnits
                        "y" to transform.translationY.visualUnits
                    }
                    "rotation" to transform.rotation
                })
            })
        }

    }), "game-loop").start()

    webSocket(WEB_SOCKET_ENDPOINT, GameWebSocketHandler(messaging, json))

    port(10000)
    init()
}