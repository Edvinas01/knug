package com.edd.knug.player

import com.edd.knug.util.*
import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.World
import org.dyn4j.geometry.Geometry
import org.dyn4j.geometry.MassType
import org.dyn4j.geometry.Vector2
import java.util.*

class PlayerManager(private val world: World) {

    fun create(name: String?): Player {
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

    fun update(players: Collection<Player>) {
        players.forEach {
            update(it)
        }
    }

    private fun update(player: Player) {
        val force = 50.0
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

    fun remove(player: Player) {
        world.removeBody(player.body)
    }
}