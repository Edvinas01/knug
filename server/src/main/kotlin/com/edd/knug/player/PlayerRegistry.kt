package com.edd.knug.player

import org.eclipse.jetty.websocket.api.RemoteEndpoint
import org.eclipse.jetty.websocket.api.Session

class PlayerRegistry {

    private val playerStore = HashMap<Session, Player>()
    private val remoteStore = HashMap<String, Pair<Session, RemoteEndpoint>>()

    fun getPlayer(session: Session) = playerStore[session]

    fun getOtherPlayers(current: Player) = getPlayers().filter {
        it.id !== current.id
    }

    /**
     * @return list of registered players.
     */
    fun getPlayers() = playerStore.filter {
        it.key.isOpen
    }.map {
        it.value
    }

    /**
     * Register a player with a session and remote endpoint.
     */
    fun add(player: Player, session: Session, remote: RemoteEndpoint) {
        playerStore[session] = player
        remoteStore[player.id] = session.to(remote)
    }

    /**
     * Remove player data related to a session.
     *
     * @return removed player data.
     */
    fun remove(session: Session) = playerStore.remove(session)?.let {
        remoteStore.remove(it.id)
        it
    }

    /**
     * @return remote endpoint associated with the player.
     */
    fun getSession(player: Player) = remoteStore[player.id]
}