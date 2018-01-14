package com.edd.knug.player

import com.edd.knug.events.ConnectEvent
import com.edd.knug.events.DisconnectEvent
import com.edd.knug.events.InputEvent
import com.edd.knug.messages.*
import com.edd.knug.util.*
import com.edd.knug.util.messaging.EventMessaging
import com.edd.knug.websockets.PlayerMessaging
import mu.KLogging
import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.World
import org.dyn4j.geometry.Geometry
import org.dyn4j.geometry.MassType
import org.dyn4j.geometry.Vector2
import java.util.*

class PlayerManager(
        private val playerRegistry: PlayerRegistry,
        private val playerMessaging: PlayerMessaging,
        private val eventMessaging: EventMessaging,
        private val world: World
) {

    private companion object : KLogging()

    init {
        initListeners()
    }

    fun update() {
        val players = playerRegistry.getPlayers()

        players.forEach { player ->
            update(player)
        }

        val message = GameStateMessage(players.map { (_, id, body) ->
            body.transform.let {
                PlayerState(
                        Position(it.translationX.visualUnits, it.translationY.visualUnits),
                        it.rotation,
                        id
                )
            }
        })

        // Can probably avoid double looping.
        players.forEach {
            playerMessaging.send(it, message)
        }
    }

    private fun update(player: Player) {
        val force = 5.0
        val body = player.body

        if (player.movingUp) {
            body.applyForce(Vector2(0.0, force))
        }
        if (player.movingDown) {
            body.applyForce(Vector2(0.0, -force))
        }
        if (player.movingLeft) {
            body.applyForce(Vector2(-force, 0.0))
        }
        if (player.movingRight) {
            body.applyForce(Vector2(force, 0.0))
        }
    }

    private fun create(name: String?): Player {
        val body = Body().apply {
            addFixture(Geometry.createRectangle(
                    PLAYER_WIDTH.worldUnits,
                    PLAYER_HEIGHT.worldUnits
            ))

            setMass(MassType.NORMAL)
            translate((WORLD_WIDTH / 2).worldUnits, (WORLD_HEIGHT / 2).worldUnits)
            world.addBody(this)
        }

        val id = UUID.randomUUID().toString()
        return Player(
                name = name ?: id,
                id = id,
                body = body,
                movingUp = false,
                movingDown = false,
                movingLeft = false,
                movingRight = false
        )
    }

    private fun initListeners() {
        eventMessaging.listen<ConnectEvent> { (session, remote) ->
            val player = create(
                    session.upgradeRequest.parameterMap["name"]?.firstOrNull()
            )

            playerRegistry.add(player, session, remote)
            logger.debug { "Player with id: ${player.id} and name: ${player.name} has connected" }

            // TODO: remove test world sending, use concrete class.
            playerMessaging.send(player, Json.obj {
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

            val message = player.body.transform.let {
                ConnectMessage(
                        Position(it.translationX.visualUnits, it.translationY.visualUnits),
                        Size(PLAYER_WIDTH, PLAYER_HEIGHT),
                        player.name,
                        player.id
                )
            }

            playerRegistry.getOtherPlayers(player).forEach {
                playerMessaging.send(it, message)
            }
        }

        eventMessaging.listen<DisconnectEvent> { (session) ->
            playerRegistry.remove(session)?.also { player ->
                world.removeBody(player.body)
                logger.debug { "Player with id: ${player.id} has disconnected" }

                val message = DisconnectMessage(player.id)
                playerRegistry.getOtherPlayers(player).forEach {
                    playerMessaging.send(it, message)
                }
            }
        }

        eventMessaging.listen<InputEvent> {
            playerRegistry.getPlayer(it.session)?.also { player ->
                player.movingUp = it.up
                player.movingDown = it.down
                player.movingLeft = it.left
                player.movingRight = it.right
            }
        }
    }
}